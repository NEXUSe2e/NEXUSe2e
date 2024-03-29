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
package org.nexuse2e.ui.action.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.nexuse2e.Engine;
import org.nexuse2e.NexusException;
import org.nexuse2e.configuration.EngineConfiguration;
import org.nexuse2e.ui.action.NexusE2EAction;
import org.nexuse2e.ui.form.DatabasePurgeForm;

import java.util.Calendar;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class DatabasePurgeAction extends NexusE2EAction {

    private static Logger LOG = LogManager.getLogger(DatabasePurgeAction.class);

    @Override
    public ActionForward executeNexusE2EAction(ActionMapping actionMapping, ActionForm actionForm,
                                               HttpServletRequest request, HttpServletResponse response,
                                               EngineConfiguration engineConfiguration, ActionMessages errors,
                                               ActionMessages messages) throws Exception {

        ActionForward success = actionMapping.findForward(ACTION_FORWARD_SUCCESS);
        DatabasePurgeForm dbForm = (DatabasePurgeForm) actionForm;
        LOG.debug("Type: " + dbForm.getType());
        if (dbForm.getType().equals("select")) {
            LOG.debug("nothing to do");

            dbForm.setEndEnabled(true);

        } else if (dbForm.getType().equals("preview")) {
            LOG.debug("start: " + dbForm.getStartDay() + "." + dbForm.getStartMonth() + "." + dbForm.getStartYear() + " " + dbForm.getStartHour() + ":" + dbForm.getStartMin());
            Date[] dates = getTimestamps(dbForm);

            dbForm.setLogEntryCount((int) Engine.getInstance().getTransactionService().getLogCount(dates[0], dates[1]));


            LOG.debug("preview purgeable data");
        } else if (dbForm.getType().equals("remove")) {

            LOG.debug("removing data");
            Date[] dates = getTimestamps(dbForm);
            LOG.debug("purging selected log entries");
            try {
                Engine.getInstance().getTransactionService().removeLogEntries(dates[0], dates[1]);
            } catch (Exception e) {
                LOG.error("Error while deleting log entries", e);
            }


        }

        return success;
    }

    private Date[] getTimestamps(DatabasePurgeForm form) throws NexusException {

        Date startDate = null;
        Date endDate = null;

        if (form.isStartEnabled()) {
            Calendar start = Calendar.getInstance();
            start.set(Calendar.DAY_OF_MONTH, Integer.parseInt(form.getStartDay()));
            start.set(Calendar.MONTH, Integer.parseInt(form.getStartMonth()) - 1);
            start.set(Calendar.YEAR, Integer.parseInt(form.getStartYear()));
            start.set(Calendar.HOUR_OF_DAY, Integer.parseInt(form.getStartHour()));
            start.set(Calendar.MINUTE, Integer.parseInt(form.getStartMin()));
            startDate = start.getTime();
        }
        if (form.isEndEnabled()) {
            Calendar end = Calendar.getInstance();
            end.set(Calendar.DAY_OF_MONTH, Integer.parseInt(form.getEndDay()));
            end.set(Calendar.MONTH, Integer.parseInt(form.getEndMonth()) - 1);
            end.set(Calendar.YEAR, Integer.parseInt(form.getEndYear()));
            end.set(Calendar.HOUR_OF_DAY, Integer.parseInt(form.getEndHour()));
            end.set(Calendar.MINUTE, Integer.parseInt(form.getEndMin()));
            endDate = end.getTime();
        }
        return new Date[]{startDate, endDate};
    }
}
