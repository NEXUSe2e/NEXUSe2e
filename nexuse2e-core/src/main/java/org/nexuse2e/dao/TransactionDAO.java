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
package org.nexuse2e.dao;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Level;
import org.hibernate.Session;
import org.nexuse2e.NexusException;
import org.nexuse2e.controller.StateTransitionException;
import org.nexuse2e.pojo.ActionPojo;
import org.nexuse2e.pojo.ChoreographyPojo;
import org.nexuse2e.pojo.ConversationPojo;
import org.nexuse2e.pojo.LogPojo;
import org.nexuse2e.pojo.MessagePayloadPojo;
import org.nexuse2e.pojo.MessagePojo;
import org.nexuse2e.pojo.PartnerPojo;
import org.nexuse2e.reporting.Statistics;

public interface TransactionDAO {

    public static final int SORT_NONE     = 0;
    public static final int SORT_CREATED  = 1;
    public static final int SORT_MODIFIED = 2;
    public static final int SORT_STATUS   = 3;
    public static final int SORT_CPAID    = 4;
    public static final int SORT_ACTION   = 5;

    /**
     * Find a conversation by its identifier
     * @param conversationId The converstaion identifier
     * @return
     */
    public abstract ConversationPojo getConversationByConversationId( String conversationId ); // getConversationByConversationId

    /**
     * Gets a <code>ConversationPojo</code> by it's primary key.
     * @param nxConversationId The NEXUS conversation ID.
     * @return The conversation, or <code>null</code> if none with the given ID exists.
     */
    public abstract ConversationPojo getConversationByConversationId( int nxConversationId ); // getConversationByConversationId

    public abstract MessagePojo getMessageByMessageId( String messageId ) throws NexusException;

    public abstract MessagePojo getMessageByReferencedMessageId( String messageId ) throws NexusException;

    /**
     * @param status
     * @param nxChoreographyId
     * @param nxPartnerId
     * @param nxConversationId
     * @param messageId
     * @param startDate
     * @param endDate
     * @return
     * @throws NexusException
     */
    public abstract int getMessagesCount( String status, int nxChoreographyId, int nxPartnerId, String conversationId,
            String messageId, Date startDate, Date endDate ) throws NexusException;


    public abstract Statistics getStatistics(Date startDate, Date endDate);


        /**
         * @return
         * @throws NexusException
         */
    public abstract List<MessagePojo> getActiveMessages() throws NexusException; // getActiveMessages

    /**
     * @param status
     * @param nxChoreographyId
     * @param nxPartnerId
     * @param nxConversationId
     * @param messageId
     * @param type
     * @param start
     * @param end
     * @param itemsPerPage
     * @param page
     * @param field
     * @param ascending
     * @return
     * @throws NexusException
     */
    public abstract List<MessagePojo> getMessagesForReport( String status, int nxChoreographyId, int nxPartnerId,
            String conversationId, String messageId, String type, Date start, Date end, int itemsPerPage, int page,
            int field, boolean ascending ) throws NexusException;

    /**
     * @param status
     * @param nxChoreographyId
     * @param nxPartnerId
     * @param conversationId
     * @param start
     * @param end
     * @param itemsPerPage
     * @param page
     * @param field
     * @param ascending
     * @return
     * @throws NexusException
     */
    public abstract List<ConversationPojo> getConversationsForReport( String status, int nxChoreographyId,
            int nxPartnerId, String conversationId, Date start, Date end, int itemsPerPage, int page, int field,
            boolean ascending ) throws NexusException;

    /**
     * provides a plain list of conversation ids for the given parameters
     * @param status
     * @param nxChoreographyId
     * @param nxPartnerId
     * @param conversationId
     * @param start
     * @param end
     * @return
     * @throws NexusException
     */
    public List<BigDecimal> getConversationsForReportPlain(String status, int nxChoreographyId, int nxPartnerId, String conversationId, Date start, Date end) throws NexusException;


    /**
     * delete conversations
     * @throws NexusException
     */
    public void removeConversations(String status, int nxChoreographyId, int nxPartnerId, String conversationId, Date start, Date end) throws NexusException;

    /**
     * @param status
     * @param nxChoreographyId
     * @param nxPartnerId
     * @param conversationId
     * @param messageId
     * @param startDate
     * @param endDate
     * @throws NexusException
     */
    void removeMessages(String status, int nxChoreographyId, int nxPartnerId, String conversationId, String messageId, Date startDate, Date endDate) throws NexusException;

        /**
         * Gets the number of conversations created between the given start and end dates.
         * @param start The start date. May be <code>null</code> for stone age.
         * @param end The end date. May be <code>null</code> for Star Wars age.
         * @return The number of conversations that have been created between <code>start</code> and <code>end</code>.
         * @throws NexusException If something went wrong.
         */
    public abstract long getConversationsCount( Date start, Date end ) throws NexusException;

    /**
     * Gets the number of messages associated with a conversation that has been created
     * between the given start and end dates.
     * @param start The start date. May be <code>null</code> for stone age.
     * @param end The end date. May be <code>null</code> for Star Wars age.
     * @return The number of messages in conversations that have been created between
     * <code>start</code> and <code>end</code>.
     * @throws NexusException If something went wrong.
     */
    public abstract long getMessagesCount( Date start, Date end ) throws NexusException;

    /**
     * Gets the number of log entries created between the given start and end dates.
     * @param start The start date. May be <code>null</code> for stone age.
     * @param end The end date. May be <code>null</code> for Star Wars age.
     * @return The number of log entries that have been created between <code>start</code> and <code>end</code>.
     * @throws NexusException If something went wrong.
     */
    public abstract long getLogCount( Date start, Date end ) throws NexusException;

    /**
     * Gets the number of log entries created between the given start and end dates and the
     * given severity levels.
     * @param start The start date. May be <code>null</code> for stone age.
     * @param end The end date. May be <code>null</code> for Star Wars age.
     * @param minLevel The minimum log level. Can be <code>null</code> to indicate all levels.
     * @param maxLevel The maximum log level. Can be <code>null</code> to indicate min level only.
     * @return The number of log entries per severity level that have been created between
     * <code>start</code> and <code>end</code>, mapped from their severity <code>Level</code>.
     * @throws NexusException If something went wrong.
     */
    public abstract Map<Level,Long> getLogCount( Date start, Date end, Level minLevel, Level maxLevel ) throws NexusException;

    /**
     * @param start
     * @param end
     * @param session
     * @param transaction
     * @return
     * @throws NexusException
     */
    public abstract long removeLogEntries( Date start, Date end ) throws NexusException;

    /**
     * Removes all conversations that have been created between the given start and end dates.
     * @param start The start date. May be <code>null</code> for stone age.
     * @param end The end date. May be <code>null</code> for Star Wars age.
     * @return The number of conversations that have been deleted.
     * @throws NexusException If something went wrong.
     */
    public abstract long removeConversations( Date start, Date end ) throws NexusException;

    /**
     * @param status
     * @param choreographyId
     * @param participantId
     * @param conversationId
     * @param start
     * @param end
     * @param field
     * @param ascending
     * @return
     * @throws PersistenceException
     */
    public abstract int getConversationsCount( String status, int nxChoreographyId, int nxPartnerId,
            String conversationId, Date start, Date end, int field, boolean ascending ) throws NexusException;

    public abstract void storeTransaction( ConversationPojo conversationPojo, MessagePojo messagePojo )
            throws NexusException; // storeTransaction

    /**
     * @param partner
     * @param session
     * @param transaction
     * @return
     * @throws NexusException
     */
    public abstract List<ConversationPojo> getConversationsByPartner( PartnerPojo partner );

    /**
     * @param partner
     * @param choreography
     * @param session
     * @param transaction
     * @return
     * @throws NexusException
     */
    public abstract List<ConversationPojo> getConversationsByPartnerAndChoreography( PartnerPojo partner,
            ChoreographyPojo choreography ) throws NexusException;

    /**
     * @param choreography
     * @return
     */
    public abstract List<ConversationPojo> getConversationsByChoreography( ChoreographyPojo choreography );

    /**
     * @param partner
     * @param session
     * @param transaction
     * @return
     * @throws NexusException
     */
    public abstract List<MessagePojo> getMessagesByPartner( PartnerPojo partner, int field, boolean ascending )
            throws NexusException;

    /**
     * @param messagePojo
     * @param session
     * @param transaction
     * @throws NexusException
     */
    public abstract void deleteMessage( MessagePojo messagePojo ) throws NexusException; // updateMessage

    /**
     * @param conversationPojo
     * @param session
     * @param transaction
     * @throws NexusException
     */
    public abstract void deleteConversation( ConversationPojo conversationPojo ) throws NexusException; // updateMessage

    /**
     * @param partner
     * @param outbound
     * @param field
     * @param ascending
     * @param session
     * @param transaction
     * @return
     * @throws NexusException
     */
    public abstract List<MessagePojo> getMessagesByPartnerAndDirection( PartnerPojo partner, boolean outbound,
            int field, boolean ascending ) throws NexusException;

    public abstract List<MessagePojo> getMessagesByActionPartnerDirectionAndStatus( ActionPojo action,
            PartnerPojo partner, boolean outbound, int status, int field, boolean ascending );

    /**
     * @param choreography
     * @param partner
     * @param field
     * @param ascending
     * @return
     */
    public abstract List<MessagePojo> getMessagesByChoreographyAndPartner( ChoreographyPojo choreography,
            PartnerPojo partner, int field, boolean ascending );

    /**
     * @param choreography
     * @param partner
     * @param conversation
     * @param field
     * @param ascending
     * @return
     */
    public abstract List<MessagePojo> getMessagesByChoreographyPartnerAndConversation( ChoreographyPojo choreography,
            PartnerPojo partner, ConversationPojo conversation, int field, boolean ascending );

    /**
     * @param logEntry
     * @param session
     * @param transaction
     */
    public abstract void deleteLogEntry( LogPojo logEntry ) throws NexusException;

    public abstract List<MessagePayloadPojo> fetchLazyPayloads( MessagePojo message );

    public abstract List<MessagePojo> fetchLazyMessages( ConversationPojo conversation );

    public abstract void updateTransaction( MessagePojo message, boolean force ) throws NexusException, StateTransitionException;

    public abstract void updateTransaction(MessagePojo message, UpdateTransactionOperation operation, boolean force) throws NexusException, StateTransitionException;

        /**
     * Updates the retry count for the given persistent message.
     * @param message The message. Must not be <code>null</code>. Shall be persistent, otherwise
     * this method does nothing.
     * @throws NexusException
     */
    public abstract void updateRetryCount( MessagePojo message ) throws NexusException;
    
    /**
     * Gets a count of messages that have been created since the given time. 
     * @param since The earliest creation date of messages that shall be counted.
     * @return A count.
     * @throws NexusException if something went wrong.
     */
    public abstract int getCreatedMessagesSinceCount( Date since ) throws NexusException;
    
    /**
     * Gets a list of <code>int[]</code> objects mapping the conversation states (index 0) to their
     * conversation counts (index 1).
     * @param since The date boundary.
     * @return The mapping list as described above. If no conversations were found, an empty list is returned.
     */
    public abstract List<int[]> getConversationStatesSince( Date since );
    
    /**
     * Gets a list of <code>int[]</code> objects mapping the message states (index 0) to their
     * message counts (index 1).
     * @param since The date boundary.
     * @return The mapping list as described above. If no messages were found, an empty list is returned.
     */
    public abstract List<int[]> getMessageStatesSince( Date since );
    
    /**
     * Gets a list of string arrays mapping conversation names (index 0) to their message counts
     * (as a string, index 1).
     * @param since The date boundary.
     * @return The mapping list as described above.
     */
    public List<String[]> getMessagesPerConversationSince( Date since );

    /**
     * Gets a list of <code>int[]</code> objects mapping the hour of the day (index 0, values 0 to 23) to their
     * message counts (index 1).
     * @return A mapping list as described above with 24 entries (including 0-message entries). The first entry
     * contains the least recent hour of day.
     */
    public List<int[]> getMessagesPerHourLast24Hours();
    
    /**
     * Convenience method for direct hibernate session access. This method shall only
     * be called if advanced features are required that are not directly supported by
     * <code>TransactionDAO</code>.
     * @return A DB session.
     */
    public Session getDBSession();

    /**
     * Release the given session.
     * @param session The session to be released.
     */
    public void releaseDBSession( Session session );


}