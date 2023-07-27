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
package org.nexuse2e.messaging.generic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nexuse2e.MessageStatus;
import org.nexuse2e.NexusException;
import org.nexuse2e.logging.LogMessage;
import org.nexuse2e.messaging.AbstractPipelet;
import org.nexuse2e.messaging.MessageContext;
import org.nexuse2e.messaging.ProtocolAdapter;
import org.nexuse2e.pojo.MessagePojo;

public class AcknowledgementResponsePipelet extends AbstractPipelet {

    private static final Logger LOG = LogManager.getLogger(AcknowledgementResponsePipelet.class);

    public AcknowledgementResponsePipelet() {

        forwardPipelet = false;
        frontendPipelet = true;
    }

    /* (non-Javadoc)
     * @see org.nexuse2e.messaging.AbstractPipelet#processMessage(org.nexuse2e.messaging.MessageContext)
     */
    @Override
    public MessageContext processMessage(MessageContext messageContext) throws IllegalArgumentException,
            IllegalStateException, NexusException {

        MessagePojo message = messageContext.getMessagePojo();

        // get ebxml 2.0 protocol adapter
        ProtocolAdapter protocolAdapter = new org.nexuse2e.messaging.ebxml.v20.ProtocolAdapter();

        MessageContext responseMessageContext = null;
        if (messageContext.getMessagePojo().getStatus() != MessageStatus.FAILED.getOrdinal()) {
            LOG.info(new LogMessage("No error response message found, creating ack", message));
            // generate ack for reliable connections
            try {
                responseMessageContext = protocolAdapter.createAcknowledgement(messageContext.getChoreography(),
                        messageContext);
            } catch (NexusException e) {
                  LOG.error(new LogMessage("Error creating acknowledgement", messageContext, e), e);
            }
        } else {
            LOG.error(new LogMessage("error response message found", message));
        }
        return responseMessageContext;
    }

}
