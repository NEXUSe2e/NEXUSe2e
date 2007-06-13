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

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.nexuse2e.Engine;
import org.nexuse2e.NexusException;
import org.nexuse2e.Constants.BeanStatus;
import org.nexuse2e.Constants.Runlevel;
import org.nexuse2e.configuration.EngineConfiguration;
import org.nexuse2e.pojo.ConversationPojo;
import org.nexuse2e.pojo.MessagePojo;

/**
 * Component in the NEXUSe2e backend that serializes the processing of outbound messages per Choreography. 
 * Messages are placed in a queue and are processed by a single thread in fifo order.
 *
 * @author mbreilmann
 */
public class BackendActionSerializer extends AbstractPipelet {

    private static Logger                 LOG                        = Logger.getLogger( BackendActionSerializer.class );

    private String                        choreographyId             = null;

    private FrontendOutboundDispatcher    frontendOutboundDispatcher = null;

    private FrontendInboundDispatcher     frontendInboundDispatcher  = null;

    private StateMachineExecutor          stateMachineExecutor       = null;

    private BlockingQueue<MessageContext> queue                      = new LinkedBlockingQueue<MessageContext>();

    private String                        queueName                  = "queue_name_not_set";

    private OutboundQueueListener         outboundQueueListener      = null;

    private Thread                        queueListenerThread        = null;

    /**
     * @param choreographyId
     */
    public BackendActionSerializer( String choreographyId ) {

        this.choreographyId = choreographyId;
        queueName = choreographyId + org.nexuse2e.Constants.POSTFIX_INBOUND_QUEUE;
        status = BeanStatus.INSTANTIATED;

    } // constructor

    /* (non-Javadoc)
     * @see org.nexuse2e.messaging.Pipelet#processMessage(org.nexuse2e.messaging.MessageContext)
     */
    public MessageContext processMessage( MessageContext messageContext ) throws NexusException {

        LOG.debug( "BackendActionSerializer.processMessage - " + choreographyId );

        if ( status == BeanStatus.ACTIVATED ) {

            //Test whether the action is allowed at this time
            ConversationPojo conversationPojo = null;
            try {
                conversationPojo = stateMachineExecutor.validateTransition( messageContext );
            } catch ( NexusException e ) {
                e.printStackTrace();
                LOG.error( "Not a valid action: " + messageContext.getMessagePojo().getAction() );
                throw e;
            }

            if ( conversationPojo == null ) {
                throw new NexusException( "Choreography transition not allowed at this time - message ID: "
                        + messageContext.getMessagePojo().getMessageId() );
            }

            // Persist and queue the message
            try {
                queueMessage( messageContext, conversationPojo, true );
            } catch ( Exception e ) {
                e.printStackTrace();
                LOG.error( "OutboundQueueListener.run detected an exception when storing message status: " + e );
            }

        } else {
            LOG.error( "Received message for BackendActionSerializer (" + choreographyId
                    + ") which hasn't been properly started!" );
        }

        return messageContext;
    } // processMessage

    /**
     * @param messageContext
     * @param conversationPojo
     * @param newMessage
     * @throws NexusException
     */
    private void queueMessage( MessageContext messageContext, ConversationPojo conversationPojo, boolean newMessage )
            throws NexusException {

        synchronized ( conversationPojo ) {

            List<MessagePojo> messages = conversationPojo.getMessages();

            messageContext.getMessagePojo().setStatus( org.nexuse2e.Constants.MESSAGE_STATUS_QUEUED );
            if ( messageContext.getMessagePojo().getType() == Constants.INT_MESSAGE_TYPE_NORMAL ) {
                messageContext.getMessagePojo().getConversation().setStatus(
                        org.nexuse2e.Constants.CONVERSATION_STATUS_PROCESSING );
                if ( newMessage ) {
                    messages.add( messageContext.getMessagePojo() );
                    Engine.getInstance().getTransactionService().storeTransaction( conversationPojo,
                            messageContext.getMessagePojo() );
                } else {
                    Engine.getInstance().getTransactionService().updateTransaction( conversationPojo );
                }
            } else {
                messageContext.getMessagePojo().setConversation( null );
                messages.add( messageContext.getMessagePojo() );
                messageContext.getMessagePojo().setConversation( conversationPojo );
                Engine.getInstance().getTransactionService().updateTransaction( conversationPojo );
            }

            // Submit the message to the queue/backend
            queue.add( messageContext );
        } // synchronized
    } // queueMessage

    /**
     * @param messageContext
     * @throws NexusException
     */
    public void requeueMessage( MessageContext messageContext ) throws NexusException {
        if ( messageContext == null ) {
            LOG.error( "No MessageContext supplied!" );
            throw new NexusException( "No MessageContext supplied!" );
        }
        queueMessage( messageContext, messageContext.getConversation(), false );        
    } // requeueMessage
    
    /**
     * @param choreographyId
     * @param participantId
     * @param conversationId
     * @param messageId
     * @throws NexusException
     */
    public void requeueMessage( String choreographyId, String participantId, String conversationId, String messageId )
            throws NexusException {

        LOG.debug( "Requeueing message " + messageId + " for choreography " + choreographyId + ", participant "
                + participantId + ", conversation " + conversationId );

        MessageContext messageContext = Engine.getInstance().getTransactionService().getMessageContext( messageId );

        if ( messageContext != null ) {
            queueMessage( messageContext, messageContext.getConversation(), false );
        } else {
            LOG.error( "Message: " + messageId + " could not be found in database, cancelled requeueing!" );
            throw new NexusException( "Message: " + messageId + " could not be found in database, cancelled requeueing!" );
        }
    } // requeueMessage

    /* (non-Javadoc)
     * @see org.nexuse2e.Manageable#deactivate()
     */
    public void deactivate() {

        LOG.trace( "deactivate " + hashCode() );
        if ( status == BeanStatus.ACTIVATED ) {
            outboundQueueListener.setStopRequested( true );
            queueListenerThread.interrupt();
            status = BeanStatus.INITIALIZED;
        }
    } // stop

    /* (non-Javadoc)
     * @see org.nexuse2e.Manageable#getRunLevel()
     */
    public Runlevel getActivationRunlevel() {

        return Runlevel.CORE;
    }

    /* (non-Javadoc)
     * @see org.nexuse2e.Manageable#activate()
     */
    public void activate() {

        LOG.trace( "activate - " + choreographyId + " " + hashCode() );

        if ( status == BeanStatus.INITIALIZED ) {
            queueListenerThread = new Thread( outboundQueueListener, queueName );
            queueListenerThread.start();
            status = BeanStatus.ACTIVATED;
        } else {
            LOG.error( "Trying to start uninitialized BackendActionSerializer (" + choreographyId + ")!" );
            new Exception().printStackTrace();
        }
    } // start

    /* (non-Javadoc)
     * @see org.nexuse2e.Manageable#initialize(org.nexuse2e.configuration.EngineConfiguration)
     */
    public void initialize( EngineConfiguration config ) {

        LOG.debug( "Initializing BackendActionSerializer " + choreographyId );

        frontendOutboundDispatcher = config.getStaticBeanContainer().getFrontendOutboundDispatcher();

        frontendInboundDispatcher = config.getStaticBeanContainer().getFrontendInboundDispatcher();

        stateMachineExecutor = config.getStaticBeanContainer().getFrontendInboundDispatcher();

        outboundQueueListener = new OutboundQueueListener();

        if ( validate() ) {
            status = BeanStatus.INITIALIZED;
        } else {
            status = BeanStatus.ERROR;
        }
    } // initialize

    /* (non-Javadoc)
     * @see org.nexuse2e.Manageable#teardown()
     */
    public void teardown() {

        LOG.debug( "Freeing resources..." );

        status = BeanStatus.INSTANTIATED;
    } // teardown

    /**
     * @return
     */
    public boolean validate() {

        if ( ( frontendOutboundDispatcher != null ) && ( stateMachineExecutor != null ) && ( queue != null )
                && ( outboundQueueListener != null ) ) {
            return true;
        }

        return false;
    } // validate

    /**
     * @return bean status
     */
    public BeanStatus getStatus() {

        return status;
    } // getStatus

    /**
     * Return the ID of the choreography this <code>BackendActionSerializer</code> is associated with.
     * @return the choreographyId The ID of the associated choreography.
     */
    public String getChoreographyId() {

        return choreographyId;
    } // getChoreographyId

    /**
     * Set the ID of the choreography this <code>BackendActionSerializer</code> is associated with.
     * @param choreographyId The choreography ID to set
     */
    public void setChoreographyId( String choreographyId ) {

        this.choreographyId = choreographyId;
    } // setChoreographyId

    /**
     * Listener that processes the next element from the queue
     * @author mbreilmann
     *
     */
    private class OutboundQueueListener implements Runnable {

        private boolean stopRequested = false;

        protected void setStopRequested( boolean stopRequested ) {

            this.stopRequested = stopRequested;
        }

        public void run() {

            while ( !stopRequested ) {
                try {
                    MessageContext messageContext = queue.take();
                    if ( Engine.getInstance().getTransactionService().isSynchronousReply(
                            messageContext.getMessagePojo().getMessageId() ) ) {
                        // TODO: Needs to be routed through queue
                        BackendActionSerializer.this.frontendInboundDispatcher
                                .processSynchronousReplyMessage( messageContext );
                        Engine.getInstance().getTransactionService().removeSynchronousRequest(
                                messageContext.getMessagePojo().getMessageId() );
                    } else {
                        BackendActionSerializer.this.frontendOutboundDispatcher.processMessage( messageContext );
                    }
                } catch ( NexusException nex ) {
                    nex.printStackTrace();
                    LOG.error( "InboundQueueListener.run detected an exception: " + nex );
                } catch ( InterruptedException ex ) {
                    BackendActionSerializer.LOG.debug( "Interrupted while listening on queue " );
                }
            }
            BackendActionSerializer.LOG.info( "Stopped InboundQueueListener "
                    + BackendActionSerializer.this.choreographyId );
            stopRequested = false;
        }

    } // InboundQueueListener

} // BackendActionSerializer
