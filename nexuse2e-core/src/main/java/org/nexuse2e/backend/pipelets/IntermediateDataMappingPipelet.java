/**
 * NEXUSe2e Business Messaging Open Source
 * Copyright 2000-2021, direkt gruppe GmbH
 * <p>
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation version 3 of
 * the License.
 * <p>
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.nexuse2e.backend.pipelets;

import org.apache.commons.digester.Digester;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nexuse2e.BeanStatus;
import org.nexuse2e.Engine;
import org.nexuse2e.NexusException;
import org.nexuse2e.backend.pipelets.helper.PartnerSpecificConfiguration;
import org.nexuse2e.backend.pipelets.helper.PartnerSpecificConfigurations;
import org.nexuse2e.configuration.EngineConfiguration;
import org.nexuse2e.configuration.ParameterDescriptor;
import org.nexuse2e.configuration.ParameterType;
import org.nexuse2e.logging.LogMessage;
import org.nexuse2e.messaging.AbstractPipelet;
import org.nexuse2e.messaging.MessageContext;
import org.nexuse2e.service.DataConversionException;
import org.nexuse2e.service.DataConversionService;
import org.nexuse2e.service.Service;
import org.nexuse2e.tools.mapping.FlatFileRecord;
import org.nexuse2e.tools.mapping.xmldata.MappingDefinition;
import org.nexuse2e.tools.mapping.xmldata.MappingDefinitions;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author mbreilmann
 */
public class IntermediateDataMappingPipelet extends AbstractPipelet {

    public static final String PARTNER_SPECIFIC = "partnerSpecific";
    public static final String CONFIG_FILE = "configFile";
    public static final String MAPPING_SERVICE = "mappingService";
    public static final String COMMAND_MAP_LEFT = "$map_left";
    public static final String COMMAND_MAP_RIGHT = "$map_right";
    private static Logger LOG = LogManager.getLogger(IntermediateDataMappingPipelet.class);
    private String configFileName = null;
    private DataConversionService mappingService = null;
    private MappingDefinitions mappingDefinitions = null;
    private PartnerSpecificConfigurations partnerSpecificConfigurations = null;
    private boolean partnerSpecific = false;
    private HashMap<String, MappingDefinitions> partnerConfigurations = null;

    /**
     *
     */
    public IntermediateDataMappingPipelet() {

        parameterMap.put(MAPPING_SERVICE, new ParameterDescriptor(ParameterType.SERVICE, "Data Mapping Service", "The"
                + " Data Mapping and Conversion Service", null));
        parameterMap.put(CONFIG_FILE, new ParameterDescriptor(ParameterType.STRING, "Configuration Path",
                "Path to " + "configuration file", ""));
        parameterMap.put(PARTNER_SPECIFIC, new ParameterDescriptor(ParameterType.BOOLEAN, "Partner Specific " +
                "Configuration", "Partner Specific Configuration", Boolean.FALSE));
    }

    /* (non-Javadoc)
     * @see org.nexuse2e.messaging.AbstractPipelet#initialize(org.nexuse2e.configuration.EngineConfiguration)
     */
    @Override
    public void initialize(EngineConfiguration config) throws InstantiationException {

        File testFile = null;

        String mappingServiceName = getParameter(MAPPING_SERVICE);
        if (!StringUtils.isEmpty(mappingServiceName)) {

            Service service = Engine.getInstance().getActiveConfigurationAccessService().getService(mappingServiceName);
            if (service != null && service instanceof DataConversionService) {
                this.mappingService = (DataConversionService) service;
            }
        }

        Boolean partnerSpecificValue = getParameter(PARTNER_SPECIFIC);
        if (partnerSpecificValue != null) {
            partnerSpecific = partnerSpecificValue.booleanValue();
        }

        String configFileNameValue = getParameter(CONFIG_FILE);
        if ((configFileNameValue != null) && (configFileNameValue.length() != 0)) {
            configFileName = configFileNameValue;
            testFile = new File(configFileName);
            if (!testFile.exists()) {
                status = BeanStatus.ERROR;
                LOG.error("Configuration file does not exist!");
                throw new InstantiationException("Configuration file does not exist!");
            }

            if (partnerSpecific) {
                partnerSpecificConfigurations = readPartnerSpecificConfigurations(configFileName);
                partnerConfigurations = new HashMap<String, MappingDefinitions>();
                for (PartnerSpecificConfiguration partnerSpecificConfiguration :
                        partnerSpecificConfigurations.getPartnerSpecificConfigurations()) {
                    MappingDefinitions partnerMappingDefinitions =
                            readConfiguration(partnerSpecificConfiguration.getConfigurationFile());
                    partnerConfigurations.put(partnerSpecificConfiguration.getPartnerId(), partnerMappingDefinitions);
                }
            } else {
                mappingDefinitions = readConfiguration(configFileName);
            }

        } else {
            status = BeanStatus.ERROR;
            LOG.error("No value for setting 'conversion definition file' provided!");
            throw new InstantiationException("No value for setting 'conversion definition file' provided!");
        }

        LOG.trace("configFileName  : " + configFileName);

        super.initialize(config);
    }

    /* (non-Javadoc)
     * @see org.nexuse2e.messaging.AbstractPipelet#processMessage(org.nexuse2e.messaging.MessageContext)
     */
    @SuppressWarnings("unchecked")
    @Override
    public MessageContext processMessage(MessageContext messageContext) throws IllegalArgumentException,
            IllegalStateException, NexusException {

        if (configFileName != null) {
            Object records = messageContext.getData();
            if ((records != null) && (records instanceof List)) {
                try {
                    processMappings(messageContext.getMessagePojo().getParticipant().getPartner().getPartnerId(),
                            (List<FlatFileRecord>) records);

                    LOG.debug("Modified records: " + records);
                } catch (Exception e) {
                    throw new NexusException(new LogMessage("Error converting records: " + e, messageContext), e);
                }
            } else {
                LOG.warn(new LogMessage("no records found", messageContext));
            }
        } else {
            LOG.warn(new LogMessage("No configuration file specified - no mapping possible.", messageContext));
        }// if

        return messageContext;
    }

    /**
     * @param document
     * @return
     */
    private List<FlatFileRecord> processMappings(String partnerId, List<FlatFileRecord> records) throws NexusException {

        MappingDefinitions theMappingDefinitions = null;

        if (partnerSpecific) {
            theMappingDefinitions = partnerConfigurations.get(partnerId);
        } else {
            theMappingDefinitions = mappingDefinitions;
        }

        if (theMappingDefinitions == null) {
            throw new NexusException("No mappings found for partner: " + partnerId);
        }

        for (MappingDefinition mappingDefinition : theMappingDefinitions.getMappingDefinitions()) {
            LOG.debug("def.Category: " + mappingDefinition.getCategory());
            LOG.debug("def.Command: " + mappingDefinition.getCommand());
            LOG.debug("def.XPath: " + mappingDefinition.getXpath());

            for (FlatFileRecord flatFileRecord : records) {
                String value = flatFileRecord.getColumn(mappingDefinition.getXpath(), null);
                if (value != null) {
                    flatFileRecord.setColumn(mappingDefinition.getXpath(), mapData(value, mappingDefinition));
                } else {
                    // Special handling for command "static":
                    // A static field should be always present, even if there was no such input field.
                    // That's why we add it here.
                    String command = mappingDefinition.getCommand();
                    Pattern pattern = Pattern.compile("[a-zA-Z]+");
                    Matcher matcher = pattern.matcher(command);
                    if (matcher.find()) {
                        String commandName = matcher.group();
                        if (commandName != null && commandName.equals(DataConversionService.STATIC)) {
                            flatFileRecord.setColumn(mappingDefinition.getXpath(), mapData("", mappingDefinition));
                        }
                    }
                }
            }
        }

        return records;
    }

    /**
     * @param sourceValue
     * @param mappingDefinition
     * @return
     */
    private String mapData(String sourceValue, MappingDefinition mappingDefinition) throws DataConversionException {

        String result = null;

        if (mappingService != null) {
            // LOG.debug( "calling mappingservice" );
            String targetValue = mappingService.processConversion(null, null, sourceValue, mappingDefinition);
            if (!StringUtils.isEmpty(targetValue)) {
                return targetValue;
            } else {
                return "";
            }
        } else {
            LOG.error("Data Mapping Service must be configured!");
        }

        return result;
    }

    /**
     * @param configFileName
     * @return
     */
    private PartnerSpecificConfigurations readPartnerSpecificConfigurations(String configFileName) {

        PartnerSpecificConfigurations partnerSpecificConfigurations = null;
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.addObjectCreate("PartnerSpecificConfigurations", "org.nexuse2e.backend.pipelets.helper" +
                ".PartnerSpecificConfigurations");
        digester.addSetProperties("PartnerSpecificConfigurations");
        digester.addObjectCreate("PartnerSpecificConfigurations/PartnerSpecificConfiguration",
                "org.nexuse2e.backend" + ".pipelets.helper.PartnerSpecificConfiguration");
        digester.addSetProperties("PartnerSpecificConfigurations/PartnerSpecificConfiguration");
        digester.addSetNext("PartnerSpecificConfigurations/PartnerSpecificConfiguration",
                "addPartnerSpecificConfiguration", "org.nexuse2e.backend.pipelets.helper.PartnerSpecificConfiguration");

        try {
            partnerSpecificConfigurations = (PartnerSpecificConfigurations) digester.parse(configFileName);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return partnerSpecificConfigurations;
    }

    /**
     * @param configFileName
     * @return
     */
    private MappingDefinitions readConfiguration(String configFileName) {

        MappingDefinitions mappingDefinitions = null;
        Digester digester = new Digester();
        digester.setValidating(false);
        digester.addObjectCreate("MappingDefinitions", "org.nexuse2e.tools.mapping.xmldata.MappingDefinitions");
        digester.addSetProperties("MappingDefinitions");
        digester.addObjectCreate("MappingDefinitions/MappingDefinition", "org.nexuse2e.tools.mapping.xmldata" +
                ".MappingDefinition");
        digester.addSetProperties("MappingDefinitions/MappingDefinition");
        digester.addSetNext("MappingDefinitions/MappingDefinition", "addMappingDefinition", "org.nexuse2e.tools" +
                ".mapping.xmldata.MappingDefinition");

        try {
            mappingDefinitions = (MappingDefinitions) digester.parse(configFileName);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return mappingDefinitions;
    }

    /* (non-Javadoc)
     * @see org.nexuse2e.messaging.AbstractPipelet#teardown()
     */
    @Override
    public void teardown() {

        super.teardown();
    }

    public MappingDefinitions getMappingDefinitions() {

        return mappingDefinitions;
    }

    public void setMappingDefinitions(MappingDefinitions mappingDefinitions) {

        this.mappingDefinitions = mappingDefinitions;
    }
} // IntermediateDataMappingPipelet
