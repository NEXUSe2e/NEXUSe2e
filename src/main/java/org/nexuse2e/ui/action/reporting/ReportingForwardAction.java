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
package org.nexuse2e.ui.action.reporting;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.nexuse2e.Engine;
import org.nexuse2e.configuration.Constants;
import org.nexuse2e.pojo.ChoreographyPojo;
import org.nexuse2e.pojo.PartnerPojo;
import org.nexuse2e.ui.action.NexusE2EAction;
import org.nexuse2e.ui.form.ReportingSettingsForm;

/**
 * @author guido.esch
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ReportingForwardAction extends NexusE2EAction {

    private static String URL     = "reporting.error.url";
    private static String TIMEOUT = "reporting.error.timeout";


    @Override
    public ActionForward executeNexusE2EAction( ActionMapping actionMapping, ActionForm actionForm,
            HttpServletRequest request, HttpServletResponse response, ActionMessages errors, ActionMessages messages )
            throws Exception {

        ReportingSettingsForm form = (ReportingSettingsForm) actionForm;
        String type = request.getParameter( "type" );
        if ( type != null ) {
            LOG.trace( "----- form: " + form );
            form.setCommand( type );
        }
        form.setStartCount( 0 );
        form.setEndCount( 0 );

        if ( form.getCommand() != null && form.getCommand().equals( "transaction" ) ) {
            List<String> participants = new ArrayList<String>();

            List<PartnerPojo> partners = Engine.getInstance().getActiveConfigurationAccessService().getPartners(
                    Constants.PARTNER_TYPE_PARTNER, Constants.PARTNERCOMPARATOR );
            Iterator<PartnerPojo> partnerI = partners.iterator();
            LOG.trace( "PartnerCount: " + partners.size() );
            while ( partnerI.hasNext() ) {
                PartnerPojo partner = partnerI.next();
                participants.add( partner.getPartnerId() );

            }
            form.setParticipantIds( participants );

            List<String> choreographyIds = new ArrayList<String>();

            List<ChoreographyPojo> choreographies = Engine.getInstance().getActiveConfigurationAccessService().getChoreographies();

            Iterator<ChoreographyPojo> choreographyI = choreographies.iterator();
            while ( choreographyI.hasNext() ) {
                ChoreographyPojo choreography = choreographyI.next();
                choreographyIds.add( choreography.getName() );
            }
            form.setChoreographyIds( choreographyIds );

            Map<String, Object> values = new HashMap<String, Object>();
            values = Engine.getInstance().getActiveConfigurationAccessService().getGenericParameters(
                    "log_display_configuration", null, form.getParameterMap() );
            fillForm( values, form );

            return actionMapping.findForward( "conversation" );
        } else if ( form.getCommand() != null && form.getCommand().equals( "engine" ) ) {

            Map<String, Object> values = new HashMap<String, Object>();
            values = Engine.getInstance().getActiveConfigurationAccessService().getGenericParameters(
                    "log_display_configuration", null, form.getParameterMap() );
            fillForm( values, form );

            return actionMapping.findForward( "engine" );

        } else if ( form.getCommand() != null && form.getCommand().equals( "saveFields" ) ) {

            //            System.out.println("form.severity: "+form.isEngineColSeverity());
            //            System.out.println("form.className: "+form.isEngineColClassName());

            Map<String, Object> values = new HashMap<String, Object>();

            values.put( ReportingSettingsForm.PARAM_NAME_ENGCOL_SEVERITY, Boolean
                    .valueOf( form.isEngineColSeverity() ) );
            values.put( ReportingSettingsForm.PARAM_NAME_ENGCOL_CLASSNAME, Boolean.valueOf( form
                    .isEngineColClassName() ) );
            values.put( ReportingSettingsForm.PARAM_NAME_ENGCOL_DESCRIPTION, Boolean.valueOf( form
                    .isEngineColDescription() ) );
            values.put( ReportingSettingsForm.PARAM_NAME_ENGCOL_ISSUEDDATE, Boolean
                    .valueOf( form.isEngineColIssued() ) );
            values.put( ReportingSettingsForm.PARAM_NAME_ENGCOL_METHODNAME, Boolean.valueOf( form
                    .isEngineColmethodName() ) );
            values.put( ReportingSettingsForm.PARAM_NAME_ENGCOL_ORIGIN, Boolean.valueOf( form.isEngineColOrigin() ) );

            values.put( ReportingSettingsForm.PARAM_NAME_CONVCOL_ACTION, Boolean.valueOf( form.isConvColAction() ) );
            values.put( ReportingSettingsForm.PARAM_NAME_CONVCOL_CHOREOGRAPHYID, Boolean.valueOf( form
                    .isConvColChorId() ) );
            values.put( ReportingSettingsForm.PARAM_NAME_CONVCOL_CONVID, Boolean.valueOf( form.isConvColConId() ) );
            values.put( ReportingSettingsForm.PARAM_NAME_CONVCOL_CREATED, Boolean.valueOf( form.isConvColCreated() ) );
            values.put( ReportingSettingsForm.PARAM_NAME_CONVCOL_PARTICIPANTID, Boolean.valueOf( form
                    .isConvColPartId() ) );
            values.put( ReportingSettingsForm.PARAM_NAME_CONVCOL_SELECT, Boolean.valueOf( form.isConvColSelect() ) );
            values.put( ReportingSettingsForm.PARAM_NAME_CONVCOL_STATUS, Boolean.valueOf( form.isConvColStatus() ) );
            values.put( ReportingSettingsForm.PARAM_NAME_CONVCOL_TURNAROUND, Boolean.valueOf( form
                    .isConvColTurnaround() ) );

            values.put( ReportingSettingsForm.PARAM_NAME_MSGCOL_ACTION, Boolean.valueOf( form.isMessColAction() ) );
            values.put( ReportingSettingsForm.PARAM_NAME_MSGCOL_CREATED, Boolean.valueOf( form.isMessColCreated() ) );
            values.put( ReportingSettingsForm.PARAM_NAME_MSGCOL_MSGID, Boolean.valueOf( form.isMessColMessageId() ) );
            values.put( ReportingSettingsForm.PARAM_NAME_MSGCOL_PARTICIPANTID, Boolean.valueOf( form
                    .isMessColParticipantId() ) );
            values.put( ReportingSettingsForm.PARAM_NAME_MSGCOL_SELECT, Boolean.valueOf( form.isMessColSelect() ) );
            values.put( ReportingSettingsForm.PARAM_NAME_MSGCOL_STATUS, Boolean.valueOf( form.isMessColStatus() ) );
            values.put( ReportingSettingsForm.PARAM_NAME_MSGCOL_TURNAROUND, Boolean.valueOf( form
                    .isMessColTurnaround() ) );
            values.put( ReportingSettingsForm.PARAM_NAME_MSGCOL_TYPE, Boolean.valueOf( form.isMessColType() ) );

            values.put( ReportingSettingsForm.PARAM_NAME_TIMEZONE, form.getTimezone() );
            values.put( ReportingSettingsForm.PARAM_NAME_ROWCOUNT, "" + form.getPageSize() );

            Engine.getInstance().getActiveConfigurationAccessService().setGenericParameters(
                    "log_display_configuration", null, values, form.getParameterMap(), true );
            form.setCommand( "" );
            return actionMapping.findForward( "save" );
        } else if ( form.getCommand() != null && form.getCommand().equals( "view" ) ) {
            Map<String, Object> values =
                Engine.getInstance().getActiveConfigurationAccessService().getGenericParameters(
                        "log_display_configuration", null, form.getParameterMap() );

            fillForm( values, form );

            return actionMapping.findForward( "view" );
        } else if (form.getCommand() != null && form.getCommand().equals( "statistics" )) {
            Calendar cal = Calendar.getInstance();
            cal.set( Calendar.HOUR_OF_DAY, 0 );
            cal.set( Calendar.MINUTE, 0 );
            cal.set( Calendar.SECOND, 0 );
            cal.set( Calendar.MILLISECOND, 0 );
            request.setAttribute( "startOfDay", cal.getTime() );
            return actionMapping.findForward( "statistics" );
        } else {
            LOG.error( "Invalid actionparameter=>" + form.getCommand() + "<" );
            ActionMessage errorMessage = new ActionMessage( "generic.error", "Invalid actionparameter=>"
                    + form.getCommand() + "<" );
            errors.add( ActionMessages.GLOBAL_MESSAGE, errorMessage );
            addRedirect( request, URL, TIMEOUT );
            return actionMapping.findForward( ACTION_FORWARD_FAILURE );
        }

    }

    /**
     * @param values
     * @param form
     */
    private void fillForm( Map<String, Object> values, ReportingSettingsForm form ) {

        form.setEngineColSeverity( getBooleanValue( values, ReportingSettingsForm.PARAM_NAME_ENGCOL_SEVERITY, true ) );
        form
                .setEngineColClassName( getBooleanValue( values, ReportingSettingsForm.PARAM_NAME_ENGCOL_CLASSNAME,
                        true ) );
        form.setEngineColDescription( getBooleanValue( values, ReportingSettingsForm.PARAM_NAME_ENGCOL_DESCRIPTION,
                true ) );
        form.setEngineColIssued( getBooleanValue( values, ReportingSettingsForm.PARAM_NAME_ENGCOL_ISSUEDDATE, true ) );
        form.setEngineColmethodName( getBooleanValue( values, ReportingSettingsForm.PARAM_NAME_ENGCOL_METHODNAME,
                true ) );
        form.setEngineColOrigin( getBooleanValue( values, ReportingSettingsForm.PARAM_NAME_ENGCOL_ORIGIN, true ) );

        form.setConvColAction( getBooleanValue( values, ReportingSettingsForm.PARAM_NAME_CONVCOL_ACTION, true ) );
        form
                .setConvColChorId( getBooleanValue( values, ReportingSettingsForm.PARAM_NAME_CONVCOL_CHOREOGRAPHYID,
                        true ) );
        form.setConvColConId( getBooleanValue( values, ReportingSettingsForm.PARAM_NAME_CONVCOL_CONVID, true ) );
        form.setConvColCreated( getBooleanValue( values, ReportingSettingsForm.PARAM_NAME_CONVCOL_CREATED, true ) );
        form
                .setConvColPartId( getBooleanValue( values, ReportingSettingsForm.PARAM_NAME_CONVCOL_PARTICIPANTID,
                        true ) );
        form.setConvColSelect( getBooleanValue( values, ReportingSettingsForm.PARAM_NAME_CONVCOL_SELECT, true ) );
        form.setConvColStatus( getBooleanValue( values, ReportingSettingsForm.PARAM_NAME_CONVCOL_STATUS, true ) );
        form
                .setConvColTurnaround( getBooleanValue( values, ReportingSettingsForm.PARAM_NAME_CONVCOL_TURNAROUND,
                        true ) );

        form.setMessColAction( getBooleanValue( values, ReportingSettingsForm.PARAM_NAME_MSGCOL_ACTION, true ) );
        form.setMessColCreated( getBooleanValue( values, ReportingSettingsForm.PARAM_NAME_MSGCOL_CREATED, true ) );
        form.setMessColMessageId( getBooleanValue( values, ReportingSettingsForm.PARAM_NAME_MSGCOL_MSGID, true ) );
        form.setMessColParticipantId( getBooleanValue( values, ReportingSettingsForm.PARAM_NAME_MSGCOL_PARTICIPANTID,
                true ) );
        form.setMessColSelect( getBooleanValue( values, ReportingSettingsForm.PARAM_NAME_MSGCOL_SELECT, true ) );
        form.setMessColStatus( getBooleanValue( values, ReportingSettingsForm.PARAM_NAME_MSGCOL_STATUS, true ) );
        form
                .setMessColTurnaround( getBooleanValue( values, ReportingSettingsForm.PARAM_NAME_MSGCOL_TURNAROUND,
                        true ) );
        form.setMessColType( getBooleanValue( values, ReportingSettingsForm.PARAM_NAME_MSGCOL_TYPE, true ) );

        form.setTimezone( getStringValue( values, ReportingSettingsForm.PARAM_NAME_TIMEZONE, "" ) );
        form.setPageSize( getIntValue( values, ReportingSettingsForm.PARAM_NAME_ROWCOUNT, 20 ) );

    }

    /**
     * @param values
     * @param label
     * @param defaultValue
     * @return
     */
    private int getIntValue( Map<String, Object> values, String label, int defaultValue ) {

        int returnvalue = defaultValue;
        if ( values != null && label != null ) {
            Object value = values.get( label );
            if ( value instanceof String ) {
                try {
                    returnvalue = Integer.parseInt( (String) value );
                } catch ( NumberFormatException e ) {
                    LOG.error( "Generic parameter " + label + " is not a parsable integer value: " + value );
                }
            } else {
                if ( value == null ) {
                    LOG.error( "Generic parameter " + label + ", no value found, using default value: " + defaultValue );
                } else {
                    LOG.error( "Generic parameter " + label + " is instance of " + value.getClass().getName()
                            + " but Int was expected!" );
                }
            }

        } else {
            if ( values == null ) {
                LOG.debug( "Generic Parameters must not be null!" );
            }
            if ( label == null ) {
                LOG.debug( "Generic Parameter Label must not be null" );
            }
        }

        return returnvalue;
    }

    /**
     * @param values
     * @param label
     * @param defaultValue
     * @return
     */
    private String getStringValue( Map<String, Object> values, String label, String defaultValue ) {

        String returnvalue = defaultValue;
        if ( values != null && label != null ) {
            Object value = values.get( label );
            if ( value instanceof String ) {
                returnvalue = (String) value;
            } else {
                if ( value == null ) {
                    LOG.warn( "Generic parameter " + label + ", no value found, using default value: " + defaultValue );
                } else {
                    LOG.error( "Generic parameter " + label + " is instance of " + value.getClass().getName()
                            + " but String was expected!" );
                }
            }

        } else {
            if ( values == null ) {
                LOG.debug( "Generic Parameters must not be null!" );
            }
            if ( label == null ) {
                LOG.debug( "Generic Parameter Label must not be null" );
            }
        }

        return returnvalue;
    }

    /**
     * @param values
     * @param label
     * @param defaultValue
     * @return
     */
    private boolean getBooleanValue( Map<String, Object> values, String label, boolean defaultValue ) {

        boolean returnvalue = defaultValue;
        if ( values != null && label != null ) {
            Object value = values.get( label );
            if ( value instanceof Boolean ) {
                returnvalue = ( (Boolean) value ).booleanValue();
            } else {
                if ( value == null ) {
                    LOG.error( "Generic parameter " + label + ", no value found, using default value: " + defaultValue );
                } else {
                    LOG.error( "Generic parameter " + label + " is instance of " + value.getClass().getName()
                            + " but Boolean was expected!" );
                }
            }

        } else {
            if ( values == null ) {
                LOG.debug( "Generic Parameters must not be null!" );
            }
            if ( label == null ) {
                LOG.debug( "Generic Parameter Label must not be null" );
            }
        }

        return returnvalue;
    }

}
