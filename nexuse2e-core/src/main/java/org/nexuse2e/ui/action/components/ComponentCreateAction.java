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
package org.nexuse2e.ui.action.components;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.nexuse2e.configuration.ComponentType;
import org.nexuse2e.configuration.EngineConfiguration;
//import org.nexuse2e.logging.LogAppender;
import org.nexuse2e.messaging.Pipelet;
import org.nexuse2e.pojo.ComponentPojo;
import org.nexuse2e.service.Service;
import org.nexuse2e.ui.action.NexusE2EAction;
import org.nexuse2e.ui.form.ComponentForm;

/**
 * @author guido.esch
 */
public class ComponentCreateAction extends NexusE2EAction {

    private static String URL     = "partner.error.url";
    private static String TIMEOUT = "partner.error.timeout";

    /* (non-Javadoc)
     * @see com.tamgroup.nexus.e2e.ui.action.NexusE2EAction#executeNexusE2EAction(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.struts.action.ActionMessages)
     */
    @Override
    public ActionForward executeNexusE2EAction( ActionMapping actionMapping, ActionForm actionForm,
            HttpServletRequest request, HttpServletResponse response, EngineConfiguration engineConfiguration, ActionMessages errors, ActionMessages messages )
            throws Exception {

        ActionForward success = actionMapping.findForward( ACTION_FORWARD_SUCCESS );
        ActionForward error = actionMapping.findForward( ACTION_FORWARD_SUCCESS );

        ComponentForm form = (ComponentForm) actionForm;

        ComponentPojo component = new ComponentPojo();
        form.getProperties( component );

        Object o = null;
        try {
            o = Class.forName( form.getClassName() ).newInstance();
        } catch ( Exception ex ) {
            ex.printStackTrace();
            ActionMessage errorMessage = new ActionMessage( "generic.error", "Class " + form.getClassName()
                    + ( " could not be instantiated: " + ex ) );
            errors.add( ActionMessages.GLOBAL_MESSAGE, errorMessage );
            addRedirect( request, URL, TIMEOUT );
            return error;
        }
        if ( o instanceof Service ) {
            component.setType( ComponentType.SERVICE.getValue() );
        }
//        else if ( o instanceof LogAppender ) {
//            component.setType( ComponentType.LOGGER.getValue() );
//        }
        else if ( o instanceof Pipelet ) {
            component.setType( ComponentType.PIPELET.getValue() );
        } else {
            ActionMessage errorMessage = new ActionMessage( "generic.error", "Class " + form.getClassName()
                    + ( o == null ? " could not be found" : " is not a valid component" ) );
            errors.add( ActionMessages.GLOBAL_MESSAGE, errorMessage );
            addRedirect( request, URL, TIMEOUT );
            return error;
        }

        engineConfiguration.getComponents( ComponentType.ALL, null ).add(
                component );
        engineConfiguration.updateComponent( component );

        request.setAttribute( REFRESH_TREE, "true" );

        return success;
    }
}
