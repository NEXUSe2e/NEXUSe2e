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
package org.nexuse2e.messaging;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nexuse2e.BeanStatus;
import org.nexuse2e.Engine;
import org.nexuse2e.Layer;
import org.nexuse2e.NexusException;
import org.nexuse2e.controller.StateTransitionException;
import org.nexuse2e.logging.LogMessage;
import org.nexuse2e.pojo.MessagePojo;
import org.nexuse2e.pojo.ParticipantPojo;
import org.nexuse2e.util.NexusThreadStorage;
import org.springframework.beans.factory.InitializingBean;

/**
 * Component dispatching outbound messages to the correct pipeline based on their messaging protocol (TRP).
 * This component also implements re-sending of messages to support reliable message if the underlying
 * protocol supports it.
 *
 * @author mbreilmann
 */
public class FrontendOutboundDispatcher extends AbstractPipelet implements InitializingBean {

    private static Logger LOG = LogManager.getLogger(FrontendOutboundDispatcher.class);
    private FrontendPipeline[] frontendOutboundPipelines;
    private BeanStatus status = BeanStatus.UNDEFINED;

    /* (non-Javadoc)
     * @see org.nexuse2e.messaging.Pipelet#processMessage(org.nexuse2e.messaging.MessageContext)
     */
    public MessageContext processMessage(MessageContext messageContext) throws NexusException {

        // Set some thread-local information so everyone in the pipeline can always access it
        NexusThreadStorage.set("conversationId", messageContext.getConversation().getConversationId());
        NexusThreadStorage.set("messageId", messageContext.getMessagePojo().getMessageId());

        // for  participants with "hold" connections, do not go on with processing
        if (messageContext.getParticipant().getConnection().isHold()) {
            return null;
        }

        FrontendPipeline pipeline = getProtocolSpecificPipeline(messageContext);
        if (pipeline == null) {
            throw new NexusException("No valid pipeline found for " + messageContext.getProtocolSpecificKey());
        }

        int retries = messageContext.getParticipant().getConnection().getRetries();

        ParticipantPojo participantPojo = messageContext.getMessagePojo().getParticipant();

        if (messageContext.isFirstTimeInQueue() || messageContext.getMessagePojo().getRetries() < retries) {
            LOG.info(new LogMessage("Sending " + messageContext.getMessagePojo().getTypeName() + " message (" + messageContext.getMessagePojo().getMessageId() + ") to " + participantPojo.getPartner().getPartnerId() + " for " + messageContext.getChoreography().getName() + "/" + messageContext.getMessagePojo().getAction().getName(), messageContext.getMessagePojo()));
        }

        // Request an conversation lock in order to not disallow parallel i/o, and processing activity in this
        // conversation.
        // If everything goes alright, the lock will be released in the message sender thread after the message was
        // sent,
        // or the sending failed. In case of error while the current thread is processing, the lock will be released
        // as well.

        // do it
        sendMessage(pipeline, messageContext, retries);

        // ThreadLocal-objects need to be manually removed
        NexusThreadStorage.remove("conversationId");
        NexusThreadStorage.remove("messageId");

        return messageContext;
    } // processMessage

    /**
     * @param messageContext
     * @return
     */
    private FrontendPipeline getProtocolSpecificPipeline(MessageContext messageContext) {
        if (frontendOutboundPipelines != null) {
            for (int i = 0; i < frontendOutboundPipelines.length; i++) {
                LOG.debug(new LogMessage("comparing keys:" + messageContext.getProtocolSpecificKey() + " - " + frontendOutboundPipelines[i].getKey(), messageContext.getMessagePojo()));
                if (frontendOutboundPipelines[i].getKey().equals(messageContext.getProtocolSpecificKey())) {
                    return frontendOutboundPipelines[i];
                }
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {

        if (frontendOutboundPipelines == null || frontendOutboundPipelines.length == 0) {
            status = BeanStatus.ERROR;
        }
        status = BeanStatus.INSTANTIATED;
    }

    /**
     * @return
     */
    public FrontendPipeline[] getFrontendOutboundPipelines() {

        return frontendOutboundPipelines;
    }

    /**
     * @param frontendOutboundPipelines
     */
    public void setFrontendOutboundPipelines(FrontendPipeline[] frontendOutboundPipelines) {

        this.frontendOutboundPipelines = frontendOutboundPipelines;
    }

    /* (non-Javadoc)
     * @see org.nexuse2e.Manageable#getStatus()
     */
    public BeanStatus getStatus() {

        return status;
    }

    /* (non-Javadoc)
     * @see org.nexuse2e.Manageable#initialize()
     */
    public void initialize() throws InstantiationException {

        initialize(Engine.getInstance().getCurrentConfiguration());

    }

    /* (non-Javadoc)
     * @see org.nexuse2e.Manageable#getRunLevel()
     */
    public Layer getActivationLayer() {

        return Layer.OUTBOUND_PIPELINES;
    }

    /* (non-Javadoc)
     * @see org.nexuse2e.Manageable#validate()
     */
    public boolean validate() {

        return false;
    }

    public void sendMessage(FrontendPipeline pipeline, MessageContext messageContext, int retries) {

        MessagePojo messagePojo = messageContext.getMessagePojo();

        LOG.trace(new LogMessage("Message ( " + messagePojo.getMessageId() + " ) end timestamp: " + messagePojo.getEndDate(), messagePojo));

        if (messageContext.isFirstTimeInQueue() || messageContext.getMessagePojo().getRetries() < retries) {
            LOG.debug(new LogMessage("Sending message...", messagePojo));

            MessageContext returnedMessageContext = null;

            try {
                if (messagePojo.isNormal() && messagePojo.getEndDate() != null) {
                    // If message has been ack'ed while we were waiting do nothing
                    LOG.info(new LogMessage("Cancelled sending message (ack was just received): " + messagePojo.getMessageId(), messagePojo));
                    cancelRetrying(messageContext, false);
                    return;
                }
                // Send message
                // increment retry count AFTER message update because the retry hasn't yet been performed
                messagePojo.setRetries(messageContext.isFirstTimeInQueue() ? 0 : messagePojo.getRetries() + 1);
                messageContext.setFirstTimeInQueue(false);

                messageContext.getStateMachine().sendingMessage();
                returnedMessageContext = pipeline.processMessage(messageContext);
                messageContext.getStateMachine().sentMessage();

                // if this message is not reliable, requeueing must be stopped after processing the frontend pipeline
                // successfully.
                if (messageContext.getParticipant() != null && messageContext.getParticipant().getConnection() != null && !messageContext.getParticipant().getConnection().isReliable()) {
                    cancelRetrying(messageContext, false);
                }

                if (!messagePojo.isAck()) {
                    Engine.getInstance().getTransactionService().updateRetryCount(messagePojo);
                }

                LOG.debug(new LogMessage("Message sent.", messagePojo));
            } catch (Throwable e) {
                LOG.error(new LogMessage("Error sending message", messagePojo, e), e);

                try {
                    if (messagePojo.isAck()) {
                        messageContext.getStateMachine().sentMessage(); // mark ack message as "sent" - normal
                        // message will be re-sent by partner
                    } else {
                        // Persist retry count changes
                        Engine.getInstance().getTransactionService().updateRetryCount(messagePojo);
                        if (retries == 0 || messagePojo.getRetries() >= retries) {
                            handleErrorState(messageContext, messagePojo);
                        }
                    }
                } catch (Exception e1) {
                    LOG.error(new LogMessage("Error saving message", messagePojo, e1), e1);
                }
            }

            if ((returnedMessageContext != null) && !returnedMessageContext.equals(messageContext)) {
                try {
                    Engine.getInstance().getCurrentConfiguration().getStaticBeanContainer().getFrontendInboundDispatcher().processMessage(returnedMessageContext);
                } catch (NexusException e) {
                    LOG.error(new LogMessage("Error processing synchronous reply", messagePojo, e), e);
                }
            }

        } else {
            handleErrorState(messageContext, messagePojo);
        }
    } // run

    private void handleErrorState(MessageContext messageContext, MessagePojo messagePojo) {
        if (messagePojo.getType() == Constants.INT_MESSAGE_TYPE_NORMAL) {

            if (Engine.getInstance().getAdvancedRetryLogging() && StringUtils.isNotBlank(Engine.getInstance().getRetryLoggingTemplate())) {

                try {
                    String choreographyName = messagePojo.getConversation().getChoreography().getName();
                    String partnerId = messagePojo.getConversation().getPartner().getPartnerId();
                    String fileName = messageContext.getMessagePojo().getMessagePayloads().get(0).getContentId();


                    String message = Engine.getInstance().getRetryLoggingTemplate();
                    message = message.replace("{filename}", fileName);
                    message = message.replace("{partnerId}", partnerId);
                    message = message.replace("{choreographyId}", choreographyName);
                    message = message.replace("{actionId}", messagePojo.getAction().getName());
                    message = message.replace("{connectionUrl}", messagePojo.getParticipant().getConnection().getUri());
                    message = message.replace("{retries}", "" + messagePojo.getRetries());
                    message = message.replace("{messageStatus}", messagePojo.getStatusName());
                    message = message.replace("{messageId}", messagePojo.getMessageId());
                    message = message.replace("{conversationId}", messagePojo.getConversation().getConversationId());

                    LOG.error(new LogMessage(message, messagePojo));


                } catch (Exception e) {
                    LOG.error(new LogMessage("(Templating failed) - Maximum number of retries reached without " +
                            "receiving acknowledgment - choreography: " + messagePojo.getConversation().getChoreography().getName() + ", partner: " + messagePojo.getConversation().getPartner().getPartnerId(), messagePojo));

                }

            } else {

                LOG.error(new LogMessage("Maximum number of retries reached without receiving acknowledgment - " +
                        "choreography: " + messagePojo.getConversation().getChoreography().getName() + ", partner: " + messagePojo.getConversation().getPartner().getPartnerId(), messagePojo));
            }
        } else {
            LOG.debug(new LogMessage("Max number of retries reached!", messagePojo));
        }
        cancelRetrying(messageContext);
    }

    /**
     * Stop the thread for resending the message based on its reliability parameters
     */
    private void cancelRetrying(MessageContext messageContext) {

        cancelRetrying(messageContext, true);
    }

    /**
     * Stop the thread for resending the message based on its reliability parameters. If updateStatus is true also
     * set the message to failed and the conversation to error.
     * @param messageContext the message context.
     * @param updateStatus update the message status?
     */
    private void cancelRetrying(MessageContext messageContext, boolean updateStatus) {

        MessagePojo messagePojo = messageContext.getMessagePojo();
        synchronized (messagePojo.getConversation()) {

            if (updateStatus) {
                try {
                    messageContext.getStateMachine().processingFailed();
                } catch (StateTransitionException stex) {
                    LOG.warn(stex.getMessage());
                } catch (NexusException e) {
                    LOG.error(new LogMessage("Error while setting conversation status to ERROR", messagePojo), e);
                }
            }
            Engine.getInstance().getTransactionService().deregisterProcessingMessage(messageContext.getMessagePojo().getMessageId());
        } // synchronized
    }

} // FrontendOutboundDispatcher
