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
package org.nexuse2e.ui.taglib;

import javax.servlet.ServletContext;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.eclipse.birt.report.engine.api.IReportEngine;
import org.nexuse2e.reporting.BirtEngine;

/**
 * Evaluates the body only if BIRT reports are available
 *
 * @author Jonas Reese
 * @version $LastChangedRevision:  $ - $LastChangedDate:  $ by $LastChangedBy:  $
 */
public class ReportsAvailable extends BodyTagSupport {

    private static final long serialVersionUID = 1L;
    
    @Override
    public int doStartTag() throws JspException {
        ServletContext sc = pageContext.getSession().getServletContext();
        IReportEngine birtReportEngine = (IReportEngine) getValue( "birtReportEngine" );
        if (birtReportEngine == null) {
            try {
                birtReportEngine = BirtEngine.getBirtEngine(sc);
                setValue( "birtReportEngine", birtReportEngine );
            } catch (UnsupportedOperationException uoex) {
                return SKIP_BODY;
            }
        }
        
        return EVAL_BODY_INCLUDE;
    }
}
