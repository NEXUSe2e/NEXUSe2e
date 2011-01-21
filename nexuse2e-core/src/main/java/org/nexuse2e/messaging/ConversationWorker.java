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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.nexuse2e.Engine;
import org.nexuse2e.NexusException;
import org.nexuse2e.controller.StateTransitionException;
import org.nexuse2e.controller.TransactionService;
import org.nexuse2e.logging.LogMessage;
import org.nexuse2e.pojo.ActionPojo;
import org.nexuse2e.pojo.ConversationPojo;
import org.nexuse2e.pojo.MessagePojo;
import org.nexuse2e.pojo.ParticipantPojo;

/**
 * This class implements <code>Runnable</code> in order to asynchronously process inbound/outbound
 * messages.
 * <p>
 * For client code, only the static method {@link #queue(MessageContext)} is relevant.
 * 
 * @author sschulze, jreese
 */
public class ConversationWorker implements Runnable {

    private static final Logger LOG = Logger.getLogger(ConversationWorker.class);

//    private static Map<String, ConversationWorker> workers = new HashMap<String, ConversationWorker>();
    private static ScheduledExecutorService threadPool = Executors.newScheduledThreadPool(100); // TODO: Make this configurable

    //private DelayQueue<DelayedMessageContext> queue;
    private ScheduledFuture<?> handle;
    private int retries;
    private int interval;
    private boolean updateMessageContext;
    private int initialDelay;
    private MessageContext messageContext;

    private ConversationWorker(MessageContext messageContext) {
        this.messageContext = messageContext;
    }

    /**
     * Adds a message to the message scheduler.
     * @param messageContext The message context to be queued. Must not be <code>null</code>.
     */
    public static void queue(MessageContext messageContext) {
        queue(messageContext, 0, false);
    }
    
    
    private static void queue(MessageContext messageContext, int initialDelay, boolean updateMessageContext) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(new LogMessage("Created new conversation worker", messageContext));
        }
        // queue msg
        if (LOG.isDebugEnabled()) {
            LOG.debug(new LogMessage("Queuing message", messageContext));
        }

        // init execution of worker
        ConversationWorker worker = new ConversationWorker(messageContext);
        worker.initialDelay = initialDelay;
        worker.updateMessageContext = updateMessageContext;
        worker.retries = 0;
        worker.interval = Constants.DEFAULT_MESSAGE_INTERVAL;
        if (!messageContext.getMessagePojo().isAck() &&
                messageContext.getParticipant().getConnection().isReliable()) {
            ParticipantPojo participantPojo = messageContext.getMessagePojo().getParticipant();
            if (participantPojo != null) {
                worker.retries = participantPojo.getConnection().getRetries();
                worker.interval = participantPojo.getConnection().getMessageInterval();
            }
        }

        if (messageContext.getMessagePojo().isOutbound() && worker.retries > 0) {
            worker.handle = threadPool.scheduleWithFixedDelay(worker, initialDelay, worker.interval, TimeUnit.SECONDS);
            Engine.getInstance().getTransactionService().registerProcessingMessage(messageContext.getMessagePojo(), worker.handle);
        } else {
            worker.handle = threadPool.schedule(worker, initialDelay, TimeUnit.SECONDS);
        }
    }

    public void run() {
        if (updateMessageContext) {
            try {
                boolean firstTimeInQueue = messageContext.isFirstTimeInQueue();
                ActionPojo currentAction = messageContext.getConversation().getCurrentAction();
                messageContext = Engine.getInstance().getTransactionService().getMessageContext(messageContext.getMessagePojo().getMessageId());
                messageContext.setFirstTimeInQueue(firstTimeInQueue); // restore previous firstTimeInQueue flag, since this is in-memory only
                messageContext.getConversation().setCurrentAction(currentAction); // restore current action, since this was updated in the old MessageContext
            } catch (NexusException e) {
                LOG.warn("Error creating new MessageContext on re-processing, recycling old MessageContext", e);
            }
        }
        updateMessageContext = true;
        
        if (LOG.isDebugEnabled()) {
            LOG.debug(new LogMessage("Starting message processing... ", messageContext));
        }
        // process
        if (messageContext.getMessagePojo().isOutbound()) {
            // process outbound
            processOutbound(messageContext);
        } else {
            // process inbound
            processInbound(messageContext);
        }
    }

    protected void processInbound(MessageContext messageContext) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(new LogMessage("Processing inbound message: "
                    + messageContext.getStateMachine().toString(),
                    messageContext));
        }
        
        TransactionService transactionService = Engine.getInstance().getTransactionService();
        Object syncObj = transactionService.getSyncObjectForConversation(messageContext.getConversation());
        synchronized (syncObj) {
            // Initiate the backend process
            // We Synchronize the conversation so that -- with fast back-end
            // systems -- response
            // messages don't get processed earlier than the state machine
            // transition.
            try {
                Engine.getInstance().getCurrentConfiguration()
                        .getStaticBeanContainer().getBackendInboundDispatcher()
                        .processMessage(messageContext);
                messageContext.getStateMachine().processedBackend();

            } catch (NexusException nex) {
                LOG.error("InboundQueueListener.run detected an exception: ", nex);
                try {
                    messageContext.getStateMachine().processingFailed();
                } catch (StateTransitionException e) {
                    LOG.warn(new LogMessage(e.getMessage(), messageContext));
                } catch (NexusException e) {
                    LOG.error(new LogMessage(
                            "Error while setting conversation status to ERROR: "
                                    + e.getMessage(), messageContext), e);
                }
            } catch (StateTransitionException stex) {
                LOG.warn(new LogMessage(stex.getMessage(), messageContext));
            }
        }
    }

    protected void processOutbound(MessageContext messageContext) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(new LogMessage("Processing outbound message...", messageContext));
        }
        
        TransactionService transactionService = Engine.getInstance().getTransactionService();
        
        if (!messageContext.getMessagePojo().isAck()) {
            // check if ack message for previous step is pending, so this message needs to be sent later
            ConversationPojo conv = messageContext.getConversation();
            if (conv != null && conv.getMessages() != null) {
                for (MessagePojo m : conv.getMessages()) {
                    if (m.isAck() && m.isOutbound() && m.getStatus() == Constants.MESSAGE_STATUS_QUEUED) {
                        LOG.debug(new LogMessage(
                                "Not sending " + messageContext.getMessagePojo().getTypeName() +
                                " message now, outbound ack for previous choreography action still in QUEUED. " +
                                "Requeueing this message in " + (initialDelay + 1) + " s.", messageContext));
                        transactionService.deregisterProcessingMessage(messageContext.getMessagePojo().getMessageId());
                        queue(messageContext, initialDelay + 1, true);
                        return;
                    }
                }
            }
        }
        
        try {
            if (transactionService.isSynchronousReply(messageContext.getMessagePojo().getMessageId())) {
                Engine.getInstance().getCurrentConfiguration().getStaticBeanContainer().getFrontendInboundDispatcher().processSynchronousReplyMessage(messageContext);
                transactionService.removeSynchronousRequest(messageContext.getMessagePojo().getMessageId());
            } else {
                Engine.getInstance().getCurrentConfiguration().getStaticBeanContainer().getFrontendOutboundDispatcher().processMessage(messageContext);
            }
        } catch (NexusException e) {
            LOG.error(new LogMessage(
                    "OutboundQueueListener.run() detected an exception: "
                            + e.getMessage(), messageContext), e);
        }
    }

}
