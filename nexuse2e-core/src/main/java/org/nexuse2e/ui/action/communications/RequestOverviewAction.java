/**
 *  NEXUSe2e Business Messaging Open Source
 *  Copyright 2000-2009, Tamgroup and X-ioma GmbH
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation version 2.1 of
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
package org.nexuse2e.ui.action.communications;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.nexuse2e.configuration.Constants;
import org.nexuse2e.configuration.EngineConfiguration;
import org.nexuse2e.pojo.CertificatePojo;
import org.nexuse2e.ui.action.NexusE2EAction;
import org.nexuse2e.ui.form.RequestButtonStateForm;
import org.nexuse2e.util.CertificateUtil;

/**
 * @author gesch
 *
 */
public class RequestOverviewAction extends NexusE2EAction {

    /* (non-Javadoc)
     * @see com.tamgroup.nexus.e2e.ui.action.NexusE2EAction#executeNexusE2EAction(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.struts.action.ActionMessages)
     */
    @Override
    public ActionForward executeNexusE2EAction( ActionMapping actionMapping, ActionForm actionForm,
            HttpServletRequest request, HttpServletResponse response, EngineConfiguration engineConfiguration, ActionMessages errors, ActionMessages messages )
            throws Exception {

        ActionForward success = actionMapping.findForward( ACTION_FORWARD_SUCCESS );

        RequestButtonStateForm form = (RequestButtonStateForm) actionForm;

        CertificatePojo certificateRequest = engineConfiguration
                .getFirstCertificateByType( Constants.CERTIFICATE_TYPE_REQUEST, true );
        CertificatePojo certificateKey = null;
        if ( certificateRequest == null ) {
            LOG.error( "no certificate request found in database" );
        } else {
            certificateKey = engineConfiguration.getFirstCertificateByType(
                    Constants.CERTIFICATE_TYPE_PRIVATE_KEY, true );
        }
        form.setCreateRequest( true );
        form.setImportCert( false );
        form.setShowRequest( false );
        form.setDeleteRequest( true );
        if(certificateRequest != null && certificateKey != null) {
            try {
                CertificateUtil.getPKCS10Request( certificateRequest );
                form.setCreateRequest( false );
                form.setImportCert( true );
                form.setShowRequest( true );
            } catch ( IllegalArgumentException e ) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        if(certificateRequest == null && certificateKey == null) {
            form.setDeleteRequest( false );
        }
        
        form.setImportBackup( true );
       
        form.setExportPKCS12( true );

        form.setExportRequest( true );

        
        //request.getSession().setAttribute( Crumbs.CURRENT_LOCATION, Crumbs.REQUEST_OVERVIEW );

        return success;
    }

}