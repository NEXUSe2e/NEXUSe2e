/**
 * NEXUSe2e Business Messaging Open Source  
 * Copyright 2007, Tamgroup and X-ioma GmbH   
 *  
 * This is free software; you can redistribute it and/or modify it  
 * under the terms of the GNU Lesser General Public License as  
 * published by the Free Software Foundation version 2.1 of  
 * the License.  
 *  
 * This software is distributed in the hope that it will be useful,  
 * but WITHOUT ANY WARRANTY; without even the implied warranty of  
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU  
 * Lesser General Public License for more details.  
 *  
 * You should have received a copy of the GNU Lesser General Public  
 * License along with this software; if not, write to the Free  
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.nexuse2e.backend.pipelets;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.nexuse2e.Engine;
import org.nexuse2e.NexusException;
import org.nexuse2e.backend.BackendPipelineDispatcher;
import org.nexuse2e.configuration.EngineConfiguration;
import org.nexuse2e.configuration.ParameterDescriptor;
import org.nexuse2e.configuration.Constants.ParameterType;
import org.nexuse2e.messaging.AbstractPipelet;
import org.nexuse2e.messaging.MessageContext;

/**
 * Pipelet that simulates an immediate business reply, thereby challanging the state machine.
 *
 * @author mbreilmann
 */
public class ShortCircuitPipelet extends AbstractPipelet {

    private static Logger         LOG          = Logger.getLogger( ShortCircuitPipelet.class );

    protected static final String PARAM_ACTION = "action";

    protected String              action;

    /**
     * 
     */
    public ShortCircuitPipelet() {

        parameterMap.put( PARAM_ACTION, new ParameterDescriptor( ParameterType.STRING, "Action",
                "Action for return message", "" ) );
    }

    @Override
    public void initialize( EngineConfiguration config ) throws InstantiationException {

        LOG.trace( "initializing" );
        action = getParameter( PARAM_ACTION );
        if ( StringUtils.isEmpty( action ) ) {
            LOG.info( "No value for setting 'action' provided!" );
            throw new InstantiationException( "No value for setting 'action' provided!" );
        }

        super.initialize( config );
    }

    @Override
    public MessageContext processMessage( MessageContext messageContext ) throws IllegalArgumentException,
            IllegalStateException, NexusException {

        BackendPipelineDispatcher backendPipelineDispatcher = Engine.getInstance().getCurrentConfiguration()
                .getStaticBeanContainer().getBackendPipelineDispatcher();

        if ( backendPipelineDispatcher != null ) {
            try {
                backendPipelineDispatcher.processMessage( messageContext.getMessagePojo().getConversation()
                        .getConversationId(), action, null, "<?xml version=\"1.0\" encoding=\"UTF-8\"?><test />"
                        .getBytes() );
            } catch ( NexusException e ) {
                LOG.debug( "sendStringMessage - error: " + e );
                e.printStackTrace();
            }
        }
        return messageContext;
    }

} // ShortCircuitPipelet