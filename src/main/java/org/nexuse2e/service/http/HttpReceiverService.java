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
package org.nexuse2e.service.http;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.nexuse2e.ClusterException;
import org.nexuse2e.Engine;
import org.nexuse2e.NexusException;
import org.nexuse2e.Constants.BeanStatus;
import org.nexuse2e.Constants.Layer;
import org.nexuse2e.configuration.ParameterDescriptor;
import org.nexuse2e.configuration.Constants.ParameterType;
import org.nexuse2e.logging.LogMessage;
import org.nexuse2e.messaging.MessageContext;
import org.nexuse2e.messaging.MessageProcessor;
import org.nexuse2e.messaging.ebxml.v20.Constants;
import org.nexuse2e.pojo.MessagePojo;
import org.nexuse2e.service.AbstractControllerService;
import org.nexuse2e.service.ReceiverAware;
import org.nexuse2e.transport.TransportReceiver;
import org.springframework.web.servlet.ModelAndView;

/**
 * A service that can be used by a <code>TransportReceiver</code> in order
 * to receive messages via HTTP.
 *
 * @author gesch, jonas.reese
 */
public class HttpReceiverService extends AbstractControllerService implements ReceiverAware, MessageProcessor {

    private static Logger      LOG            = Logger.getLogger( HttpReceiverService.class );

    public static final String URL_PARAM_NAME = "logical_name";

    private TransportReceiver  transportReceiver;

    @Override
    public void fillParameterMap( Map<String, ParameterDescriptor> parameterMap ) {

        parameterMap.put( URL_PARAM_NAME, new ParameterDescriptor( ParameterType.STRING, "Logical Name",
                "Logical name that is appended to the URL", "not_defined" ) );
    }

    /* (non-Javadoc)
     * @see org.springframework.web.servlet.mvc.Controller#handleRequest(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @SuppressWarnings("unchecked")
    public ModelAndView handleRequest( HttpServletRequest request, HttpServletResponse response ) throws Exception {

        try {
            LOG.debug( "HTTPService: " + this );
            
            if ( getStatus() != BeanStatus.STARTED ) {
                response.setStatus( HttpServletResponse.SC_SERVICE_UNAVAILABLE );
                return null;
            }
            
            MessageContext messageContext = new MessageContext();
            
            messageContext.setData( getContentFromRequest( request ) );
            if ( LOG.isTraceEnabled() ) {
                LOG.trace( "Inbound message:\n" + new String( (byte[]) messageContext.getData() ) );
            }
            
            messageContext.setMessagePojo( new MessagePojo() );
            messageContext.setOriginalMessagePojo( messageContext.getMessagePojo() );
            messageContext.getMessagePojo().setCustomParameters( new HashMap<String, String>() );
            Enumeration<String> headerNames = request.getHeaderNames();
            while ( headerNames.hasMoreElements() ) {
                String key = headerNames.nextElement();
                String value = request.getHeader( key );
                messageContext.getMessagePojo().getCustomParameters().put(
                        Constants.PARAMETER_PREFIX_HTTP + key.toLowerCase(), value );
                LOG.trace( "  key: " + key + ", value: " + value );
            }

            Enumeration<String> requestParameters = request.getParameterNames();
            while ( requestParameters.hasMoreElements() ) {
                String key = requestParameters.nextElement();
                String value = request.getParameter( key );
                messageContext.getMessagePojo().getCustomParameters().put(
                        Constants.PARAMETER_PREFIX_HTTP_REQUEST_PARAM + key, value );
            }

            try {
                messageContext.getMessagePojo().addCustomParameter("remoteAddr",request.getRemoteAddr() );
                messageContext.getMessagePojo().addCustomParameter("remoteHost",request.getRemoteHost() );
                messageContext.getMessagePojo().addCustomParameter("remotePort",""+request.getRemotePort() );
            } catch ( RuntimeException e ) {
                e.printStackTrace();
            }
            
            processMessage( messageContext );
            LOG.trace( new LogMessage( "Processing Done",messageContext.getMessagePojo()) );

            // PrintWriter out = new PrintWriter( response.getOutputStream() );
            response.setStatus( HttpServletResponse.SC_OK );
            // out.println( "\n" );
            //out.flush();
            //out.close();
        } catch ( ClusterException ce ) {
            ce.printStackTrace();
            LOG.error(ce.getMessage());
            response.setStatus( ce.getResponseCode() );
            
        } catch ( Exception e ) {
        	// print stack trace to console
            e.printStackTrace();
            // prepare the response string (basically the SOAP faultString)
            if ( e.getMessage() == null || e instanceof RuntimeException ) {
            	// createErrorResponse() must not get a message that is null.
                // RuntimeExceptions are usually unexpected. For easier debugging, we include the complete stack trace in the message.
            	StringWriter sw = new StringWriter();
            	e.printStackTrace( new PrintWriter( sw ) );
            	createErrorResponse( request, response, sw.toString() );
            } else {
            	// Usually we will have a NexusException here. Hence we have a meaningful message that can be passed to the sender.
            	// NexusExceptions should always be related to expected errors, so there is no need for a stack trace.
            	createErrorResponse( request, response, e.getMessage() );
            }
        }

        return null;
    }

    /**
     * @param request
     * @return
     * @throws IOException
     */
    public byte[] getContentFromRequest( ServletRequest request ) throws IOException {

        int contentLength = request.getContentLength();
        if ( contentLength < 1 ) {
            throw new IOException( "No payload in HTTP request!" );
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(request.getInputStream(), baos);
        return baos.toByteArray();
    }

    /**
     * @param request
     * @param response
     * @param message
     * @throws IOException
     */
    private void createErrorResponse( HttpServletRequest request, HttpServletResponse response, String message )
            throws IOException {

        if ( ( transportReceiver != null )
                && ( transportReceiver.getKey().getCommunicationProtocolId().equalsIgnoreCase( "ebxml" ) ) ) {
            try {
                SOAPFactory soapFactory = SOAPFactory.newInstance();
                MessageFactory messageFactory = MessageFactory.newInstance();
                SOAPMessage soapMessage = messageFactory.createMessage();
                soapMessage.setProperty( SOAPMessage.WRITE_XML_DECLARATION, "true" );
                SOAPPart soapPart = soapMessage.getSOAPPart();
                SOAPEnvelope soapEnvelope = soapPart.getEnvelope();
                soapEnvelope.addNamespaceDeclaration( "xsi", "http://www.w3.org/2001/XMLSchema-instance" );
                soapEnvelope.addAttribute( soapFactory.createName( "xsi:schemaLocation" ),
                        "http://schemas.xmlsoap.org/soap/envelope/ http://www.oasis-open.org/committees/ebxml-msg/schema/envelope.xsd" );

                SOAPBody soapBody = soapEnvelope.getBody();
                QName faultName = new QName( javax.xml.soap.SOAPConstants.URI_NS_SOAP_ENVELOPE, "Server" );
                SOAPFault soapFault = soapBody.addFault();
                soapFault.setFaultCode( faultName );
                soapFault.setFaultString( (message == null ? "" : message) );
                soapMessage.saveChanges();
                soapMessage.writeTo( response.getOutputStream() );
                response.setContentType( "text/xml" );
            } catch ( Exception e ) {
                LOG.error( "NEXUSe2e - Processing error creating SOAPFault ", e );
                response.sendError( 500, "NEXUSe2e - Processing error creating SOAPFault " + e );
            }
        } else {
            response.sendError( 500, "NEXUSe2e - Processing error: " + message );
        }
    }

    /* (non-Javadoc)
     * @see org.nexuse2e.service.AbstractService#getActivationTime()
     */
    @Override
    public Layer getActivationLayer() {

        return Layer.INBOUND_PIPELINES;
    }

    /* (non-Javadoc)
     * @see org.nexuse2e.Manageable#teardown()
     */
    public void teardown() {

        super.teardown();

        transportReceiver = null;
    } // teardown

    public TransportReceiver getTransportReceiver() {

        return transportReceiver;
    }

    public void setTransportReceiver( TransportReceiver transportReceiver ) {

        this.transportReceiver = transportReceiver;
    }

    public MessageContext processMessage( MessageContext messageContext ) throws IllegalArgumentException,
            IllegalStateException, NexusException {

        if ( transportReceiver != null ) {
            transportReceiver.processMessage( messageContext );
            if ( transportReceiver.getStatus() != BeanStatus.ACTIVATED ) {
                savePaylod( messageContext );
            }
        } else {
            LOG.fatal( "No TransportReceiver available for inbound message!" );
            savePaylod( messageContext );
        }
        return null;
    }

    private void savePaylod( MessageContext messageContext ) {

        String dir = Engine.getInstance().getNexusE2ERoot();
        if ( dir == null ) {
            dir = ".";
        }

        File dirFile = new File( dir );
        if ( !dirFile.isDirectory() ) {
            LOG.error( new LogMessage( "NEXUSe2eRoot not pointing to a directory!",messageContext.getMessagePojo()) );
        }
        File outputDir = new File( dirFile.getAbsolutePath() + "/inbox/notproc" );

        outputDir.mkdirs();

        DateFormat df = new SimpleDateFormat( "yyyyMMddHHmmssSSS" );
        String now = df.format( new Date() );

        File outputFile = new File( outputDir.getAbsolutePath() + "/HttpReceiverService_" + now + ".dat" );

        if ( messageContext.getData() != null ) {
            try {
                FileOutputStream fos = new FileOutputStream( outputFile );
                fos.write( (byte[]) messageContext.getData() );
                fos.flush();
                fos.close();
            } catch ( Exception e ) {
                LOG.error( new LogMessage( "Error saving raw inbound message:" + e,messageContext.getMessagePojo()),e );
            }
        } else {
            LOG.error( new LogMessage( "No raw inbound message data found!",messageContext.getMessagePojo()) );
        }

    }

} // HttpReceiverService
