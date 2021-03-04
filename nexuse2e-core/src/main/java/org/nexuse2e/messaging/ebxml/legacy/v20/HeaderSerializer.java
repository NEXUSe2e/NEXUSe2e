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
package org.nexuse2e.messaging.ebxml.legacy.v20;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.apache.log4j.Logger;
import org.nexuse2e.Constants.Severity;
import org.nexuse2e.Engine;
import org.nexuse2e.NexusException;
import org.nexuse2e.logging.LogMessage;
import org.nexuse2e.messaging.AbstractPipelet;
import org.nexuse2e.messaging.ErrorDescriptor;
import org.nexuse2e.messaging.MessageContext;
import org.nexuse2e.messaging.TimestampFormatter;
import org.nexuse2e.messaging.ebxml.v20.Constants;
import org.nexuse2e.pojo.MessagePayloadPojo;
import org.nexuse2e.pojo.MessagePojo;

public class HeaderSerializer extends AbstractPipelet {

    private static Logger LOG = Logger.getLogger( HeaderSerializer.class );
    
    /**
     * Default constructor.
     */
    public HeaderSerializer() {

        frontendPipelet = true;
    }

    /**
     * 
     */
    public MessageContext processMessage( MessageContext messageContext ) throws NexusException {

        try {
            MessagePojo messagePojo = messageContext.getMessagePojo();

            // Test for re-send of ack
            if ( ( messagePojo.getType() == Constants.INT_MESSAGE_TYPE_ACK ) && ( messagePojo.getHeaderData() != null )
                    && ( messagePojo.getHeaderData().length != 0 ) ) {
                LOG.debug( "Re-send of acknowledgment - using existing header." );
            } else {

                SOAPFactory soapFactory = SOAPFactory.newInstance();
                MessageFactory messageFactory = MessageFactory.newInstance();

                TimestampFormatter formatter = Engine.getInstance().getTimestampFormatter( "ebxml" );
                String createdDate;
                try {
                    Date createdDateObject = messagePojo.getCreatedDate();
                    createdDate = formatter.getTimestamp( createdDateObject );
                } catch ( Exception e ) {
                    throw new NexusException( "error while processing createdDate field:"
                            + messagePojo.getCreatedDate(), e );
                }

                LOG.debug( new LogMessage("Messgae Factory: " + messageFactory.getClass().getCanonicalName(),messagePojo) );
                SOAPMessage soapMessage = messageFactory.createMessage();
                soapMessage.setProperty( SOAPMessage.WRITE_XML_DECLARATION, "true" );
                SOAPPart soapPart = soapMessage.getSOAPPart();
                SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
                SOAPBody soapBody = soapEnvelope.getBody();
                SOAPHeader soapHeader = soapEnvelope.getHeader();
                SOAPElement soapElement = null;
                Name name = null;
                boolean ack = false;
                boolean error = false;
                if ( messagePojo.getType() == org.nexuse2e.messaging.Constants.INT_MESSAGE_TYPE_ACK ) {
                    ack = true;
                }
                if ( messagePojo.getType() == org.nexuse2e.messaging.Constants.INT_MESSAGE_TYPE_ERROR ) {
                    error = true;
                }

                // ENVELOPE ATTRS ------------------------------------------------------
                //  namespace attributes, 'soap-env' namespace handled by JAXM
                soapEnvelope.addNamespaceDeclaration( Constants.EBXML_NAMESPACE_PREFIX, Constants.EBXML_NAMESPACE );
                soapEnvelope.addNamespaceDeclaration( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );
                soapEnvelope.addNamespaceDeclaration( "xlink", "http://www.w3.org/1999/xlink" );
                soapEnvelope
                        .addAttribute( soapFactory.createName( "xsi:schemaLocation" ),
                                "http://schemas.xmlsoap.org/soap/envelope/ http://www.oasis-open.org/committees/ebxml-msg/schema/envelope.xsd" );
                // HEADER ATTRS --------------------------------------------------------
                soapHeader
                        .addAttribute(
                                soapFactory.createName( "xsi:schemaLocation" ),
                                "http://www.oasis-open.org/committees/ebxml-msg/schema/msg-header-2_0.xsd http://www.oasis-open.org/committees/ebxml-msg/schema/msg-header-2_0.xsd" );
                //soapHeader.addAttribute( soapFactory.createName( "xmlns:eb" ),
                //        "http://www.oasis-open.org/committees/ebxml-msg/schema/msg-header-2_0.xsd" );

                // BODY ATTRS ----------------------------------------------------------
                // soapBody.addAttribute(soapFactory.createName( "xsi:schemaLocation" ),"http://www.oasis-open.org/committees/ebxml-msg/schema/msg-header-2_0.xsd http://www.oasis-open.org/committees/ebxml-msg/schema/msg-header-2_0.xsd" );
                // soapBody.addAttribute( soapFactory.createName( "xmlns:eb" ),"http://www.oasis-open.org/committees/ebxml-msg/schema/msg-header-2_0.xsd" );

                // MESSAGE HEADER ------------------------------------------------------
                name = soapFactory.createName( "MessageHeader", Constants.EBXML_NAMESPACE_PREFIX,
                        Constants.EBXML_NAMESPACE );
                SOAPHeaderElement msgHeader = soapHeader.addHeaderElement( name );
                msgHeader.setMustUnderstand( true );
                msgHeader.addAttribute( soapFactory.createName( Constants.VERSION, Constants.EBXML_NAMESPACE_PREFIX,
                        Constants.EBXML_NAMESPACE ), Constants.EBXMLVERSION );

                // TO & FROM -----------------------------------------------------------
                String from = messagePojo.getParticipant().getLocalPartner().getPartnerId();
                if ( from == null ) {
                    //TODO: for testing..
                    from = "dummyfrom";
                }
                String fromIdType = messagePojo.getParticipant().getLocalPartner().getPartnerIdType();
                ;
                if ( fromIdType == null ) {
                    //              TODO: for testing..
                    fromIdType = "dummyFromType";
                }
                String to = messagePojo.getConversation().getPartner().getPartnerId();
                if ( to == null ) {
                    //              TODO: for testing..
                    to = "dummyto";
                }
                String toIDType = messagePojo.getConversation().getPartner().getPartnerIdType();
                if ( toIDType == null ) {
                    //              TODO: for testing..
                    toIDType = "dummytoType";
                }
                msgHeader.addChildElement( createPartyElement( soapFactory, "From", from, fromIdType, null ) );
                msgHeader.addChildElement( createPartyElement( soapFactory, "To", to, toIDType, null ) );

                // CPA & ConversationId ------------------------------------------------
                createSOAPElement( soapFactory, msgHeader, "CPAId", messagePojo.getConversation().getChoreography()
                        .getName() );
                createSOAPElement( soapFactory, msgHeader, "ConversationId", messagePojo.getConversation()
                        .getConversationId() );

                // SERVICE -------------------------------------------------------------
                //  service is hard coded to  meet spec.  Services are not used.
                String service = messagePojo.getCustomParameters().get(
                        Constants.PARAMETER_PREFIX_EBXML20 + Constants.PROTOCOLSPECIFIC_SERVICE );
                if ( service == null ) {
                    service = messagePojo.getConversation().getChoreography().getName();
                }
                if ( ( messagePojo.getType() == org.nexuse2e.messaging.Constants.INT_MESSAGE_TYPE_ACK )
                        || ( messagePojo.getType() == org.nexuse2e.messaging.Constants.INT_MESSAGE_TYPE_ERROR ) ) {
                    service = "urn:oasis:names:tc:ebxml-msg:service";
                }
                String serviceVal = "";
                if ( !( service.startsWith( "uri:" ) || service.startsWith( "urn:" ) ) ) {
                    serviceVal += "uri:";
                }

                createSOAPElement( soapFactory, msgHeader, "Service", serviceVal + service );

                // ACTION --------------------------------------------------------------

                String actionName = null;
                if ( messagePojo.getType() == org.nexuse2e.messaging.Constants.INT_MESSAGE_TYPE_ACK ) {
                    actionName = "Acknowledgment";
                } else if ( messagePojo.getType() == org.nexuse2e.messaging.Constants.INT_MESSAGE_TYPE_ERROR ) {
                    actionName = "MessageError";

                } else {
                    actionName = messagePojo.getAction().getName();
                }

                createSOAPElement( soapFactory, msgHeader, "Action", actionName );

                // MESSAGE DATA --------------------------------------------------------
                SOAPElement msgDataEl = soapFactory.createElement( "MessageData", Constants.EBXML_NAMESPACE_PREFIX,
                        Constants.EBXML_NAMESPACE );
                msgDataEl.removeNamespaceDeclaration( Constants.EBXML_NAMESPACE_PREFIX );

                // MESSAGEDATA MESSAGE ID ----------------------------------------------
                createSOAPElement( soapFactory, msgDataEl, "MessageId", messagePojo.getMessageId() );

                // MESSAGEDATA TIMESTAMP -----------------------------------------------
                createSOAPElement( soapFactory, msgDataEl, Constants.TIMESTAMP_ID, createdDate );

                if ( ack || error ) {
                    // MESSAGEDATA REFTOMESSAGE ID ----------------------------------------------
                    createSOAPElement( soapFactory, msgDataEl, Constants.REFTOMESSAGE_ID, messagePojo
                            .getReferencedMessage().getMessageId() );
                }

                msgHeader.addChildElement( msgDataEl );

                if ( ack ) { // ack
                    // ACKNOWLEDGEMENT--------------------------------------------------
                    createAck( soapFactory, soapHeader, createdDate, messagePojo.getReferencedMessage().getMessageId(),
                            from, fromIdType );
                } else if ( error ) { // error
                    createErrorList( soapFactory, soapHeader, messagePojo.getReferencedMessage().getMessageId(),
                            messageContext.getErrors() );
                } else { // regular message
                    // QUALITY OF SERVICE---------------------------------------------------
                    if ( messagePojo.getParticipant().getConnection().isReliable() ) {
                        name = soapFactory.createName( "AckRequested", Constants.EBXML_NAMESPACE_PREFIX,
                                Constants.EBXML_NAMESPACE );
                        SOAPHeaderElement ackReq = soapHeader.addHeaderElement( name );
                        ackReq.setMustUnderstand( true );
                        ackReq.addAttribute( soapFactory.createName( Constants.VERSION,
                                Constants.EBXML_NAMESPACE_PREFIX, Constants.EBXML_NAMESPACE ), Constants.EBXMLVERSION );
                        ackReq.addAttribute( soapFactory.createName( "signed", Constants.EBXML_NAMESPACE_PREFIX,
                                Constants.EBXML_NAMESPACE ), Constants.ACKREQUESTED_UNSIGNED );
                        soapElement = msgHeader.addChildElement( "DuplicateElimination",
                                Constants.EBXML_NAMESPACE_PREFIX, Constants.EBXML_NAMESPACE );

                        msgHeader.addChildElement( soapElement );
                    }

                    SOAPElement soapManifest = null;

                    // MANIFEST --------------------------------------------------------
                    name = soapFactory.createName( "Manifest", Constants.EBXML_NAMESPACE_PREFIX,
                            Constants.EBXML_NAMESPACE );
                    soapManifest = soapBody.addBodyElement( name );

                    // soapManifest.addAttribute( soapFactory.createName( Constants.MUSTUNDERSTAND,Constants.EBXML_NAMESPACE_PREFIX, Constants.EBXML_NAMESPACE ),Constants.MUSTUNDERSTAND_VALUE );
                    soapManifest.addAttribute( soapFactory.createName( Constants.VERSION,
                            Constants.EBXML_NAMESPACE_PREFIX, Constants.EBXML_NAMESPACE ), Constants.EBXMLVERSION );
                    soapManifest.addAttribute( soapFactory.createName( "id", Constants.EBXML_NAMESPACE_PREFIX,
                            Constants.EBXML_NAMESPACE ), "Manifest" );

                    // REFERENCES ------------------------------------------------------

                    // newMsg.addManifestEntry( newMsg.getMessageID() + "-body" + ( i + 1 ) );

                    for (MessagePayloadPojo bodyPart : messagePojo.getMessagePayloads()) {
                        LOG.trace(new LogMessage("ContentID:" + bodyPart.getContentId(), messagePojo));
                        createManifestReference( soapFactory, soapManifest, bodyPart.getContentId(), "Payload-" + bodyPart.getSequenceNumber(), bodyPart.getMimeType(), null );
                        // MBE: Changed 20100215 due to interop problem - 20103007: changed back
                        // createManifestReference( soapFactory, soapManifest, "cid:" + bodyPart.getContentId(), "Payload-" + bodyPart.getSequenceNumber(), bodyPart.getMimeType(), null );
                    }

                }
                soapMessage.saveChanges();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                soapMessage.writeTo( baos );
                messagePojo.setHeaderData( baos.toByteArray() );
            } // Test for re-send of ack
            LOG.trace( new LogMessage( "Message:" + new String( messagePojo.getHeaderData() ) ,messagePojo));
        } catch ( NexusException e ) {
            throw e;
        } catch ( Exception e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return messageContext;
    }

    /**
     * Creates acknowledgement elements.
     * 
     * @param soapFactory
     * @param soapHeader
     * @param timestamp
     * @param refMessageId
     * @param from
     * @param FromIDType
     * @return
     * @throws SOAPException
     */
    private SOAPElement createAck( SOAPFactory soapFactory, SOAPHeader soapHeader, String timestamp,
            String refMessageId, String from, String FromIDType ) throws SOAPException {

        Name name = soapFactory.createName( "Acknowledgment", Constants.EBXML_NAMESPACE_PREFIX,
                Constants.EBXML_NAMESPACE );
        SOAPHeaderElement ackEl = soapHeader.addHeaderElement( name );
        ackEl.setActor( Constants.SOAPACTOR );
        ackEl.setMustUnderstand( true );
        ackEl.addAttribute( soapFactory.createName( Constants.VERSION, Constants.EBXML_NAMESPACE_PREFIX,
                Constants.EBXML_NAMESPACE ), Constants.EBXMLVERSION );

        // TIMESTAMP------------------------------------------------------------
        createSOAPElement( soapFactory, ackEl, Constants.TIMESTAMP_ID, timestamp );

        // RefToMessageId-------------------------------------------------------
        createSOAPElement( soapFactory, ackEl, Constants.REFTOMESSAGE_ID, refMessageId );

        // FROM-----------------------------------------------------------------
        ackEl.addChildElement( createPartyElement( soapFactory, "From", from, FromIDType, null ) );

        return ackEl;
    }

    /**
     * Creates errorList element
     * 
     * @param soapFactory
     * @param soapHeader
     * @param timestamp
     * @param refMessageId
     * @param from
     * @param FromIDType
     * @return
     * @throws SOAPException
     */
    /*  
     <eb:ErrorList eb:id="3490sdo", eb:highestSeverity="error" eb:version="2.0" SOAP:mustUnderstand="1">    
     <eb:Error eb:errorCode="SecurityFailure" eb:severity="Error" eb:location="URI_of_ds:Signature"> 
     <eb:Description xml:lang="en-US">Validation of signature failed<eb:Description> 
     </eb:Error> 
     <eb:Error ...> ... </eb:Error> 
     </eb:ErrorList> 
     */
    private SOAPElement createErrorList( SOAPFactory soapFactory, SOAPHeader soapHeader, String refMessageId,
            List<ErrorDescriptor> errors ) throws SOAPException {

        Name name = soapFactory.createName( "ErrorList", Constants.EBXML_NAMESPACE_PREFIX, Constants.EBXML_NAMESPACE );
        SOAPHeaderElement errorListEl = soapHeader.addHeaderElement( name );
        //errorListEl.setActor( Constants.SOAPACTOR );
        errorListEl.setMustUnderstand( true );
        errorListEl.addAttribute( soapFactory.createName( Constants.EBXML_NAMESPACE_PREFIX + ":" + Constants.VERSION ),
                Constants.EBXMLVERSION );
        errorListEl.addAttribute( soapFactory.createName( Constants.EBXML_NAMESPACE_PREFIX + ":id" ), "unknown" );
        Severity severity = Severity.INFO;
        String highestSeverity = "nothing";
        if ( errors != null ) {
            Iterator<ErrorDescriptor> i = errors.iterator();
            while ( i.hasNext() ) {
                ErrorDescriptor ed = i.next();
                if ( ed.getSeverity() != null && ed.getSeverity().ordinal() > severity.ordinal() ) {
                    severity = ed.getSeverity();
                    highestSeverity = ed.getSeverity().name();
                }
            }
        }

        errorListEl.addAttribute( soapFactory.createName( Constants.EBXML_NAMESPACE_PREFIX + ":highestSeverity" ),
                highestSeverity );

        if ( errors != null ) {
            Iterator<ErrorDescriptor> i = errors.iterator();
            while ( i.hasNext() ) {
                ErrorDescriptor ed = i.next();
                SOAPElement soapEl = errorListEl.addChildElement( "Error", Constants.EBXML_NAMESPACE_PREFIX,
                        Constants.EBXML_NAMESPACE );
                soapEl.addAttribute( soapFactory.createName( Constants.EBXML_NAMESPACE_PREFIX + ":errorCode" ), ""
                        + ed.getErrorCode() );
                severity = ed.getSeverity();

                soapEl.addAttribute( soapFactory.createName( Constants.EBXML_NAMESPACE_PREFIX + ":severity" ), severity
                        .name() );
                soapEl.addAttribute( soapFactory.createName( Constants.EBXML_NAMESPACE_PREFIX + ":location" ), ed
                        .getLocation() );

                SOAPElement soapElDesc = soapEl.addChildElement( "Description", Constants.EBXML_NAMESPACE_PREFIX,
                        Constants.EBXML_NAMESPACE );
                soapElDesc.addAttribute( soapFactory.createName( "xml:lang" ), "en-US" );
                soapElDesc.addTextNode( "" + ed.getDescription() );
            }
        }

        return errorListEl;
    }

    /**
     *  Takes a reference object and populates a SOAPelement.
     *
     */
    private void createManifestReference( SOAPFactory soapFactory, SOAPElement parent, String href, String id,
            String type, String role ) throws SOAPException {

        SOAPElement soapEl = parent.addChildElement( "Reference", Constants.EBXML_NAMESPACE_PREFIX,
                Constants.EBXML_NAMESPACE );
        soapEl.addAttribute( soapFactory.createName( "xmlns:xlink" ), "http://www.w3.org/1999/xlink" );

        // soapEl.addAttribute( soapFactory.createName( "id", Constants.EBXML_NAMESPACE_PREFIX, Constants.EBXML_NAMESPACE ), id );
        soapEl.addAttribute( soapFactory.createName( Constants.EBXML_NAMESPACE_PREFIX + ":id" ), id );
        soapEl.addAttribute( soapFactory.createName( "xlink:href" ), href );

        if ( role != null && role.length() != 0 ) {
            soapEl.addAttribute( soapFactory.createName( "xlink:role" ), role );
        }

        if ( type != null && type.length() != 0 ) {
            soapEl.addAttribute( soapFactory.createName( "xlink:type" ), "simple" ); // hard coded according to spec, p.23, section 3.2.1
        }
    }

    /**
     * convenience routine for adding text nodes to soap elements.
     *
     */
    private void createSOAPElement( SOAPFactory soapFactory, SOAPElement parent, String childName, String childText )
            throws SOAPException {

        // LOG.trace( "createSOAPElement: " + childName );
        SOAPElement soapEl = parent.addChildElement( childName, Constants.EBXML_NAMESPACE_PREFIX,
                Constants.EBXML_NAMESPACE );
        soapEl.removeNamespaceDeclaration( Constants.EBXML_NAMESPACE_PREFIX );
        soapEl.addTextNode( childText );
    }

    /**
     * Create an ebXML Party element
     * @param id
     * @param value
     * @param type
     * @param location
     * @return
     * @throws SOAPException
     */
    private SOAPElement createPartyElement( SOAPFactory soapFactory, String id, String value, String type,
            String location ) throws SOAPException {

        String party = null;
        SOAPElement soapEl = soapFactory
                .createElement( id, Constants.EBXML_NAMESPACE_PREFIX, Constants.EBXML_NAMESPACE );
        soapEl.removeNamespaceDeclaration( Constants.EBXML_NAMESPACE_PREFIX );
        SOAPElement partyId = soapEl.addChildElement( Constants.PARTY_ID, Constants.EBXML_NAMESPACE_PREFIX,
                Constants.EBXML_NAMESPACE );
        partyId.removeNamespaceDeclaration( Constants.EBXML_NAMESPACE_PREFIX );

        if ( type != null && !type.equals( "" ) ) {
            partyId.addAttribute( soapFactory.createName( Constants.EBXML_NAMESPACE_PREFIX + ":type" ), type );
            party = value;
        } else { // as per ebXML 1.0 spec, if no type attr, value is a uri
            if ( ( value.startsWith( Constants.URI_ID ) == false ) && ( value.indexOf( ":" ) == -1 ) ) {
                party = "uri:" + value;
            } else {
                party = value;
            }
        }

        partyId.addTextNode( party );
        // soapEl.addChildElement( partyId );

        if ( location != null ) {
            createSOAPElement( soapFactory, soapEl, Constants.LOCATION_ID, location );
        }

        return soapEl;
    } // createPartyElement

    public void afterPropertiesSet() throws Exception {

        // TODO Auto-generated method stub

    }

}
