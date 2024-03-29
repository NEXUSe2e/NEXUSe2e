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
package org.nexuse2e.ui.action.communications;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.nexuse2e.Engine;
import org.nexuse2e.configuration.CertificateType;
import org.nexuse2e.configuration.EngineConfiguration;
import org.nexuse2e.pojo.CertificatePojo;
import org.nexuse2e.ui.action.NexusE2EAction;
import org.nexuse2e.ui.form.ProtectedFileAccessForm;
import org.nexuse2e.util.CertificateUtil;
import org.nexuse2e.util.EncryptionUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author guido.esch
 */
public class CACertFinishExportAction extends NexusE2EAction {

    private static Logger LOG = LogManager.getLogger(CACertFinishExportAction.class);

    /*
     * (non-Javadoc)
     *
     * @see com.tamgroup.nexus.e2e.ui.action.NexusE2EAction#executeNexusE2EAction(org.apache.struts.action
     * .ActionMapping, org.apache.struts.action.ActionForm,
     * javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, org.apache.struts.action
     * .ActionMessages)
     */
    @Override
    public ActionForward executeNexusE2EAction(ActionMapping actionMapping, ActionForm actionForm,
                                               HttpServletRequest request, HttpServletResponse response,
                                               EngineConfiguration engineConfiguration, ActionMessages errors,
                                               ActionMessages messages) throws Exception {

        ActionForward success = actionMapping.findForward(ACTION_FORWARD_SUCCESS);
        ActionForward error = actionMapping.findForward(ACTION_FORWARD_FAILURE);
        ProtectedFileAccessForm form = (ProtectedFileAccessForm) actionForm;

        if (form.getStatus() != 3) {
            String path = Engine.getInstance().getCacertsPath();
            if (form.getStatus() == 2) {

                path = form.getCertficatePath();
            }

            LOG.debug("CA certificate export path: " + path);

            CertificatePojo cPojo =
                    engineConfiguration.getFirstCertificateByType(CertificateType.CACERT_METADATA.getOrdinal(), true);
            String password = "changeit";
            if (cPojo == null) {
                LOG.error("Error retrieving CA meta data!");
            } else {
                password = EncryptionUtil.decryptString(cPojo.getPassword());
            }
            LOG.trace("Using password: " + password);

            try {
                File certFile = new File(path);
                LOG.trace("Created CA keystore file handle");
                FileOutputStream fos = new FileOutputStream(certFile);
                LOG.trace("Created CA keystore output stream: " + fos);

                List<CertificatePojo> caCertificates =
                        engineConfiguration.getCertificates(CertificateType.CA.getOrdinal(), null);

                KeyStore jks = CertificateUtil.generateKeyStoreFromPojos(caCertificates);
                LOG.trace("Created CA keystore: " + jks);

                jks.store(fos, new String(password).toCharArray());
                fos.flush();
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("Exception saving keystore", e);

                ActionMessage errorMessage = new ActionMessage("generic.error", e);
                errors.add(ActionMessages.GLOBAL_MESSAGE, errorMessage);
            } catch (Error e) {
                e.printStackTrace();
                LOG.error("Error saving keystore", e);

                ActionMessage errorMessage = new ActionMessage("generic.error", e);
                errors.add(ActionMessages.GLOBAL_MESSAGE, errorMessage);
            }
        }
        if (!errors.isEmpty()) {
            return error;
        }

        return success;
    }
}
