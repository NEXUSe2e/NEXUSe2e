/**
 *  NEXUSe2e Business Messaging Open Source
 *  Copyright 2000-2009, Tamgroup and X-ioma GmbH
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation version 2.1 of
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
package org.nexuse2e.messaging;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;
import org.nexuse2e.Engine;
import org.nexuse2e.Manageable;
import org.nexuse2e.NexusException;
import org.nexuse2e.Constants.BeanStatus;
import org.nexuse2e.Constants.Layer;
import org.nexuse2e.configuration.EngineConfiguration;
import org.nexuse2e.controller.StateTransitionException;
import org.nexuse2e.logging.LogMessage;
import org.nexuse2e.pojo.ConversationPojo;
import org.nexuse2e.pojo.MessagePojo;

/**
 * Component in the NEXUSe2e frontend that serializes the processing of inbound messages per Choreography. 
 * Messages are placed in a queue and are processed by a single thread in fifo order.
 *
 * @author mbreilmann
 */
public class FrontendActionSerializer implements Manageable {

    private static Logger                 LOG                      = Logger.getLogger( FrontendActionSerializer.class );

    private String                        choreographyId           = null;

    private BackendInboundDispatcher      backendInboundDispatcher = null;

    private StateMachineExecutor          stateMachineExecutor     = null;

    private BlockingQueue<MessageContext> queue                    = new LinkedBlockingQueue<MessageContext>();

    private String                        queueName                = "queue_name_not_set";

    private BeanStatus                    status                   = BeanStatus.UNDEFINED;

    private InboundQueueListener          inboundQueueListener     = null;

    private Thread                        queueListenerThread      = null;

    /**
     * @param choreographyId
     */
    public FrontendActionSerializer( String choreographyId ) {

        this.choreographyId = choreographyId;
        queueName = choreographyId + org.nexuse2e.Constants.POSTFIX_OUTBOUND_QUEUE;

    } // constructor

    /**
     * @param messageContext
     * @return
     */
    public MessageContext processMessage( MessageContext messageContext ) throws NexusException {

        if ( LOG.isDebugEnabled() ) {
        	LOG.debug( new LogMessage( "FrontendActionSerializer.processMessage - " + choreographyId
        			+ "/" + messageContext.getMessagePojo().getAction().getName()
	    			+ "/" + MessagePojo.getTypeName( messageContext.getMessagePojo().getType() ), messageContext ) );
	        LOG.debug( new LogMessage( messageContext.getStateMachine().toString(), messageContext ) );
        }

        if ( status == BeanStatus.STARTED ) {

            // Test whether the action is allowed at this time
            ConversationPojo conversationPojo = null;
            try {
                conversationPojo = stateMachineExecutor.validateTransition( messageContext );
            } catch ( NexusException e ) {
                e.printStackTrace();
                LOG.error( new LogMessage( "Not a valid action: " + messageContext.getMessagePojo().getAction(),
                        messageContext.getMessagePojo() ) );
                throw e;
            }

            if ( conversationPojo == null ) {
                throw new NexusException( "Choreography transition not allowed at this time - message ID: "
                        + messageContext.getMessagePojo().getMessageId() );
            }

            // Persist the message
            try {
                queueMessage( messageContext, false );
            } catch ( Exception e ) {
            	if ( !(e instanceof NexusException) ) {
	                throw new NexusException( new LogMessage(
	                		"Error storing new conversation/message state: " + e.getMessage(),
	                		messageContext ), e );
            	} else {
            		throw (NexusException) e;
            	}
            }
        } else {
            throw new NexusException( new LogMessage( "Received message for FrontendActionSerializer (" + choreographyId
                    + ") which hasn't been properly started!", messageContext.getMessagePojo() ) );
        }

        return messageContext;
    } // processMessage

    /**
     * @param messageContext
     * @param force Indicates if state transition shall be forced.
     * @throws NexusException
     */
    private void queueMessage( MessageContext messageContext, boolean force )
            throws NexusException {

        try {
            messageContext.getStateMachine().queueMessage( force );
        } catch (StateTransitionException e) {
        	LOG.warn( new LogMessage( e.getMessage(), messageContext ) );
        }
        queue.add( messageContext );
    } // queueMessage

    /**
     * @param choreographyId
     * @param participantId
     * @param conversationId
     * @param messageId
     * @throws NexusException
     */
    public void requeueMessage( String choreographyId, String participantId, String conversationId, String messageId )
            throws NexusException {

        LOG.debug( new LogMessage( "Requeueing message " + messageId + " for choreography " + choreographyId
                + ", participant " + participantId + ", conversation " + conversationId, conversationId, messageId ) );

        MessageContext messageContext = Engine.getInstance().getTransactionService().getMessageContext( messageId );
        requeueMessage( messageContext, conversationId, messageId );
    }

    /**
     * @param messageContext
     * @param conversationId
     * @param messageId
     * @throws NexusException
     */
    public void requeueMessage( MessageContext messageContext, String conversationId, String messageId )
            throws NexusException {

        // if message is processing, cancel it
        if (Engine.getInstance().getTransactionService().isProcessingMessage( messageId )) {
            Engine.getInstance().getTransactionService().deregisterProcessingMessage( messageId );
        }

        if ( messageContext != null ) {
            // set message end date to null, otherwise it's processing may be cancelled
            if (messageContext.getMessagePojo() != null) {
                messageContext.getMessagePojo().setEndDate( null );
            }
            
            // queue message
            queueMessage( messageContext, true );
        } else {
            LOG.error( "Message: " + messageId + " could not be found in database, cancelled requeueing!" );
        }
    }

    /**
     * @return the choreographyId
     */
    public String getChoreographyId() {

        return choreographyId;
    } // getChoreographyId

    /**
     * @param choreographyId the choreographyId to set
     */
    public void setChoreographyId( String choreographyId ) {

        this.choreographyId = choreographyId;
    } // setChoreographyId

    /**
     * 
     */
    public void deactivate() {

        LOG.trace( "deactivate" );

        if ( status == BeanStatus.STARTED ) {
            inboundQueueListener.setStopRequested( true );
            queueListenerThread.interrupt();
            status = BeanStatus.INITIALIZED;
        }
    } // stop

    /* (non-Javadoc)
     * @see org.nexuse2e.Manageable#getRunLevel()
     */
    public Layer getActivationLayer() {

        return Layer.CORE;
    }

    /**
     * 
     */
    public void activate() {

        LOG.trace( "FrontendActionSerializer.activate - " + choreographyId );

        if ( status == BeanStatus.INITIALIZED ) {
            queueListenerThread = new Thread( inboundQueueListener, queueName );
            queueListenerThread.start();
            status = BeanStatus.STARTED;
        } else {
            LOG.error( "Trying to start uninitialized FrontendActionSerializer (" + choreographyId + ")!" );
        }
    } // start

    /**
     * force configuration update
     */
    public void initialize() {

        initialize( Engine.getInstance().getCurrentConfiguration() );
    } // initialize

    /**
     * force configuration update
     */
    public void initialize( EngineConfiguration config ) {

        LOG.trace( "Initializing FrontendActionSerializer " + choreographyId );

        backendInboundDispatcher = config.getStaticBeanContainer().getBackendInboundDispatcher();

        stateMachineExecutor = config.getStaticBeanContainer().getFrontendInboundDispatcher();

        inboundQueueListener = new InboundQueueListener();

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

        LOG.trace( "Freeing resources..." );

        status = BeanStatus.INSTANTIATED;
    } // teardown

    /**
     * @return
     */
    public boolean validate() {

        if ( ( backendInboundDispatcher != null ) && ( stateMachineExecutor != null ) && ( queue != null )
                && ( inboundQueueListener != null ) ) {
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
     * @author mbreilmann
     *
     */
    private class InboundQueueListener implements Runnable {

        private boolean stopRequested = false;

        protected void setStopRequested( boolean stopRequested ) {

            this.stopRequested = stopRequested;
        }

        public void run() {

            MessageContext messageContext = null;

            while ( !stopRequested ) {
                try {
                    messageContext = queue.take();
                    Object syncObj = Engine.getInstance().getTransactionService().getSyncObjectForConversation( messageContext.getConversation() );
                    synchronized (syncObj) {
                        try {
                            // Initiate the backend process
                            FrontendActionSerializer.this.backendInboundDispatcher.processMessage( messageContext );
                            
                            messageContext.getStateMachine().processedBackend();
                        } catch ( NexusException nex ) {
                            LOG.error( "InboundQueueListener.run detected an exception: ", nex );
                            try {
                                messageContext.getStateMachine().processingFailed();
                            } catch (StateTransitionException e) {
                                LOG.warn( new LogMessage( e.getMessage(), messageContext ) );
                            } catch (NexusException e) {
                                LOG.error( new LogMessage( "Error while setting conversation status to ERROR: "
                                		+ e.getMessage(), messageContext), e );
                            }
                        } catch (StateTransitionException stex) {
                            LOG.warn( new LogMessage( stex.getMessage(), messageContext ) );
                        }
                    }
                } catch ( InterruptedException ex ) {
                    LOG.debug( new LogMessage( "Interrupted while listening on queue ",
                            (messageContext == null ? null : messageContext.getMessagePojo() ) ) );
                }
            } // while
            LOG.info( new LogMessage( "Stopped InboundQueueListener (FrontendActionSerializer) "
                    + FrontendActionSerializer.this.choreographyId,
                    (messageContext == null ? null : messageContext.getMessagePojo() ) ) );
            stopRequested = false;
        } // run

    } // InboundQueueListener

} // FrontendActionSerializer
