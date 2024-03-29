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
package org.nexuse2e.backend.pipelets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nexuse2e.NexusException;
import org.nexuse2e.configuration.ParameterDescriptor;
import org.nexuse2e.configuration.ParameterType;
import org.nexuse2e.logging.LogMessage;
import org.nexuse2e.messaging.AbstractPipelet;
import org.nexuse2e.messaging.MessageContext;
import org.nexuse2e.pojo.MessagePayloadPojo;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * Pipelet for debugging purposes.
 *
 * @author Jonas Reese
 */
public class DebugBackendPipelet extends AbstractPipelet {

    public static final String TEXT_PARAM_NAME = "text";
    public static final String PRINT_PAYLOAD_PARAM_NAME = "printPayload";
    public static final String THROW_EXCEPTION_PARAM_NAME = "throwException";
    public static final String EXCEPTION_MESSAGE_PARAM_NAME = "exceptionMessage";
    private static Logger LOG = LogManager.getLogger(DebugBackendPipelet.class);


    public DebugBackendPipelet() {
        parameterMap.put(TEXT_PARAM_NAME, new ParameterDescriptor(ParameterType.STRING, "Output text",
                "The text to " + "display on the console when a message is processed by this pipelet", ""));
        parameterMap.put(PRINT_PAYLOAD_PARAM_NAME, new ParameterDescriptor(ParameterType.BOOLEAN,
                "Output message " + "payload", "Check if message payload shall be displayed on the console",
                Boolean.FALSE));
        parameterMap.put(THROW_EXCEPTION_PARAM_NAME, new ParameterDescriptor(ParameterType.BOOLEAN, "Throw an " +
                "exception", "Ends the processing by throwing an exception", Boolean.FALSE));
        parameterMap.put(EXCEPTION_MESSAGE_PARAM_NAME, new ParameterDescriptor(ParameterType.STRING, "Exception " +
                "message", "Message text if an exception shall be thrown", ""));
        setFrontendPipelet(false);
    }

    @Override
    public MessageContext processMessage(MessageContext messageContext) throws IllegalArgumentException,
            IllegalStateException, NexusException {
        String s = getParameter(TEXT_PARAM_NAME);
        if (s != null && s.trim().length() > 0) {
            LOG.info(s);
        }
        if (((Boolean) getParameter(PRINT_PAYLOAD_PARAM_NAME)).booleanValue() && messageContext != null && messageContext.getMessagePojo() != null) {
            List<MessagePayloadPojo> list = messageContext.getMessagePojo().getMessagePayloads();
            if (list != null) {
                for (MessagePayloadPojo payload : list) {
                    byte[] data = payload.getPayloadData();
                    LOG.info("Payload " + payload.getContentId() + ", mime-type " + payload.getMimeType());
                    if (data != null) {
                        String encoding = messageContext.getEncoding();
                        try {
                            LOG.info(new LogMessage(new String(data, encoding), messageContext));
                        } catch (UnsupportedEncodingException e) {
                            LOG.warn(new LogMessage("configured payload encoding '" + encoding + "' is not supported"
                                    , messageContext));
                            LOG.info(new LogMessage(new String(data), messageContext)); // use jvm encoding
                        }
                    } else {
                        LOG.info("data is null");
                    }
                }
            }
        }

        if (((Boolean) getParameter(THROW_EXCEPTION_PARAM_NAME)).booleanValue()) {
            throw new NexusException((String) getParameter(EXCEPTION_MESSAGE_PARAM_NAME));
        }

        return messageContext;
    }
}
