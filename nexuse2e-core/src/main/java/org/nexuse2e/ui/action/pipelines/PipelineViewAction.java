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
package org.nexuse2e.ui.action.pipelines;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.nexuse2e.configuration.EngineConfiguration;
import org.nexuse2e.pojo.ComponentPojo;
import org.nexuse2e.pojo.PipelinePojo;
import org.nexuse2e.ui.action.NexusE2EAction;
import org.nexuse2e.ui.form.PipelineForm;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author guido.esch
 */
public class PipelineViewAction extends NexusE2EAction {

    private static Logger LOG = LogManager.getLogger(PipelineViewAction.class);

    /* (non-Javadoc)
     * @see com.tamgroup.nexus.e2e.ui.action.NexusE2EAction#executeNexusE2EAction(org.apache.struts.action
     * .ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http
     * .HttpServletResponse, org.apache.struts.action.ActionMessages)
     */
    @Override
    public ActionForward executeNexusE2EAction(ActionMapping actionMapping, ActionForm actionForm,
                                               HttpServletRequest request, HttpServletResponse response,
                                               EngineConfiguration engineConfiguration, ActionMessages errors,
                                               ActionMessages messages) throws Exception {

        ActionForward success = actionMapping.findForward(ACTION_FORWARD_SUCCESS);

        PipelineForm form = (PipelineForm) actionForm;

        String keepData = (String) request.getAttribute("keepData");
        LOG.trace("attrib: " + keepData);

        PipelinePojo pipeline = engineConfiguration.getPipelinePojoByNxPipelineId(form.getNxPipelineId());
        if ((pipeline != null) && (keepData == null || !keepData.equals("true"))) {
            form.setProperties(pipeline);
        }

        List<ComponentPojo> components = engineConfiguration.getPipelets(pipeline.isFrontend());
        form.setAvailableTemplates(components);

        //request.getSession().setAttribute( Crumbs.CURRENT_LOCATION, Crumbs.PIPELINE_VIEW + "_" + form
        // .getNxPipelineId() );
        return success;
    }

}
