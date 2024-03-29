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

/**
 * This <code>MessageProcessor</code> implementation acts as an endpoint for frontend
 * outbound pipelines, on the return pipeline side. It basically forwards messages
 * to the <code>BackendInboundDispatcher</code>.
 *
 * @author Jonas Reese
 * @version $LastChangedRevision:  $ - $LastChangedDate:  $ by $LastChangedBy:  $
 */
public class FrontendOutboundResponseEndpoint implements MessageProcessor, Manageable {

    //private static final Logger LOG = LogManager.getLogger(FrontendOutboundResponseEndpoint.class);
    
    private BeanStatus status = BeanStatus.INSTANTIATED;

    
    public MessageContext processMessage( MessageContext messageContext )
            throws IllegalArgumentException, IllegalStateException,
            NexusException {

        if (messageContext != null && messageContext.isResponseMessage()) {
//            BackendInboundDispatcher dispatcher =
//                Engine.getInstance().getCurrentConfiguration().getStaticBeanContainer().getBackendInboundDispatcher();
//            try {
//                messageContext.getStateMachine().queueMessage();
//            } catch (StateTransitionException e) {
//                LOG.error(new LogMessage("Error queueing response message", messageContext), e);
//            }
//            return messageContext;
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
