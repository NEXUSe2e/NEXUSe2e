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
package org.nexuse2e.messaging.ebxml.v10;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.nexuse2e.Engine;
import org.nexuse2e.NexusException;
import org.nexuse2e.ProtocolSpecificKey;
import org.nexuse2e.logging.LogMessage;
import org.nexuse2e.messaging.Constants.ErrorMessageReasonCode;
import org.nexuse2e.messaging.ebxml.v20.Constants;
import org.nexuse2e.messaging.ErrorDescriptor;
import org.nexuse2e.messaging.MessageContext;
import org.nexuse2e.pojo.ChoreographyPojo;
import org.nexuse2e.pojo.MessagePojo;
import org.nexuse2e.pojo.ParticipantPojo;
import org.nexuse2e.pojo.PartnerPojo;

/**
 * @author gesch
 *
 */
public class ProtocolAdapter implements org.nexuse2e.messaging.ProtocolAdapter {

    private static Logger       LOG = Logger.getLogger( ProtocolAdapter.class );

    private ProtocolSpecificKey key = null;

    /* (non-Javadoc)
     * @see org.nexuse2e.messaging.ProtocolAdapter#createAcknowledge(org.nexuse2e.messaging.MessageContext)
     */
    public MessageContext createAcknowledgement( ChoreographyPojo choreography, MessageContext messageContext ) throws NexusException {

        MessageContext ackMessageContext = null;
        try {
            ackMessageContext = (MessageContext) messageContext.clone();
        } catch ( CloneNotSupportedException e ) {
            throw new NexusException( new LogMessage( "Cannot clone message context to create an acknowledgment: " + e.getMessage(), messageContext ), e );
        }

        String currentMessageId = messageContext.getMessagePojo().getMessageId();
        String currentPartnerId = messageContext.getMessagePojo().getConversation().getPartner().getPartnerId();

        MessagePojo acknowledgment = new MessagePojo();
        acknowledgment.setCustomParameters( new HashMap<String, String>() );
        acknowledgment.setCreatedDate( new Date() );
        acknowledgment.setModifiedDate( new Date() );

        acknowledgment.setTRP( messageContext.getMessagePojo().getTRP() );

        acknowledgment.setType( org.nexuse2e.messaging.Constants.INT_MESSAGE_TYPE_ACK );

        acknowledgment.setAction( messageContext.getMessagePojo().getAction() );

        String messageId;
        try {
            messageId = Engine.getInstance().getIdGenerator( org.nexuse2e.Constants.ID_GENERATOR_MESSAGE ).getId();
        } catch ( NexusException e ) {
            LOG.fatal( "Unable to create AcknowldegeMessageId for message: " + currentMessageId );
            e.printStackTrace();
            ackMessageContext.setMessagePojo( null );
            ackMessageContext.setOriginalMessagePojo( null );
            return ackMessageContext;
        }
        acknowledgment.setMessageId( messageId );

        acknowledgment.setReferencedMessage( messageContext.getMessagePojo() );

        PartnerPojo partner = Engine.getInstance().getActiveConfigurationAccessService().getPartnerByPartnerId( currentPartnerId );

        if ( partner == null ) {
            ackMessageContext.setMessagePojo( null );
            LOG.error( "No partner entry found for partnerId: " + currentPartnerId );
            return ackMessageContext;
        }
        ParticipantPojo participant = Engine.getInstance().getActiveConfigurationAccessService()
                .getParticipantFromChoreographyByPartner(
                        messageContext.getMessagePojo().getConversation().getChoreography(), partner );

        acknowledgment.getCustomParameters().put( Constants.PROTOCOLSPECIFIC_TOIDTYPE, partner.getPartnerIdType() );
        acknowledgment.getCustomParameters().put( Constants.PROTOCOLSPECIFIC_FROM,
                participant.getLocalPartner().getPartnerId() );
        acknowledgment.getCustomParameters().put( Constants.PROTOCOLSPECIFIC_FROMIDTYPE,
                participant.getLocalPartner().getPartnerIdType() );
        acknowledgment.getCustomParameters().put( Constants.PROTOCOLSPECIFIC_SERVICE, "uri:Acknowledgement" );

        if (LOG.isTraceEnabled()) {
            LOG.trace( new LogMessage( "-----conversation:" + messageContext.getMessagePojo().getConversation(),messageContext.getMessagePojo()) );
        }
        acknowledgment.setConversation( messageContext.getMessagePojo().getConversation() );
        acknowledgment.setOutbound( true );

        ackMessageContext.setMessagePojo( acknowledgment );

        return ackMessageContext;
    } // createAcknowledgement

    /* (non-Javadoc)
     * @see org.nexuse2e.messaging.ProtocolAdapter#createErrorAcknowledge(int, org.nexuse2e.messaging.MessageContext)
     */
    public MessageContext createErrorAcknowledgement( Constants.ErrorMessageReasonCode reasonCode,
            ChoreographyPojo choreography, MessageContext messageContext, Vector<ErrorDescriptor> errorMessages ) {

        //        String currentMessageId = messageContext.getMessagePojo().getMessageKey().getMessageId();
        //        String currentConversationId = messageContext.getMessagePojo().getMessageKey()
        //                .getConversationId();
        //        String currentChoreographyId = messageContext.getMessagePojo().getMessageKey()
        //                .getChoreographyId();
        //        String currentPartnerId = messageContext.getMessagePojo().getMessageKey().getPartnerId();
        //
        //        MessagePojo errrorNotification = new MessagePojo();
        //
        //        errrorNotification.setCreatedDate( DateWrapper.getNOWdatabaseString() );
        //        errrorNotification.setLastModifiedDate( DateWrapper.getNOWdatabaseString() );
        //
        //        errrorNotification.setCommunicationProtocolId( "ebXML" );
        //        errrorNotification.setCommunicationProtocolVersion( "2.0" );
        //
        //        errrorNotification.setAction( messageContext.getMessagePojo().getAction() );
        //        errrorNotification.setMessageType( Constants.MESSAGE_TYPE_ERROR );
        //        errrorNotification.setOutbound( true );
        //
        //        String messageId = null;
        //        try {
        //            messageId = Engine.getInstance().getIdGenerator( "messageId" ).getId();
        //        } catch ( NexusException e ) {
        //            LOG.fatal( "unable to create ErrorMessageId for message:" + currentMessageId );
        //            e.printStackTrace();
        //            messageContext.setMessagePojo( null );
        //            return messageContext;
        //        }
        //
        //        errrorNotification.setMessageKey( new MessageKey( currentChoreographyId, currentConversationId, messageId,
        //                currentPartnerId ) );
        //        errrorNotification.setReferenceMessageId( currentMessageId );
        //
        //        List<CommunicationPartnerPojo> partners = Engine.getInstance().getCurrentConfiguration().getPartners();
        //        Iterator<CommunicationPartnerPojo> i = partners.iterator();
        //        CommunicationPartnerPojo partner = null;
        //        while ( i.hasNext() ) {
        //            CommunicationPartnerPojo tempPartner = i.next();
        //            if ( tempPartner.getPartnerId().equals( currentPartnerId ) ) {
        //                partner = tempPartner;
        //            }
        //        }
        //        if ( partner == null ) {
        //            messageContext.setMessagePojo( null );
        //            LOG.error( "no partner entry found for partnerId:" + currentPartnerId );
        //            return messageContext;
        //        }
        //
        //        errrorNotification.getCustomParameters().put( Constants.PROTOCOLSPECIFIC_TOIDTYPE, partner.getPartnerType() );
        //
        //        String from = null;
        //        String fromIdType = null;
        //        if ( choreography == null ) {
        //            from = messageContext.getMessagePojo().getCustomParameters().get(
        //                    Constants.PROTOCOLSPECIFIC_TO );
        //            fromIdType = messageContext.getMessagePojo().getCustomParameters().get(
        //                    Constants.PROTOCOLSPECIFIC_TOIDTYPE );
        //        } else {
        //            from = choreography.getLocalPartnerId();
        //            fromIdType = choreography.getLocalPartnerIdType();
        //        }
        //        errrorNotification.getCustomParameters()
        //                .put( Constants.PROTOCOLSPECIFIC_FROM, from );
        //        errrorNotification.getCustomParameters().put( Constants.PROTOCOLSPECIFIC_FROMIDTYPE, fromIdType );
        //        errrorNotification.getCustomParameters().put( Constants.PROTOCOLSPECIFIC_SERVICE,
        //                "uri:" + messageContext.getMessagePojo().getAction() );
        //        messageContext.setMessagePojo( errrorNotification );

        return messageContext;
    }

    /* (non-Javadoc)
     * @see org.nexuse2e.messaging.ProtocolAdapter#addProtcolSpecificParameters(org.nexuse2e.messaging.MessageContext)
     */
    public void addProtcolSpecificParameters( MessageContext messageContext ) {

        String localPartnerId = messageContext.getParticipant().getLocalPartner().getPartnerId();
        String localPartnerType = messageContext.getParticipant().getLocalPartner().getPartnerIdType();
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put( Constants.PROTOCOLSPECIFIC_FROM, localPartnerId );
        parameters.put( Constants.PROTOCOLSPECIFIC_FROMIDTYPE, localPartnerType );
        messageContext.getMessagePojo().setCustomParameters( parameters );

    }

    /* (non-Javadoc)
     * @see org.nexuse2e.ProtocolSpecific#getKey()
     */
    public ProtocolSpecificKey getKey() {

        return key;
    }

    /* (non-Javadoc)
     * @see org.nexuse2e.ProtocolSpecific#setKey(org.nexuse2e.ProtocolSpecificKey)
     */
    public void setKey( ProtocolSpecificKey key ) {

        this.key = key;
    }

    /* (non-Javadoc)
     * @see org.nexuse2e.messaging.ProtocolAdapter#createErrorAcknowledgement(org.nexuse2e.messaging.Constants.ErrorMessageReasonCode, org.nexuse2e.pojo.ChoreographyPojo, org.nexuse2e.messaging.MessageContext, java.util.List)
     */
    public MessageContext createErrorAcknowledgement(
            ErrorMessageReasonCode reasonCode, ChoreographyPojo choreography,
            MessageContext messageContext, List<ErrorDescriptor> errorMessages) {

        return null;
    }


    public MessageContext createResponse( MessageContext messageContext )
            throws NexusException {

        // request-response semantics not part of ebXML
        return null;
    }

} // ProtocolAdapter
