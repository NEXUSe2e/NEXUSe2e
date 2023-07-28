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
package org.nexuse2e.service.http;

import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nexuse2e.Engine;
import org.nexuse2e.Layer;
import org.nexuse2e.MessageStatus;
import org.nexuse2e.NexusException;
import org.nexuse2e.configuration.CertificateType;
import org.nexuse2e.configuration.ParameterDescriptor;
import org.nexuse2e.configuration.ParameterType;
import org.nexuse2e.logging.LogMessage;
import org.nexuse2e.messaging.FrontendPipeline;
import org.nexuse2e.messaging.MessageContext;
import org.nexuse2e.messaging.ebxml.v20.Constants;
import org.nexuse2e.pojo.CertificatePojo;
import org.nexuse2e.pojo.ChoreographyPojo;
import org.nexuse2e.pojo.MessagePayloadPojo;
import org.nexuse2e.pojo.MessagePojo;
import org.nexuse2e.pojo.ParticipantPojo;
import org.nexuse2e.pojo.TRPPojo;
import org.nexuse2e.service.AbstractService;
import org.nexuse2e.service.SenderAware;
import org.nexuse2e.transport.TransportSender;
import org.nexuse2e.util.CertSSLProtocolSocketFactory;
import org.nexuse2e.util.CertificateUtil;
import org.nexuse2e.util.EncryptionUtil;

import javax.mail.internet.ContentType;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A service that can be used by a <code>TransportSender</code> in order
 * to send messages via HTTP.
 *
 * @author gesch, jonas.reese
 */
public class HttpSenderService extends AbstractService implements SenderAware {

    /**
     * PARAMETERS
     **/
    public static final String PREEMPTIVE_AUTH_PARAM_NAME = "preemptiveAuth";
    public static final String LEGACY_HTTP_HEADER_FOLDING = "httpHeaderFolding";
    private static Logger LOG = LogManager.getLogger(HttpSenderService.class);
    private TransportSender transportSender;

    @Override
    public void fillParameterMap(Map<String, ParameterDescriptor> parameterMap) {
        parameterMap.put(PREEMPTIVE_AUTH_PARAM_NAME, new ParameterDescriptor(ParameterType.BOOLEAN, "Preemptive " +
                "Authentication", "Check, if the HTTP client should use preemtive authentication.", Boolean.FALSE));
        parameterMap.put(LEGACY_HTTP_HEADER_FOLDING, new ParameterDescriptor(ParameterType.BOOLEAN, "Legacy Http " +
                "header Folding", "HTTP Header folding is deprecated. In case of issues, the old behavior can be " +
                "activated.", Boolean.FALSE));
    }

    @Override
    public Layer getActivationLayer() {

        return Layer.OUTBOUND_PIPELINES;
    }

    public TransportSender getTransportSender() {

        return transportSender;
    }

    public void setTransportSender(TransportSender transportSender) {

        this.transportSender = transportSender;
    }

    public MessageContext sendMessage(MessageContext messageContext) throws NexusException {

        ParticipantPojo participant = messageContext.getParticipant();
        int timeout = participant.getConnection().getTimeout() * 1000;
        PostMethod method = null;
        HttpClient client = null;

        MessageContext returnMessageContext = null;
        URL receiverURL = null;
        try {
            receiverURL = new URL(messageContext.getParticipant().getConnection().getUri());
        } catch (MalformedURLException e) {
            throw new NexusException(new LogMessage("Error creating HTTP POST call for: " + messageContext.getParticipant().getConnection().getUri(), messageContext), e);

        }

        try {

            String pwd = messageContext.getParticipant().getConnection().getPassword();
            String user = messageContext.getParticipant().getConnection().getLoginName();
            LOG.debug(new LogMessage("ConnectionURL:" + receiverURL, messageContext.getMessagePojo()));
            client = new HttpClient();
            // TODO: check for https and check isEnforced
            if (receiverURL.toString().toLowerCase().startsWith("https")) {

                LOG.debug("Using SSL");
                Protocol myhttps;
                String tlsProtocols = messageContext.getParticipant().getConnection().getTlsProtocols();
                String tlsCiphers = messageContext.getParticipant().getConnection().getTlsCiphers();

                if (LOG.isTraceEnabled()) {
                    LOG.trace(new LogMessage("participant: " + participant, messageContext.getMessagePojo()));
                    LOG.trace(new LogMessage("participant.name: " + participant.getPartner().getName(),
                            messageContext.getMessagePojo()));
                    LOG.trace(new LogMessage("participant.localcerts: " + participant.getLocalCertificate(),
                            messageContext.getMessagePojo()));
                    if (participant.getLocalCertificate() != null) {
                        LOG.trace(new LogMessage("localcert.name(" + participant.getLocalCertificate().getNxCertificateId() + "): " + participant.getLocalCertificate().getName(), messageContext.getMessagePojo()));
                    }
                }

                CertificatePojo localCert = participant.getLocalCertificate();
                if (localCert == null) {
                    LOG.error(new LogMessage("No local certificate selected for using SSL with partner " + participant.getPartner().getName(), messageContext.getMessagePojo()));
                    throw new NexusException("No local certificate selected for using SSL with partner " + participant.getPartner().getName());
                }

                CertificatePojo partnerCert = participant.getConnection().getCertificate();
                CertificatePojo metaPojo =
                        Engine.getInstance().getActiveConfigurationAccessService().getFirstCertificateByType(CertificateType.CACERT_METADATA.getOrdinal(), true);

                String cacertspwd = "changeit";
                if (metaPojo != null) {
                    cacertspwd = EncryptionUtil.decryptString(metaPojo.getPassword());
                }
                KeyStore privateKeyChain = CertificateUtil.getPKCS12KeyStore(localCert);

                myhttps = new Protocol("https",
                        (ProtocolSocketFactory) new CertSSLProtocolSocketFactory(privateKeyChain,
                                EncryptionUtil.decryptString(localCert.getPassword()),
                                Engine.getInstance().getActiveConfigurationAccessService().getCacertsKeyStore(),
                                cacertspwd, partnerCert, tlsProtocols, tlsCiphers), 443);

                client.getHostConfiguration().setHost(receiverURL.getHost(), receiverURL.getPort(), myhttps);

            } else {
                client.getHostConfiguration().setHost(receiverURL.getHost(), receiverURL.getPort());

            }

            client.getHttpConnectionManager().getParams().setConnectionTimeout(timeout);
            client.getHttpConnectionManager().getParams().setSoTimeout(timeout);
            method = new PostMethod(receiverURL.getPath());
            method.setQueryString(receiverURL.getQuery());
            method.setFollowRedirects(false);
            method.getParams().setSoTimeout(timeout);
            LOG.trace(new LogMessage("Created new NexusHttpConnection with timeout: " + timeout + ", SSL: " + participant.getConnection().isSecure(), messageContext.getMessagePojo()));

            // Use basic auth if credentials are present
            if ((user != null) && (user.length() != 0) && (pwd != null)) {
                Credentials credentials = new UsernamePasswordCredentials(user, pwd);
                LOG.debug(new LogMessage("HTTPBackendConnector: Using basic auth.", messageContext.getMessagePojo()));
                client.getParams().setAuthenticationPreemptive((Boolean) getParameter(PREEMPTIVE_AUTH_PARAM_NAME));
                client.getState().setCredentials(AuthScope.ANY, credentials);
                method.setDoAuthentication(true);
            }
        } catch (Exception e) {
            throw new NexusException(new LogMessage("Error creating HTTP POST call: " + e, e), e);
        }

        try {
            String httpReply = null;

            if (LOG.isTraceEnabled()) {
                LOG.trace(new LogMessage("HTTP Message Data:\n" + (messageContext.getData() == null ? null :
                        new String((byte[]) messageContext.getData())), messageContext));
            }

            // Support for HTTP plain
            TRPPojo trpPojo = messageContext.getMessagePojo().getTRP();
            if (trpPojo.getProtocol().equalsIgnoreCase(org.nexuse2e.Constants.PROTOCOL_ID_EBXML)) {

                String contentTypeString = null;

                Boolean folding = getParameter(LEGACY_HTTP_HEADER_FOLDING);

                if (folding == null || folding) {
                    /* No bug with wrapping the content type, see section 2.2 of http://www.ietf.org/rfc/rfc2616.txt */
                    ContentType contentType = new ContentType("multipart/related");
                    contentType.setParameter("type", "text/xml");
                    contentType.setParameter("boundary", "MIME_boundary");
                    contentType.setParameter("start",
                            messageContext.getMessagePojo().getMessageId() + messageContext.getMessagePojo().getTRP().getProtocol() + "-Header");
                    contentTypeString = contentType.toString();
                } else {
                    contentTypeString =
                            "multipart/related; type=\"text/xml\"; boundary=MIME_boundary; start=" + messageContext.getMessagePojo().getMessageId() + messageContext.getMessagePojo().getTRP().getProtocol() + "-Header";
                }

                RequestEntity requestEntity = new ByteArrayRequestEntity((byte[]) messageContext.getData(), "Content" +
                        "-Type:" + contentTypeString);

                method.setRequestEntity(requestEntity);

                method.setRequestHeader("SOAPAction", "\"ebXML\"");
                method.setRequestHeader("Content-Type", contentTypeString);
            } else if (trpPojo.getProtocol().equalsIgnoreCase(org.nexuse2e.Constants.PROTOCOL_ID_HTTP_PLAIN)) {

                StringBuilder uriParams = new StringBuilder();
                uriParams.append("ChoreographyID=" + messageContext.getMessagePojo().getConversation().getChoreography().getName());
                uriParams.append("&ActionID=" + messageContext.getMessagePojo().getConversation().getCurrentAction().getName());

                ChoreographyPojo choreographyPojo = messageContext.getMessagePojo().getConversation().getChoreography();
                ParticipantPojo participantPojo =
                        Engine.getInstance().getActiveConfigurationAccessService().getParticipantFromChoreographyByNxPartnerId(choreographyPojo, messageContext.getMessagePojo().getConversation().getPartner().getNxPartnerId());
                uriParams.append("&ParticipantID=" + participantPojo.getLocalPartner().getPartnerId());
                uriParams.append("&ConversationID=" + messageContext.getMessagePojo().getConversation().getConversationId());
                uriParams.append("&MessageID=" + messageContext.getMessagePojo().getMessageId());


                String preConfiguredQuery = receiverURL.getQuery();
                String queryString;
                if (StringUtils.isBlank(preConfiguredQuery)) {
                    queryString = uriParams.toString();
                } else {
                    queryString = preConfiguredQuery + "&" + uriParams.toString();
                }
                method.setQueryString(queryString);
                method.setRequestHeader("Content-Type", "text/plain");
                LOG.debug(new LogMessage("URL: " + method.getURI(), messageContext.getMessagePojo()));

                method.setRequestEntity(new ByteArrayRequestEntity((byte[]) messageContext.getData()));
            } else {
                RequestEntity requestEntity = new ByteArrayRequestEntity((byte[]) messageContext.getData(), "text/xml");
                method.setRequestEntity(requestEntity);
            }
            LOG.info("resulting URL:" + client.getHostConfiguration().getHostURL() + method.getURI());
            client.executeMethod(method);
            LOG.debug(new LogMessage("HTTP call done", messageContext.getMessagePojo()));
            int statusCode = method.getStatusCode();
            if (statusCode > 299) {
                LogMessage lm =
                        new LogMessage("Message submission failed, server " + messageContext.getParticipant().getConnection().getUri() + " responded with status: " + statusCode, messageContext.getMessagePojo());
                LOG.error(lm);
                throw new NexusException(lm);
            } else if (statusCode < 200) {
                LOG.warn(new LogMessage("Partner server " + messageContext.getParticipant().getConnection().getUri() + " responded with status: " + statusCode, messageContext.getMessagePojo()));
            }

            boolean processReturn =
                    (transportSender != null && transportSender.getPipeline() instanceof FrontendPipeline && messageContext.isProcessThroughReturnPipeline() && ((FrontendPipeline) transportSender.getPipeline()).getReturnPipelets() != null && ((FrontendPipeline) transportSender.getPipeline()).getReturnPipelets().length > 0);

            if (processReturn || LOG.isTraceEnabled()) {
                byte[] body = method.getResponseBody();

                if (LOG.isTraceEnabled()) {
                    httpReply = getHTTPReply(body, statusCode);
                    LOG.trace(new LogMessage("Retrieved HTTP response\n:" + httpReply, messageContext.getMessagePojo()));
                }

                Header[] responseHeaders = method.getResponseHeaders();
                Map<String, String> customParameters = new HashMap<>();
                for (Header header : responseHeaders) {
                    customParameters.put(Constants.PARAMETER_PREFIX_HTTP + header.getName().toLowerCase(), header.getValue());
                    LOG.trace(new LogMessage("Response header - key: " + header.getName() + ", value: " + header.getValue(), messageContext.getMessagePojo()));
                }

                if (processReturn) {
                    MessagePojo message = (MessagePojo) messageContext.getMessagePojo().clone();
                    message.setOutbound(false);
                    message.setStatus(MessageStatus.UNKNOWN.getOrdinal());
                    message.setEndDate(null);
                    message.setRetries(0);
                    message.setMessageId(null);
                    message.getCustomParameters().putAll(customParameters);
                    // important: Payload needs to be reset, shall be set from data field by pipeline processing
                    message.setMessagePayloads(new ArrayList<MessagePayloadPojo>());
                    returnMessageContext = Engine.getInstance().getTransactionService().createMessageContext(message);
                    returnMessageContext.setData(body);
                    returnMessageContext.setOriginalMessagePojo(messageContext.getMessagePojo());
                }
            }

            method.releaseConnection();

        } catch (ConnectTimeoutException e) {
            LogMessage lm =
                    new LogMessage("Message submission failed, connection timeout for URL: " + messageContext.getParticipant().getConnection().getUri(), messageContext.getMessagePojo(), e);
            LOG.warn(lm, e);
            throw new NexusException(lm, e);
        } catch (Exception ex) {
            LogMessage lm =
                    new LogMessage("Message submission failed for URL: " + messageContext.getParticipant().getConnection().getUri(), messageContext.getMessagePojo(), ex);
            LOG.warn(lm, ex);
            throw new NexusException(lm, ex);
        }

        return returnMessageContext;
    }

    /**
     * Retrieve a reply from an HTTP message post.
     *
     * @returns String
     */
    public String getHTTPReply(byte[] responseBody, int responseCode) throws NexusException {

        StringBuffer reply = new StringBuffer();

        if (responseBody != null) {
            reply.append(new String(responseBody));
            reply.append("\n");
        }
        reply.append("HTTP Response Code:  " + responseCode);

        return reply.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.nexuse2e.Manageable#teardown()
     */
    public void teardown() {

        super.teardown();

        transportSender = null;
    } // teardown

}
