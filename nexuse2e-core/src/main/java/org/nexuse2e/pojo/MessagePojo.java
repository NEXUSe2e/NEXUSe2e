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
package org.nexuse2e.pojo;

// Generated 27.11.2006 12:45:03 by Hibernate Tools 3.2.0.beta6a

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nexuse2e.messaging.Constants;
import org.nexuse2e.messaging.ErrorDescriptor;

/**
 * MessagePojo generated by hbm2java
 */
public class MessagePojo implements NEXUSe2ePojo {
    
    /**
     * 
     */
    private static final long        serialVersionUID = 7535700207629664430L;

    // Fields    

    private int                      nxMessageId;
    private ConversationPojo         conversation;
    private String                   messageId;
    private byte[]                   headerData;
    private int                      type;
    private ActionPojo               action;
    private int                      status;
    private TRPPojo                  TRP;
    private MessagePojo              referencedMessage;
    private int                      retries;
    private boolean                  outbound;
    private Date                     expirationDate;
    private Date                     endDate;
    private Date                     createdDate;
    private Date                     modifiedDate;
    private int                      modifiedNxUserId;
    private List<MessagePayloadPojo> messagePayloads  = new ArrayList<MessagePayloadPojo>( 0 );
    private List<MessageLabelPojo>   messageLabels    = new ArrayList<MessageLabelPojo>( 0 );

    private List<ErrorDescriptor>    errors           = new ArrayList<ErrorDescriptor>();
    private Map<String, String>      customParameters = new HashMap<String, String>();

    // Constructors

    /** default constructor */
    public MessagePojo() {

        createdDate = new Date();
        modifiedDate = createdDate;
    }

    /** minimal constructor */
    public MessagePojo( ConversationPojo conversation, String messageId, byte[] headerData, int type,
            ActionPojo action, int status, int retries, boolean outbound, Date createdDate, Date modifiedDate,
            int modifiedNxUserId ) {

        this.conversation = conversation;
        this.messageId = messageId;
        this.headerData = headerData;
        this.type = type;
        this.action = action;
        this.status = status;
        this.retries = retries;
        this.outbound = outbound;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
        this.modifiedNxUserId = modifiedNxUserId;
    }

    /** full constructor */
    public MessagePojo( ConversationPojo conversation, String messageId, byte[] headerData, int type,
            ActionPojo action, int status, TRPPojo TRP, MessagePojo referencedMessageId, int retries, boolean outbound,
            Date expirationDate, Date endDate, Date createdDate, Date modifiedDate, int modifiedNxUserId,
            List<MessagePayloadPojo> messagePayloads, List<MessageLabelPojo> messageLabels ) {

        this.conversation = conversation;
        this.messageId = messageId;
        this.headerData = headerData;
        this.type = type;
        this.action = action;
        this.status = status;
        this.TRP = TRP;
        this.referencedMessage = referencedMessageId;
        this.retries = retries;
        this.outbound = outbound;
        this.expirationDate = expirationDate;
        this.endDate = endDate;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
        this.modifiedNxUserId = modifiedNxUserId;
        this.messagePayloads = messagePayloads;
        this.messageLabels = messageLabels;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {

        MessagePojo clonedMessagePojo = new MessagePojo();

        clonedMessagePojo.setAction( action );
        clonedMessagePojo.setConversation( conversation );
        clonedMessagePojo.setCreatedDate( createdDate );
        clonedMessagePojo.setCustomParameters( customParameters );
        clonedMessagePojo.setEndDate( endDate );
        clonedMessagePojo.setErrors( errors );
        clonedMessagePojo.setExpirationDate( expirationDate );
        clonedMessagePojo.setHeaderData( headerData != null ? headerData.clone() : null );
        clonedMessagePojo.setMessageId( messageId );
        List<MessageLabelPojo> clonedMessageLabels = new ArrayList<MessageLabelPojo>();
        for ( MessageLabelPojo messageLabelPojo : clonedMessageLabels ) {
            MessageLabelPojo clonedMessageLabelPojo = (MessageLabelPojo)messageLabelPojo.clone();
            clonedMessageLabelPojo.setMessage( clonedMessagePojo );
            clonedMessageLabels.add( clonedMessageLabelPojo );
        }
        clonedMessagePojo.setMessageLabels( clonedMessageLabels );
        List<MessagePayloadPojo> clonedMessagePayloads = new ArrayList<MessagePayloadPojo>();
        for ( MessagePayloadPojo messagePayload : messagePayloads ) {
            MessagePayloadPojo clonedMessagePayload = (MessagePayloadPojo)messagePayload.clone();
            clonedMessagePayload.setMessage( clonedMessagePojo );
            clonedMessagePayloads.add( clonedMessagePayload );
        }
        clonedMessagePojo.setMessagePayloads( clonedMessagePayloads );
        clonedMessagePojo.setModifiedDate( modifiedDate );
        clonedMessagePojo.setModifiedNxUserId( modifiedNxUserId );
        clonedMessagePojo.setOutbound( outbound );
        clonedMessagePojo.setReferencedMessage( referencedMessage );
        clonedMessagePojo.setRetries( retries );
        clonedMessagePojo.setStatus( status );
        clonedMessagePojo.setTRP( TRP );
        clonedMessagePojo.setType( type );

        return clonedMessagePojo;
    } // clone

    /**
     * Convenience method to retrieve the participant for this message.
     * @return The participant if it was found
     */
    public ParticipantPojo getParticipant() {

        if ( conversation != null ) {
            ChoreographyPojo choreographyPojo = conversation.getChoreography();
            PartnerPojo partnerPojo = conversation.getPartner();
            if ( ( choreographyPojo != null ) && ( partnerPojo != null ) ) {
                List<ParticipantPojo> participants = choreographyPojo.getParticipants();
                if ( participants != null ) {
                    for ( ParticipantPojo pojo : participants ) {
                        if ( ( pojo.getPartner() != null )
                                && ( pojo.getPartner().getNxPartnerId() == partnerPojo.getNxPartnerId() ) ) {
                            return pojo;
                        }
                    }
                }
            }
        }

        return null;
    }

    // Property accessors
    public int getNxMessageId() {

        return this.nxMessageId;
    }

    public void setNxMessageId( int nxMessageId ) {

        this.nxMessageId = nxMessageId;
    }

    public int getNxId() {
        return nxMessageId;
    }
    
    public void setNxId( int nxId ) {
        this.nxMessageId = nxId;
    }
    
    public ConversationPojo getConversation() {

        return this.conversation;
    }

    public void setConversation( ConversationPojo conversation ) {

        this.conversation = conversation;
    }

    public String getMessageId() {

        return this.messageId;
    }

    public void setMessageId( String messageId ) {

        this.messageId = messageId;
    }

    public byte[] getHeaderData() {

        return this.headerData;
    }

    public void setHeaderData( byte[] headerData ) {

        this.headerData = headerData;
    }

    public int getType() {

        return this.type;
    }

    public void setType( int type ) {

        this.type = type;
    }

    public ActionPojo getAction() {

        return this.action;
    }

    public void setAction( ActionPojo action ) {

        this.action = action;
    }

    public int getStatus() {

        return this.status;
    }

    public void setStatus( int status ) {

        // LOG.trace( "Message ID: " + messageId + " - status: " + status );

        this.status = status;
    }

    public TRPPojo getTRP() {

        return this.TRP;
    }

    public void setTRP( TRPPojo TRP ) {

        this.TRP = TRP;
    }

    public MessagePojo getReferencedMessage() {

        return this.referencedMessage;
    }

    public void setReferencedMessage( MessagePojo referencedMessageId ) {

        this.referencedMessage = referencedMessageId;
    }

    public int getRetries() {

        return this.retries;
    }

    public void setRetries( int retries ) {

        this.retries = retries;
    }

    public boolean isOutbound() {

        return this.outbound;
    }

    public void setOutbound( boolean outbound ) {

        this.outbound = outbound;
    }

    public Date getExpirationDate() {

        return this.expirationDate;
    }

    public void setExpirationDate( Date expirationDate ) {

        this.expirationDate = expirationDate;
    }

    public Date getEndDate() {

        return this.endDate;
    }

    public void setEndDate( Date endDate ) {

        this.endDate = endDate;
    }

    public Date getCreatedDate() {

        return this.createdDate;
    }

    public void setCreatedDate( Date createdDate ) {

        this.createdDate = createdDate;
    }

    public Date getModifiedDate() {

        return this.modifiedDate;
    }

    public void setModifiedDate( Date modifiedDate ) {

        this.modifiedDate = modifiedDate;
    }

    public int getModifiedNxUserId() {

        return this.modifiedNxUserId;
    }

    public void setModifiedNxUserId( int modifiedNxUserId ) {

        this.modifiedNxUserId = modifiedNxUserId;
    }

    public List<MessagePayloadPojo> getMessagePayloads() {

        return this.messagePayloads;
    }

    public void setMessagePayloads( List<MessagePayloadPojo> messagePayloads ) {

        this.messagePayloads = messagePayloads;
    }

    public List<MessageLabelPojo> getMessageLabels() {

        return this.messageLabels;
    }

    public void setMessageLabels( List<MessageLabelPojo> messageLabels ) {

        this.messageLabels = messageLabels;
    }

    /**
     * @param key
     * @param value
     */
    public void addCustomParameter(String key, String value) {
        if(customParameters == null) {
            customParameters = new HashMap<String, String>();
        }
        customParameters.put( key, value );
    }
    
    /**
     * @return the customParameters
     */
    public Map<String, String> getCustomParameters() {

        return customParameters;
    }

    /**
     * @param customParameters the customParameters to set
     */
    public void setCustomParameters( Map<String, String> customParameters ) {

        this.customParameters = customParameters;
    }

    /**
     * @return the errors
     */
    public List<ErrorDescriptor> getErrors() {

        return errors;
    }

    /**
     * @param errors the errors to set
     */
    public void setErrors( List<ErrorDescriptor> errors ) {

        this.errors = errors;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( Object obj ) {

        if ( !( obj instanceof MessagePojo ) ) {
            return false;
        }
        if ( nxMessageId == 0 ) {
            return super.equals( obj );
        }

        return nxMessageId == ( (MessagePojo) obj ).nxMessageId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        if ( nxMessageId == 0 ) {
            return super.hashCode();
        }

        return nxMessageId;
    }

    /**
     * Performs a flat copy of all properties (except for non-persistent properties, object references
     * and ID) from the given <code>MessagePojo</code> onto this object.
     * @param message The message to be copied onto this message object.
     */
    public void setProperties( MessagePojo message ) {
        if (message == null || message == this) {
            return;
        }
        
        this.messageId = message.messageId;
        this.headerData = message.headerData;
        this.type = message.type;
        this.status = message.status;
        this.retries = message.retries;
        this.outbound = message.outbound;
        this.expirationDate = message.expirationDate;
        this.endDate = message.endDate;
        this.createdDate = message.createdDate;
        this.modifiedDate = message.modifiedDate;
        this.modifiedNxUserId = message.modifiedNxUserId;
    }
    
    public static String getStatusName( int status ) {
        switch (status) {
        case Constants.MESSAGE_STATUS_FAILED:
            return "FAILED";
        case Constants.MESSAGE_STATUS_UNKNOWN:
            return "UNKNOWN";
        case Constants.MESSAGE_STATUS_RETRYING:
            return "RETRYING";
        case Constants.MESSAGE_STATUS_QUEUED:
            return "QUEUED";
        case Constants.MESSAGE_STATUS_SENT:
            return "SENT";
        case Constants.MESSAGE_STATUS_STOPPED:
            return "STOPPED";
        }
        return "UNKNOWN";
    }
    
    public static String getTypeName( int type ) {
    	switch ( type ) {
	    	case org.nexuse2e.messaging.Constants.INT_MESSAGE_TYPE_ACK:
	    		return "Acknowledgement";
	    	case org.nexuse2e.messaging.Constants.INT_MESSAGE_TYPE_NORMAL:
	    		return "Normal";
	    	case org.nexuse2e.messaging.Constants.INT_MESSAGE_TYPE_ERROR:
	    		return "Error";
	    	default:
	    		return "Unknown message type (" + type + ")";
    	}
    }

	@Override
	public String toString() {
		return getMessageId()
			+ " "
			+ getTypeName( type )
			+ " "
			+ ( getAction() != null ? getAction().getName() : "n/a" )
			+ " "
			+ getStatusName( status )
			+ " "
			+ getCreatedDate();
	}
}
