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
package org.nexuse2e.messaging;

import org.nexuse2e.BeanStatus;
import org.nexuse2e.Layer;
import org.nexuse2e.Manageable;
import org.nexuse2e.NexusException;
import org.nexuse2e.configuration.EngineConfiguration;
import org.nexuse2e.controller.StateTransitionException;

/**
 * This <code>MessageProcessor</code> implementation acts as an endpoint for frontend
 * inbound pipelines, on the return pipeline side. It basically manages the returned
 * response message status.
 *
 * @author Jonas Reese
 * @version $LastChangedRevision:  $ - $LastChangedDate:  $ by $LastChangedBy:  $
 */
public class FrontendInboundResponseEndpoint implements MessageProcessor, Manageable {

    private BeanStatus status = BeanStatus.INSTANTIATED;

    
    public MessageContext processMessage( MessageContext messageContext )
            throws IllegalArgumentException, IllegalStateException,
            NexusException {

        if (messageContext != null && messageContext.isResponseMessage()) {
            try {
                messageContext.getStateMachine().queueMessage(); // persist message and perform choreo step transition
                messageContext.getStateMachine().sentMessage();  // mark response as 'sent'
            } catch (StateTransitionException e) {
                throw new NexusException( e );
            }
        } else if (messageContext != null && messageContext.getMessagePojo().isAck()) {
            try {
                messageContext.getStateMachine().sentSynchronousAck();  // save ack as 'sent'
            } catch (StateTransitionException e) {
                throw new NexusException( e );
            }
        }
        
        return messageContext;
    }

    public void activate() {
        status = BeanStatus.ACTIVATED;
    }

    public void deactivate() {
        status = BeanStatus.INITIALIZED;
    }

    public Layer getActivationLayer() {
        return Layer.INBOUND_PIPELINES;
    }

    public BeanStatus getStatus() {
        return status;
    }

    public void initialize( EngineConfiguration config )
            throws InstantiationException {
        status = BeanStatus.INITIALIZED;
    }

    public void teardown() {
        status = BeanStatus.INSTANTIATED;
    }
}
