/**
 *  NEXUSe2e Business Messaging Open Source
 *  Copyright 2000-2021, direkt gruppe GmbH
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation version 3 of
 *  the License.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.nexuse2e.backend.pipelets;

import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.nexuse2e.NexusException;
import org.nexuse2e.backend.pipelets.helper.RequestResponseData;
import org.nexuse2e.backend.pipelets.helper.ResponseSender;
import org.nexuse2e.configuration.EngineConfiguration;
import org.nexuse2e.configuration.ParameterDescriptor;
import org.nexuse2e.configuration.ParameterType;
import org.nexuse2e.logging.LogMessage;
import org.nexuse2e.messaging.MessageContext;
import org.nexuse2e.pojo.MessagePayloadPojo;

/**
 * @author mbreilmann
 *
 */
public class HTTPResponseIntegrationPipelet extends HTTPIntegrationPipelet {

    private static Logger      LOG                             = Logger.getLogger( HTTPIntegrationPipelet.class );

    public static final String ACTION_PARAM_NAME               = "action";
    public static final String DELAY_PARAM_NAME                = "delay";
    public static final String USE_ORIGINAL_MESSAGE_PARAM_NAME = "useOriginalMessage";

    private int                delay                           = 1000;
    private String             action                          = null;
    private boolean            useOriginalMessage              = true;

    public HTTPResponseIntegrationPipelet() {

        super();
        parameterMap.put( ACTION_PARAM_NAME, new ParameterDescriptor( ParameterType.STRING, "Action",
                "Action to trigger for outbound message.", "" ) );
        parameterMap.put( DELAY_PARAM_NAME, new ParameterDescriptor( ParameterType.STRING, "Delay",
                "Delay in milliseconds before outbound message is sent.", "1000" ) );
        parameterMap.put( USE_ORIGINAL_MESSAGE_PARAM_NAME, new ParameterDescriptor( ParameterType.BOOLEAN,
                "Use Original Message", "Use Original Message to retrieve XML.", Boolean.TRUE ) );
    }

    /* (non-Javadoc)
     * @see org.nexuse2e.messaging.AbstractPipelet#initialize(org.nexuse2e.configuration.EngineConfiguration)
     */
    @Override
    public void initialize( EngineConfiguration config ) throws InstantiationException {

        String actionValue = getParameter( ACTION_PARAM_NAME );
        if ( actionValue == null ) {
            LOG.error( "Parameter action has not been defined!" );
            return;
        } else {
            action = actionValue;
        }
        String delayString = getParameter( DELAY_PARAM_NAME );
        delay = 1000;
        if ( ( delayString != null ) && ( delayString.length() != 0 ) ) {
            delay = Integer.parseInt( delayString );
        }
        Boolean useOriginalMessageValue = getParameter( USE_ORIGINAL_MESSAGE_PARAM_NAME );
        if ( useOriginalMessageValue != null ) {
            useOriginalMessage = useOriginalMessageValue.booleanValue();
        }

        super.initialize( config );
    }

    /* (non-Javadoc)
     * @see org.nexuse2e.messaging.Pipelet#processMessage(org.nexuse2e.messaging.MessageContext)
     */
    public MessageContext processMessage( MessageContext messageContext ) throws IllegalArgumentException,
            IllegalStateException, NexusException {

        RequestResponseData requestResponseData = null;

        if ( action == null ) {
            LOG.error( "Parameter action has not been defined!" );
            return messageContext;
        }

        // Execute the HTTP call first
        messageContext = super.processMessage( messageContext );

        if ( !( messageContext.getData() instanceof RequestResponseData ) ) {
            LOG.error( "Wrong class detected in data field, found " + messageContext.getData().getClass() );
            throw new NexusException( "Wrong class detected in data field, found "
                    + messageContext.getData().getClass() );
        }
        requestResponseData = (RequestResponseData) messageContext.getData();

        if ( useOriginalMessage ) {

            List<MessagePayloadPojo> messagePayloadPojos = messageContext.getOriginalMessagePojo().getMessagePayloads();
            Iterator<MessagePayloadPojo> iterator = messagePayloadPojos.iterator();
            if ( iterator.hasNext() ) {
                MessagePayloadPojo payload = iterator.next();
                if ( ( payload != null ) && ( payload.getPayloadData() != null ) ) {
                    try {
						requestResponseData.setRequestString( new String( payload.getPayloadData(), messageContext.getEncoding() ) );
					} catch (UnsupportedEncodingException e) {
						throw new NexusException(new LogMessage("configured encoding '"+messageContext.getEncoding()+"' is not supported",messageContext),e);
					}
                }
            }
        }

        // Trigger new response message
        new Thread( new ResponseSender( messageContext.getChoreography().getName(), messageContext.getPartner()
                .getPartnerId(), messageContext.getConversation().getConversationId(), action, requestResponseData,
                delay ) ).start();

        LOG.debug( "Done!" );
        return messageContext;
    }
} // HTTPResponseIntegrationPipelet
