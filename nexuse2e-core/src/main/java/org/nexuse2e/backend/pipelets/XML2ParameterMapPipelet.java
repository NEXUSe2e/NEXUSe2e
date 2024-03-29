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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nexuse2e.NexusException;
import org.nexuse2e.backend.pipelets.helper.RequestResponseData;
import org.nexuse2e.configuration.EngineConfiguration;
import org.nexuse2e.configuration.ParameterDescriptor;
import org.nexuse2e.configuration.ParameterType;
import org.nexuse2e.logging.LogMessage;
import org.nexuse2e.messaging.AbstractPipelet;
import org.nexuse2e.messaging.MessageContext;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * @author mbreilmann
 */
public class XML2ParameterMapPipelet extends AbstractPipelet {

    public static final String USE_DATA_FIELD = "useDataField";
    public static final String PATH_SEPARATOR = "pathSeparator";
    private static Logger LOG = LogManager.getLogger(XML2ParameterMapPipelet.class);
    private boolean useDataField = true;

    private String pathSeparator = "/";

    public XML2ParameterMapPipelet() {

        parameterMap.put(USE_DATA_FIELD, new ParameterDescriptor(ParameterType.BOOLEAN, "Use Data Field",
                "Use the " + "MessageContext Data Field to retrieve XML.", Boolean.TRUE));
        parameterMap.put(PATH_SEPARATOR, new ParameterDescriptor(ParameterType.STRING, "Path Separator",
                "Separator " + "character between element names.", "/"));
    }

    public static void main(String args[]) {

        if (args.length != 1) {
            System.err.println("Wrong number of parameters. Usage: XML2ParameterMapPipelet <xml file>");
            return;
        }
        try {
            InputSource xmlSource = new InputSource(new FileInputStream(args[0]));

            Map<String, String> map = new XML2ParameterMapPipelet().flattenXML(xmlSource);
            for (String key : map.keySet()) {
                System.out.println(key + " - " + map.get(key));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see org.nexuse2e.messaging.AbstractPipelet#initialize(org.nexuse2e.configuration.EngineConfiguration)
     */
    @Override
    public void initialize(EngineConfiguration config) throws InstantiationException {

        Boolean useDataFieldValue = getParameter(USE_DATA_FIELD);
        if (useDataFieldValue != null) {
            useDataField = useDataFieldValue.booleanValue();
        }

        String pathSeparatorValue = getParameter(PATH_SEPARATOR);
        if (pathSeparatorValue != null) {
            pathSeparator = pathSeparatorValue.trim();
        }
        LOG.trace("useDataField  : " + useDataField);
        LOG.trace("pathSeparator : " + pathSeparator);

        super.initialize(config);
    }

    /* (non-Javadoc)
     * @see org.nexuse2e.messaging.AbstractPipelet#processMessage(org.nexuse2e.messaging.MessageContext)
     */
    @Override
    public MessageContext processMessage(MessageContext messageContext) throws IllegalArgumentException,
            IllegalStateException, NexusException {

        RequestResponseData requestResponseData = null;

        if ((messageContext.getData() == null) || !(messageContext.getData() instanceof RequestResponseData)) {
            LOG.error(new LogMessage("Wrong class detected in data field, found " + messageContext.getData().getClass(), messageContext.getMessagePojo()));
            throw new NexusException("Wrong class detected in data field, found " + messageContext.getData().getClass());
        }
        requestResponseData = (RequestResponseData) messageContext.getData();

        InputSource inputSource = new InputSource(new StringReader(requestResponseData.getResponseString()));

        Map<String, String> map = flattenXML(inputSource);

        requestResponseData.setParameters(map);

        return messageContext;
    }

    private Map<String, String> flattenXML(InputSource xmlSource) throws NexusException {

        Map<String, String> map = new HashMap<String, String>();

        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        try {
            SAXParser saxParser = saxParserFactory.newSAXParser();

            DefaultHandler genericHandler = new GenericHandler(map, pathSeparator);

            saxParser.parse(xmlSource, genericHandler);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();

            throw new NexusException(e);
        } catch (SAXException e) {
            e.printStackTrace();
            throw new NexusException(e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new NexusException(e);
        }

        if (LOG.isTraceEnabled()) {
            for (String key : map.keySet()) {
                LOG.trace(key + " - " + map.get(key));
            }
        }

        return map;
    }

    /* (non-Javadoc)
     * @see org.nexuse2e.messaging.AbstractPipelet#teardown()
     */
    @Override
    public void teardown() {

        super.teardown();
    }

    private class GenericHandler extends DefaultHandler {

        private Map<String, String> map = null;
        private Stack<String> stack = new Stack<String>();
        private StringBuffer value = new StringBuffer();
        private String pathSeparator = "/";

        protected GenericHandler(Map<String, String> map, String pathSeparator) {

            this.map = map;
            this.pathSeparator = pathSeparator;
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

            // LOG.trace( "startElement: '" + uri + "' - '" + localName + "' - '" + qName + "'" );
            stack.push(qName);
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {

            // LOG.trace( "endElement: '" + localName + "'" );
            StringBuffer path = new StringBuffer();

            for (String element : stack) {
                path.append(pathSeparator + element);
            }
            // LOG.trace( "path: " + path );

            String tempValue = value.toString();

            if (tempValue.length() != 0) {
                map.put(path.toString(), tempValue);
            }

            stack.pop();

            value = new StringBuffer();
        }

        public void characters(char[] ch, int start, int length) throws SAXException {

            value.append(new String(ch, start, length).trim());

            // LOG.trace( "String: " + value );
        }

    }
} // XML2ParameterMapPipelet
