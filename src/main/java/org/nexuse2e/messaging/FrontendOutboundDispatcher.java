/**
 * NEXUSe2e Business Messaging Open Source  
 * Copyright 2007, Tamgroup and X-ioma GmbH   
 *  
 * This is free software; you can redistribute it and/or modify it  
 * under the terms of the GNU Lesser General Public License as  
 * published by the Free Software Foundation version 2.1 of  
 * the License.  
 *  
 * This software is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU  
 * Lesser General Public License for more details.  
 *  
 * You should have received a copy of the GNU Lesser General Public  
 * License along with this software; if not, write to the Free  
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.nexuse2e.messaging;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.nexuse2e.Engine;
import org.nexuse2e.NexusException;
import org.nexuse2e.ProtocolSpecificKey;
import org.nexuse2e.Constants.BeanStatus;
import org.nexuse2e.Constants.Layer;
import org.nexuse2e.dao.TransactionDAO;
import org.nexuse2e.logging.LogMessage;
import org.nexuse2e.pojo.ConversationPojo;
import org.nexuse2e.pojo.MessagePojo;
import org.nexuse2e.pojo.ParticipantPojo;
import org.springframework.beans.factory.InitializingBean;

/**
 * Component dispatching outbound messages to the correct pipeline based on their messaging protocol (TRP).
 * This component also implements re-sending of messages to support reliable message if the underlying
 * protocol supports it.
 *
 * @author mbreilmann
 */
public class FrontendOutboundDispatcher extends AbstractPipelet implements InitializingBean {

    private static Logger      LOG    = Logger.getLogger( FrontendOutboundDispatcher.class );
    private FrontendPipeline[] frontendOutboundPipelines;
    private BeanStatus         status = BeanStatus.UNDEFINED;

    /* (non-Javadoc)
     * @see org.nexuse2e.messaging.Pipelet#processMessage(org.nexuse2e.messaging.MessageContext)
     */
    public MessageContext processMessage( MessageContext messageContext ) throws NexusException {

        int interval = Constants.DEFAULT_MESSAGE_INTERVAL;

        FrontendPipeline pipeline = getProtocolSpecificPipeline( messageContext.getProtocolSpecificKey() );
        if ( pipeline == null ) {
            throw new NexusException( "No valid pipeline found for " + messageContext.getProtocolSpecificKey() );
        }

        int retries = messageContext.getParticipant().getConnection().getRetries();
        boolean reliable = messageContext.getParticipant().getConnection().isReliable();

        ParticipantPojo participantPojo = messageContext.getMessagePojo().getParticipant();
        if ( participantPojo != null ) {
            interval = participantPojo.getConnection().getMessageInterval();
        }

        String msgType = null;
        switch ( messageContext.getMessagePojo().getType() ) {
            case Constants.INT_MESSAGE_TYPE_ACK:
                msgType = "ack";
                break;
            case Constants.INT_MESSAGE_TYPE_ERROR:
                msgType = "error";
                break;
            default:
                msgType = "normal";
        }

        LOG.info( new LogMessage( "Sending " + msgType + " message (" + messageContext.getMessagePojo().getMessageId()
                + ") to " + participantPojo.getPartner().getPartnerId() + " for "
                + messageContext.getChoreography().getName() + "/"
                + messageContext.getMessagePojo().getAction().getName(), messageContext.getMessagePojo() ) );

        final Runnable messageSender = new MessageSender( pipeline, messageContext, ( reliable ? retries : 0 ) );
        if ( messageContext.getMessagePojo().getType() == Constants.INT_MESSAGE_TYPE_NORMAL ) {
            if ( !Engine.getInstance().getTransactionService().isProcessingMessage(
                    messageContext.getMessagePojo().getMessageId() ) ) {
                ScheduledExecutorService scheduler = Executors.newScheduledThreadPool( 1 );

                ScheduledFuture<?> handle = scheduler
                        .scheduleAtFixedRate( messageSender, 0, interval, TimeUnit.SECONDS );
                LOG.debug( new LogMessage( "Waiting " + interval + " seconds until message resend...", messageContext
                        .getMessagePojo() ) );
                Engine.getInstance().getTransactionService().registerProcessingMessage(
                        messageContext.getMessagePojo().getMessageId(), handle, scheduler );
            } else {
                LOG.warn( "Message is already being processed: " + messageContext.getMessagePojo().getMessageId() );
            }
        } else {
            new Thread( messageSender, messageContext.getMessagePojo().getMessageId() ).start();
        }

        return messageContext;
    } // processMessage

    /**
     * @param protocolSpecificKey
     * @return
     */
    private FrontendPipeline getProtocolSpecificPipeline( ProtocolSpecificKey protocolSpecificKey ) {

        for ( int i = 0; i < frontendOutboundPipelines.length; i++ ) {
            LOG.debug( "comparing keys:" + protocolSpecificKey + " - " + frontendOutboundPipelines[i].getKey() );
            if ( frontendOutboundPipelines[i].getKey().equals( protocolSpecificKey ) ) {
                return frontendOutboundPipelines[i];
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {

        if ( frontendOutboundPipelines == null || frontendOutboundPipelines.length == 0 ) {
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
    public void setFrontendOutboundPipelines( FrontendPipeline[] frontendOutboundPipelines ) {

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

        initialize( Engine.getInstance().getCurrentConfiguration() );

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

        // TODO Auto-generated method stub
        return false;
    }

    /**
     * @author mbreilmann
     *
     */
    private class MessageSender implements Runnable {

        MessageContext   messageContext = null;
        FrontendPipeline pipeline       = null;
        int              retries        = 0;
        int              retryCount     = 0;

        /**
         * @param pipeline
         * @param messageContext
         * @param retries
         */
        MessageSender( FrontendPipeline pipeline, MessageContext messageContext, int retries ) {

            this.messageContext = messageContext;
            this.pipeline = pipeline;
            this.retries = retries;
            LOG.debug( "Retries: " + retries );
        }

        public void run() {

            MessagePojo messagePojo = messageContext.getMessagePojo();
            ;
            ConversationPojo conversationPojo = messagePojo.getConversation();

            LOG.trace( "Message ( " + messagePojo.getMessageId() + " ) end timestamp: " + messagePojo.getEndDate() );

            if ( retryCount <= retries ) {
                LOG.debug( new LogMessage( "Sending message...", messagePojo ) );
                try {
                    retryCount++;
                    synchronized ( conversationPojo ) {
                        if ( ( messagePojo.getType() != Constants.INT_MESSAGE_TYPE_NORMAL )
                                && ( conversationPojo.getStatus() == org.nexuse2e.Constants.CONVERSATION_STATUS_PROCESSING ) ) {
                            conversationPojo.setStatus( org.nexuse2e.Constants.CONVERSATION_STATUS_SENDING_ACK );
                        } else if ( ( messagePojo.getType() == Constants.INT_MESSAGE_TYPE_NORMAL )
                                && ( messagePojo.getEndDate() != null ) ) {

                            // If message has been ack'ed while we were waiting do nothing
                            LOG.info( "Cancelled sending message (ack was just received): "
                                    + messagePojo.getMessageId() );
                            cancelRetrying( false );
                            return;
                        }
                        
                        // quick fix: remember original conversation and message states
                        int conversationStatus = conversationPojo.getStatus();
                        int messageStatus = messagePojo.getStatus();
                        MessagePojo currentMessage = Engine.getInstance().getTransactionService().getMessage(
                                messagePojo.getMessageId() );
                        if (currentMessage != null) {
                            messageStatus = currentMessage.getStatus();
                            if (currentMessage.getConversation() != null) {
                                conversationStatus = currentMessage.getConversation().getStatus();
                            }
                            TransactionDAO transactionDAO = (TransactionDAO) Engine.getInstance().getDao( "transactionDao" );
                            transactionDAO.reattachRecord( messagePojo );
                            transactionDAO.reattachRecord( conversationPojo );
                        }

                        // Send message
                        messageContext.getMessagePojo().setRetries( retryCount - 1 );
                        messageContext = pipeline.processMessage( messageContext );
                        
                        // quick fix: reload conversation and message states to check if
                        // persistent objects have changed
                        currentMessage = Engine.getInstance().getTransactionService().getMessage(
                                messagePojo.getMessageId() );
                        if (currentMessage != null) {
                            if (currentMessage.getStatus() != messageStatus) {
                                messagePojo.setStatus( currentMessage.getStatus() );
                            }
                            if (currentMessage.getConversation() != null
                                    && currentMessage.getConversation().getStatus() != conversationStatus) {
                                conversationPojo.setStatus( currentMessage.getConversation().getStatus() );
                            }
                            TransactionDAO transactionDAO = (TransactionDAO) Engine.getInstance().getDao( "transactionDao" );
                            transactionDAO.reattachRecord( messagePojo );
                            transactionDAO.reattachRecord( conversationPojo );
                        }

                        if ( messagePojo.getType() == Constants.INT_MESSAGE_TYPE_NORMAL ) {
                            if ( ( conversationPojo.getStatus() == org.nexuse2e.Constants.CONVERSATION_STATUS_PROCESSING )
                                    || ( conversationPojo.getStatus() == org.nexuse2e.Constants.CONVERSATION_STATUS_AWAITING_ACK ) ) {
                                if ( messageContext.getParticipant().getConnection().isReliable() ) {
                                    conversationPojo
                                            .setStatus( org.nexuse2e.Constants.CONVERSATION_STATUS_AWAITING_ACK );
                                } else {
                                    Engine.getInstance().getTransactionService().deregisterProcessingMessage(
                                            messagePojo.getMessageId() );
                                    messagePojo.setStatus( org.nexuse2e.Constants.MESSAGE_STATUS_SENT );
                                    messagePojo.setModifiedDate( new Date() );
                                    if ( conversationPojo.getCurrentAction().isEnd() ) {
                                        conversationPojo
                                                .setStatus( org.nexuse2e.Constants.CONVERSATION_STATUS_COMPLETED );
                                    } else {
                                        conversationPojo.setStatus( org.nexuse2e.Constants.CONVERSATION_STATUS_IDLE );
                                    }
                                }
                            } else {
                                LOG.error( new LogMessage(
                                        "Unexpected conversation state after sending normal message: "
                                                + conversationPojo.getStatus(), messagePojo ) );
                            }
                        } else {
                            // Engine.getInstance().getTransactionService().deregisterProcessingMessage( messagePojo.getMessageId() );
                            messagePojo.setStatus( org.nexuse2e.Constants.MESSAGE_STATUS_SENT );
                            Date endDate = new Date();
                            messagePojo.setModifiedDate( endDate );
                            messagePojo.setEndDate( endDate );
                            messagePojo.getReferencedMessage().setEndDate( endDate );
                            if ( conversationPojo.getStatus() == org.nexuse2e.Constants.CONVERSATION_STATUS_SENDING_ACK ) {
                                conversationPojo
                                        .setStatus( org.nexuse2e.Constants.CONVERSATION_STATUS_ACK_SENT_AWAITING_BACKEND );
                            } else if ( ( conversationPojo.getStatus() == org.nexuse2e.Constants.CONVERSATION_STATUS_BACKEND_SENT_SENDING_ACK )
                                    || ( conversationPojo.getStatus() == org.nexuse2e.Constants.CONVERSATION_STATUS_IDLE )
                                    || ( conversationPojo.getStatus() == org.nexuse2e.Constants.CONVERSATION_STATUS_ACK_SENT_AWAITING_BACKEND ) ) {
                                if ( conversationPojo.getCurrentAction().isEnd() ) {
                                    conversationPojo.setStatus( org.nexuse2e.Constants.CONVERSATION_STATUS_COMPLETED );
                                } else {
                                    conversationPojo.setStatus( org.nexuse2e.Constants.CONVERSATION_STATUS_IDLE );
                                }
                            } else if ( conversationPojo.getStatus() == org.nexuse2e.Constants.CONVERSATION_STATUS_AWAITING_BACKEND ) {
                                LOG.debug( new LogMessage(
                                        "Received ack message, backend still processing - conversation ID: "
                                                + conversationPojo.getConversationId(), messagePojo ) );
                            } else if ( ( conversationPojo.getStatus() != org.nexuse2e.Constants.CONVERSATION_STATUS_COMPLETED )
                                    && ( conversationPojo.getStatus() != org.nexuse2e.Constants.CONVERSATION_STATUS_ERROR ) ) {
                                LOG.error( new LogMessage( "Unexpected conversation state after sending ack message: "
                                        + conversationPojo.getStatus(), messagePojo ) );
                            }
                        }

                        // Persist status changes
                        Engine.getInstance().getTransactionService().updateTransaction( conversationPojo );
                    } // synchronized

                    LOG.debug( new LogMessage( "Message sent.", messagePojo ) );
                } catch ( Throwable e ) {
                    // Persist retry count changes
                    try {
                        Engine.getInstance().getTransactionService().updateMessage( messagePojo );
                    } catch ( NexusException e1 ) {
                        LOG.error( new LogMessage( "Error saving message: " + e1, messagePojo ) );
                    }

                    /*
                    if ( LOG.isTraceEnabled() ) {
                        e.printStackTrace();
                    }
                    */

                    LOG.error( new LogMessage( "Error sending message: " + e, messagePojo ), e );
                }
            } else {
                if ( messagePojo.getType() == Constants.INT_MESSAGE_TYPE_NORMAL ) {
                    LOG.error( new LogMessage(
                            "Maximum number of retries reached without receiving acknowledgment - choreography: "
                                    + messagePojo.getConversation().getChoreography().getName(), messagePojo ) );
                } else {
                    LOG.debug( new LogMessage( "Max number of retries reached!", messagePojo ) );
                }
                cancelRetrying();
            }
        } // run

        /**
         * Stop the thread for resending the message based on its reliability parameters
         */
        private void cancelRetrying() {

            cancelRetrying( true );
        }

        /**
         * Stop the thread for resending the message based on its reliability parameters. If updateStatus is true also
         * set the message to failed and the conversation to error.
         */
        private void cancelRetrying( boolean updateStatus ) {

            MessagePojo messagePojo = messageContext.getMessagePojo();
            synchronized ( messagePojo.getConversation() ) {

                // if ( messagePojo.getStatus() != org.nexuse2e.Constants.MESSAGE_STATUS_SENT ) {
                if ( updateStatus ) {
                    messagePojo.setStatus( org.nexuse2e.Constants.MESSAGE_STATUS_FAILED );
                    messagePojo.getConversation().setStatus( org.nexuse2e.Constants.CONVERSATION_STATUS_ERROR );
                    try {
                        Engine.getInstance().getTransactionService().updateTransaction( messagePojo.getConversation() );
                    } catch ( NexusException e ) {
                        LOG.error( new LogMessage( "Error updating conversation/message on failed atempt message!",
                                messagePojo ) );
                        e.printStackTrace();
                    }
                }
                // }
                Engine.getInstance().getTransactionService().deregisterProcessingMessage(
                        messageContext.getMessagePojo().getMessageId() );
            } // synchronized
        }
    } // inner class MessageSender

} // FrontendOutboundDispatcher
