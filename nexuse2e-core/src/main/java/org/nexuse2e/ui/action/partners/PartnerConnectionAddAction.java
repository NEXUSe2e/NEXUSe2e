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
package org.nexuse2e.ui.action.partners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.nexuse2e.configuration.EngineConfiguration;
import org.nexuse2e.pojo.PartnerPojo;
import org.nexuse2e.ui.action.NexusE2EAction;
import org.nexuse2e.ui.form.PartnerConnectionForm;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author guido.esch
 */
public class PartnerConnectionAddAction extends NexusE2EAction {

    private static Logger LOG = LogManager.getLogger(PartnerConnectionAddAction.class);

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

        PartnerConnectionForm form = (PartnerConnectionForm) actionForm;
        form.cleanSettings();

        // defaults
        form.setReliable(true);
        form.setRetries(3);
        form.setTimeout(30);
        form.setMessageInterval(30);

        LOG.trace("form.partnerId: " + form.getPartnerId());

        PartnerPojo partner = engineConfiguration.getPartnerByPartnerId(form.getPartnerId());
        form.setCertificates(partner.getCertificates());
        form.setTrps(engineConfiguration.getTrps());

        //request.getSession().setAttribute( Crumbs.CURRENT_LOCATION, Crumbs.PARTNER_ADDCONN+"_"+form.getPartnerId() );

        return success;
    }

}
