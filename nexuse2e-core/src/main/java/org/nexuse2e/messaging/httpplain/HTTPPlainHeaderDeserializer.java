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
package org.nexuse2e.messaging.httpplain;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nexuse2e.Engine;
import org.nexuse2e.NexusException;
import org.nexuse2e.messaging.AbstractPipelet;
import org.nexuse2e.messaging.MessageContext;
import org.nexuse2e.pojo.MessagePojo;

/**
 * @author mbreilmann
 *
 */
public class HTTPPlainHeaderDeserializer extends AbstractPipelet {

    private static Logger LOG = LogManager.getLogger(HTTPPlainHeaderDeserializer.class);

    /**
     * Default constructor.
     */
    public HTTPPlainHeaderDeserializer() {

        frontendPipelet = true;
    }

    /* (non-Javadoc)
     * @see org.nexuse2e.messaging.MessageUnpackager#processMessage(com.tamgroup.nexus.e2e.persistence.pojo
     * .MessagePojo, byte[])
     */
    public MessageContext processMessage(MessageContext messageContext) throws IllegalArgumentException,
            IllegalStateException, NexusException {

        Object object = messageContext.getData();
        if (!(object instanceof byte[])) {
            throw new IllegalArgumentException("Unable to process message: raw data not of type byte[]!");
        }

        // required params from post.
        String choreographyId =
                messageContext.getMessagePojo().getCustomParameters().get(Constants.PARAMETER_PREFIX_HTTP_REQUEST_PARAM + Constants.PARAM_CHOREOGRAPY_ID);
        String participantId =
                messageContext.getMessagePojo().getCustomParameters().get(Constants.PARAMETER_PREFIX_HTTP_REQUEST_PARAM + Constants.PARAM_PARTNER_ID);
        String actionId =
                messageContext.getMessagePojo().getCustomParameters().get(Constants.PARAMETER_PREFIX_HTTP_REQUEST_PARAM + Constants.PARAM_ACTION_ID);

        // optional params, if they don't exist, new ones will be generated.
        String conversationId =
                messageContext.getMessagePojo().getCustomParameters().get(Constants.PARAMETER_PREFIX_HTTP_REQUEST_PARAM + Constants.PARAM_CONVERSATION_ID);
        String messageId =
                messageContext.getMessagePojo().getCustomParameters().get(Constants.PARAMETER_PREFIX_HTTP_REQUEST_PARAM + Constants.PARAM_MESSAGE_ID);

        // Verify params, if required one does not exist, reject post.
        if (choreographyId == null || actionId == null || participantId == null) {
            LOG.error("Received post without required parameters.");
            throw new NexusException("Received post without required parameters."); //invalid post, Action,
            // choreography, and participantid parameters are required.
        }

        // recycle conversation ID from message context if present
        if (conversationId == null && messageContext.getConversation() != null && messageContext.getConversation().getConversationId() != null) {
            conversationId = messageContext.getConversation().getConversationId();
        }

        if (conversationId == null) {
            conversationId =
                    Engine.getInstance().getIdGenerator(org.nexuse2e.Constants.ID_GENERATOR_CONVERSATION).getId();
        }

        if (messageId == null) {
            messageId = Engine.getInstance().getIdGenerator(org.nexuse2e.Constants.ID_GENERATOR_MESSAGE).getId();
        }

        MessagePojo messagePojo = messageContext.getMessagePojo();

        messagePojo.setOutbound(false);
        messagePojo.setType(org.nexuse2e.messaging.Constants.INT_MESSAGE_TYPE_NORMAL);
        messagePojo = Engine.getInstance().getTransactionService().initializeMessage(messagePojo, messageId,
                conversationId, actionId, participantId, choreographyId);


        return messageContext;
    } // processMessage

} // HTTPPlainHeaderDeserializer
