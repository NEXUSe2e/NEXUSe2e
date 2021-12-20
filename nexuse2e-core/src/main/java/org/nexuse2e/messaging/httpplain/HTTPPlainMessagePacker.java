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
package org.nexuse2e.messaging.httpplain;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.nexuse2e.NexusException;
import org.nexuse2e.messaging.AbstractPipelet;
import org.nexuse2e.messaging.MessageContext;
import org.nexuse2e.pojo.MessagePayloadPojo;

/**
 * @author mbreilmann
 *
 */
public class HTTPPlainMessagePacker extends AbstractPipelet {

    private static Logger       LOG = LogManager.getLogger( HTTPPlainMessagePacker.class );
    
    /**
     * Default constructor.
     */
    public HTTPPlainMessagePacker() {
        frontendPipelet = true;
    }

    /* (non-Javadoc)
     * @see org.nexuse2e.messaging.MessageUnpackager#processMessage(com.tamgroup.nexus.e2e.persistence.pojo.MessagePojo, byte[])
     */
    public MessageContext processMessage( MessageContext messageContext )
            throws IllegalArgumentException, IllegalStateException, NexusException {

        List<MessagePayloadPojo> payloads = messageContext.getMessagePojo().getMessagePayloads();

        if ( !payloads.isEmpty() ) {
            messageContext.setData( payloads.iterator().next().getPayloadData() );
        } else {
            LOG.error( "No payload found in HTTPPlain outbound message!" );
            throw new NexusException( "No payload found in HTTPPlain outbound message!" );
        }

        return messageContext;
    }

} // HTTPPlainMessagePacker
