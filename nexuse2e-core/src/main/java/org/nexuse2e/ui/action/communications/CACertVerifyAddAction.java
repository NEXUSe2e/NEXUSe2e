/**
 * NEXUSe2e Business Messaging Open Source
 * Copyright 2000-2009, Tamgroup and X-ioma GmbH
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
package org.nexuse2e.ui.action.communications;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.nexuse2e.configuration.CertificateType;
import org.nexuse2e.configuration.EngineConfiguration;
import org.nexuse2e.pojo.CertificatePojo;
import org.nexuse2e.ui.action.NexusE2EAction;
import org.nexuse2e.ui.form.CertificatePropertiesForm;
import org.nexuse2e.ui.form.ProtectedFileAccessForm;
import org.nexuse2e.util.CertificateUtil;

/**
 * @author gesch
 * 
 */
public class CACertVerifyAddAction extends NexusE2EAction {

    private static String URL     = "cacerts.error.url";
    private static String TIMEOUT = "cacerts.error.timeout";
    private static String NEW     = "new";
    private static String UPDATE  = "update";
    private static String DUPLICATE = "duplicate";

    /*
     * (non-Javadoc)
     * 
     * @see com.tamgroup.nexus.e2e.ui.action.NexusE2EAction#executeNexusE2EAction(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm,
     * javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.struts.action.ActionMessages)
     */
    @Override
    public ActionForward executeNexusE2EAction(ActionMapping actionMapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response,
            EngineConfiguration engineConfiguration, ActionMessages errors, ActionMessages messages) throws Exception {

        ActionForward newCert = actionMapping.findForward(NEW);
        ActionForward update = actionMapping.findForward(UPDATE);
        ActionForward duplicateCert = actionMapping.findForward(DUPLICATE);
        ActionForward error = actionMapping.findForward(ACTION_FORWARD_FAILURE);

        ProtectedFileAccessForm form = (ProtectedFileAccessForm) actionForm;

        try {
            String alias = form.getAlias();

            CertificatePojo cPojo = engineConfiguration.getCertificateByName(CertificateType.CA.getOrdinal(), alias);
            byte[] data = form.getCertficate().getFileData();
            X509Certificate cert = CertificateUtil.getX509Certificate(data);
            List<CertificatePojo> duplicates = CertificateUtil.getDuplicates(engineConfiguration, CertificateType.CA, cert);
            if (cPojo != null) {
                CertificatePropertiesForm certForm = new CertificatePropertiesForm();
                X509Certificate x509Certificate = CertificateUtil.getX509Certificate(cPojo.getBinaryData());

                certForm.setCertificateProperties(x509Certificate);
                certForm.setAlias(alias);
                request.setAttribute("existingCertificate", certForm);

                // request.getSession().setAttribute( Crumbs.CURRENT_LOCATION, Crumbs.CA_VERIFY_ADD );
                form.setPreserve(true);

                return update;
            } else if (duplicates.size() > 0) {
                List<CertificatePropertiesForm> certForms = new ArrayList<>();
                CertificatePropertiesForm certForm;

                for (CertificatePojo duplicate : duplicates) {
                    certForm = new CertificatePropertiesForm();
                    certForm.setAlias(duplicate.getName());
                    certForm.setCertificateProperties(CertificateUtil.getX509Certificate(duplicate));
                    certForms.add(certForm);
                }
                request.setAttribute("duplicates", certForms);

                // request.getSession().setAttribute( Crumbs.CURRENT_LOCATION, Crumbs.CA_VERIFY_ADD );
                form.setPreserve(true);

                return duplicateCert;
            } else {
                // request.getSession().setAttribute( Crumbs.CURRENT_LOCATION, Crumbs.CA_VERIFY_ADD );
                form.setPreserve(true);

                return newCert;
            }
        } catch (Exception e) {
            ActionMessage errormessage = new ActionMessage("generic.error", e.getMessage());
            errors.add(ActionMessages.GLOBAL_MESSAGE, errormessage);
            addRedirect(request, URL, TIMEOUT);
            return error;
        }
    }
}
