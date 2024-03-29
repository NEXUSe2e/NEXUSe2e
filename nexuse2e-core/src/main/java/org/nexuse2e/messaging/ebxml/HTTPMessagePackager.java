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
package org.nexuse2e.messaging.ebxml;


import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nexuse2e.Engine;
import org.nexuse2e.configuration.ListParameter;
import org.nexuse2e.configuration.ParameterDescriptor;
import org.nexuse2e.configuration.ParameterType;
import org.nexuse2e.logging.LogMessage;
import org.nexuse2e.messaging.AbstractPipelet;
import org.nexuse2e.messaging.BinaryEncoding;
import org.nexuse2e.messaging.MessageContext;
import org.nexuse2e.pojo.MessageLabelPojo;
import org.nexuse2e.pojo.MessagePayloadPojo;
import org.nexuse2e.pojo.MessagePojo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.mail.internet.ParseException;
import javax.xml.soap.SOAPException;


/**
 * @author gesch, sschulze
 */
public class HTTPMessagePackager extends AbstractPipelet {
    private static final String ENCODING_PARAMETER_NAME = "binary_encoding";
    private static final String CONTENT_DISPOSITION_PARAMETER = "content_disposition";
    private static Logger LOG = LogManager.getLogger(HTTPMessagePackager.class);
    private static String CRLF = "\r\n";
    private static BinaryEncoding encoding = BinaryEncoding.BASE64;
    private static Boolean contentDisposition = false;
    private static boolean trimContent = false;

    /**
     * Default constructor.
     */
    public HTTPMessagePackager() {
        ListParameter paramList = new ListParameter();
        paramList.addElement(BinaryEncoding.BASE64.getName(), BinaryEncoding.BASE64.toString());
        paramList.addElement(BinaryEncoding.BINARY.getName(), BinaryEncoding.BINARY.toString());
        paramList.setSelectedIndex(0);
        parameterMap.put(ENCODING_PARAMETER_NAME, new ParameterDescriptor(ParameterType.LIST, "Binary encoding",
                "Which encoding to use for binary content", paramList));
        parameterMap.put(CONTENT_DISPOSITION_PARAMETER, new ParameterDescriptor(ParameterType.BOOLEAN, "Include " +
                "content disposition", "If binary encoding is used, the content disposition will be added to the " +
                "message.", Boolean.FALSE));
        frontendPipelet = true;
    }

    /**
     * Returns a JAX SOAP message for a given message pojo.
     *
     * @param messagePojo The {@link MessagePojo} for which to generate the message.
     * @return A byte[] containing the message.
     * @throws SOAPException
     * @throws ParseException
     * @throws IOException
     */
    private static byte[] getJAXMSOAPMsg(MessagePojo messagePojo) throws SOAPException, ParseException, IOException {

        String soapId = getSOAPId(messagePojo);
        String hdrContentId = "Content-ID: " + soapId;
        // TODO (encoding) which encoding is used for headers ?
        String ebXMLHeader = new String(messagePojo.getHeaderData());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StringBuffer msgBuffer = new StringBuffer();
        msgBuffer.append(Constants.MIMEPARTBOUNDARY + CRLF);
        // Add ebXML Header
        msgBuffer.append(hdrContentId + CRLF);
        msgBuffer.append(Constants.HDRCONTENTTYPE + CRLF + CRLF);
        msgBuffer.append(ebXMLHeader + CRLF + CRLF);

        if (messagePojo.getMessagePayloads() != null && messagePojo.getMessagePayloads().size() > 0) {

            for (MessagePayloadPojo payloadPojo : messagePojo.getMessagePayloads()) {
                // add bodyparts
                msgBuffer.append(Constants.MIMEPARTBOUNDARY + CRLF);

                // Cut Id from content if binary and trim parameter set
                if (trimContent && payloadPojo.getContentId() != null && payloadPojo.getContentId().trim().length() > 0) {
                    payloadPojo.setContentId(payloadPojo.getContentId().replaceAll("__body_\\d+", ""));
                }

                // Add content disposition if content disposition parameter is set
                if (contentDisposition != null && contentDisposition && payloadPojo.getContentId() != null && payloadPojo.getContentId().trim().length() > 0) {
                    msgBuffer.append("Content-Disposition: attachment; filename=\"" + payloadPojo.getContentId() +
                            "\"" + CRLF);
                    msgBuffer.append("Content-Transfer-Encoding: " + encoding.toString().toLowerCase() + CRLF);
                }

                // String payloadContentID = "Content-ID: " + "<" + getContentId( messagePojo.getMessageId(),
                // payloadPojo.getSequenceNumber() ) + ">";
                // MBE: Changed 20100215 due to interop problem
                String payloadContentID = "Content-ID: " + "<" + payloadPojo.getContentId() + ">";

                msgBuffer.append(payloadContentID + CRLF);
                msgBuffer.append("Content-Type: " + payloadPojo.getMimeType() + CRLF + CRLF);

                msgBuffer = flushToBytes(msgBuffer, baos);

                if (Engine.getInstance().isBinaryType(payloadPojo.getMimeType())) {
                    LOG.trace(new LogMessage("Using binary for mime type: " + payloadPojo.getMimeType()));
                    switch (encoding) {
                        case BINARY:
                            baos.write(payloadPojo.getPayloadData());
                            break;
                        case UNSUPPORTED:
                            LOG.warn(new LogMessage(String.format("BinaryEncoding.UNSUPPORTED found as target " +
                                    "encoding while processing message: %s - will use base64 instead, please " +
                                    "double-check your results", payloadPojo.getNxMessagePayloadId())));
                        default:
                            baos.write(Base64.encodeBase64(payloadPojo.getPayloadData()));
                    }
                } else {
                    baos.write(payloadPojo.getPayloadData());
                }

                msgBuffer.append(CRLF);
            }
        }

        msgBuffer.append(Constants.MIMEPACKBOUNDARY + CRLF);

        // flush to bytes
        flushToBytes(msgBuffer, baos);

        return baos.toByteArray();
    }

    /**
     * Returns an ack message
     *
     * @param messagePojo
     * @return
     * @throws ParseException
     */
    private static byte[] getJAXMSOAPAck(MessagePojo messagePojo) throws ParseException {

        String soapId = getSOAPId(messagePojo);
        String hdrContentId = "Content-ID: " + soapId;
        // TODO (encoding) which encoding is used for headers ?
        String ackHeader = new String(messagePojo.getHeaderData());

        StringBuffer ackBuffer = new StringBuffer();
        ackBuffer.append(Constants.MIMEPARTBOUNDARY + CRLF);
        ackBuffer.append(hdrContentId + CRLF);
        ackBuffer.append(Constants.HDRCONTENTTYPE + CRLF + CRLF);
        ackBuffer.append(ackHeader + CRLF + CRLF);
        ackBuffer.append(Constants.MIMEPACKBOUNDARY);

        return ackBuffer.toString().getBytes();
    }

    /**
     * Flushes {@link StringBuffer} to {@link java.io.ByteArrayOutputStream} and returns a new empty
     * {@link StringBuffer}.
     *
     * @param sb   must not be <code>null</code>
     * @param baos must not be <code>null</code>
     * @return a new empty {@link StringBuffer}.
     * @throws IOException
     */
    private static StringBuffer flushToBytes(StringBuffer sb, java.io.ByteArrayOutputStream baos) throws IOException {
        baos.write(sb.toString().getBytes());
        return new StringBuffer();
    }

    /**
     * Retrieve a soap/mime contentId.
     */
    private static String getSOAPId(MessagePojo nexusMsg) {

        String soapID = "<" + nexusMsg.getMessageId() + nexusMsg.getTRP().getProtocol() + "-Header" + ">";
        return soapID;
    }

    /**
     * Retrieve standard BodyPart content-Id based on it's position in the Enumeration (message ID + "-body" +
     * position).
     *
     * @param position Position of the BodyPart relative to 1.
     * @return conentId based on the BodyPart position parameter.
     */
    public static String getContentId(String id, int position) {

        String contentId = null;
        contentId = id + "-body" + (position);
        return contentId;
    }

    /* (non-Javadoc)
     * @see org.nexuse2e.messaging.MessagePipelet#processMessage(org.nexuse2e.messaging.MessageContext)
     */
    public MessageContext processMessage(MessageContext messageContext) {

        ListParameter listParam = getParameter(ENCODING_PARAMETER_NAME);
        if (listParam == null) {
            LOG.trace("You are using a HTTPMessagePackager without having set the binary encoding option. The most " +
                    "likely cause is that you recently upgraded your NEXUSe2e and did not have this configuration " +
                    "option before. Will continue with Base 64 encoding for backwards compatibility; to get rid of " +
                    "this warning please configure your HTTPMessagePackager Pipelets and set the binary encoding " +
                    "option, most likely to Base 64.");
        } else {
            String selectedEncoding = listParam.getSelectedValue();
            if (StringUtils.isNotBlank(selectedEncoding)) {
                encoding = BinaryEncoding.fromString(selectedEncoding);
                contentDisposition = getParameter(CONTENT_DISPOSITION_PARAMETER);
                if (contentDisposition == null) {
                    contentDisposition = false;
                }
                LOG.trace("Content disposition is activated.");
                List<MessageLabelPojo> messageLabels = messageContext.getMessagePojo().getMessageLabels();
                boolean includeLabels = (messageLabels != null) && (messageLabels.size() != 0);
                if (includeLabels) {
                    for (MessageLabelPojo label : messageLabels) {
                        if (label.getLabel().equals("trimContId")) {
                            trimContent = Boolean.valueOf(label.getValue().toString());
                            LOG.trace("Payload sequence number will be deleted from Content-Id.");
                        }
                    }
                }
            }
        }

        byte[] serializedSOAPMessage = null;
        MessagePojo messagePojo = messageContext.getMessagePojo();
        LOG.debug(new LogMessage("Entering HTTPMessagePackager.processMessage...", messagePojo));

        try {
            if (messagePojo.getType() == org.nexuse2e.messaging.Constants.INT_MESSAGE_TYPE_ACK) {
                serializedSOAPMessage = getJAXMSOAPAck(messagePojo);
            } else {
                serializedSOAPMessage = getJAXMSOAPMsg(messagePojo);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new IllegalArgumentException(ex.toString());
        }

        if (serializedSOAPMessage != null) {
            messageContext.setData(serializedSOAPMessage);
            //LOG.trace( new String(messageContext.getData()) );
        } else {
            throw new IllegalArgumentException("unable to create SOAPMessage");
        }

        return messageContext;
    }

    public void afterPropertiesSet() throws Exception {
    }
}
