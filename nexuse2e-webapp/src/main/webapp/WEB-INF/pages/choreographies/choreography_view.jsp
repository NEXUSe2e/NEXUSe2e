<%--

     NEXUSe2e Business Messaging Open Source
     Copyright 2000-2021, direkt gruppe GmbH

     This is free software; you can redistribute it and/or modify it
     under the terms of the GNU Lesser General Public License as
     published by the Free Software Foundation version 3 of
     the License.

     This software is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
     Lesser General Public License for more details.

     You should have received a copy of the GNU Lesser General Public
     License along with this software; if not, write to the Free
     Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
     02110-1301 USA, or see the FSF site: http://www.fsf.org.

--%>
<%@ taglib uri="/tags/nexus" prefix="nexus" %>
<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-nested" prefix="nested" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>


<% /*<nexus:helpBar helpDoc="documentation/Choreography.htm"/> */ %>
    
    <html:form action="ChoreographyUpdate">
        <html:hidden property="choreographyName"/>
        <center>
            <table class="NEXUS_TABLE" width="100%">
						    <tr>
						        <td>
						        	<nexus:crumbs/>
						        </td>
						    </tr>
						    <tr>
						        <td class="NEXUSScreenName"><bean:write name="choreographyForm" property="choreographyName"/></td>
						    </tr>
						</table>
            
            <table class="NEXUS_TABLE"
                   width="100%">
                <tr>
                    <td class="NEXUSName">Description</td>
                    <td class="NEXUSValue"><html:text property="description"/></td>
                </tr>
            </table>
			
			
            <table class="NEXUS_TABLE" width="100%">
                <tr>
                    <td class="NEXUSIcon"><nexus:link href="Actions.do?Target=ClusterTest&amp;Type=View" styleClass="NexusImgLink">
                      <img src="images/icons/lightning.png" border="0"></nexus:link></td>
                    <td class="NEXUSNameNoWidth"><nexus:link href="ActionsList.do?choreographyId=${choreographyForm.choreographyName}" styleClass="NexusLink">Actions</nexus:link></td>
                </tr>
                <tr>
                    <td class="NEXUSIcon"><nexus:link href="Notifiers.do?Target=ClusterTest&amp;Type=View" styleClass="NexusImgLink">
                    <img src="images/icons/transmit_blue.png" border="0"></nexus:link></td>
                    <td class="NEXUSNameNoWidth"><nexus:link href="NotifierList.do?choreographyId=${choreographyForm.choreographyName}" styleClass="NexusLink">Notifiers</nexus:link></td>
                </tr>
                
                <tr>
                    <td class="NEXUSIcon"><nexus:link href="ParticipantList.do?choreographyId=${choreographyForm.choreographyName}" styleClass="NexusImgLink">
                    <img src="images/icons/group.png" border="0"></nexus:link></td>
                    <td class="NEXUSNameNoWidth"><nexus:link href="ParticipantList.do?choreographyId=${choreographyForm.choreographyName}" styleClass="NexusLink">Participants</nexus:link></td>
                </tr>
                <tr>
                    <td class="NEXUSIcon"><nexus:link href="ReportingForward.do?noReset&amp;refresh&amp;type=transaction" styleClass="NexusImgLink">
                    <img src="images/icons/report.png" border="0"></nexus:link></td>
                    <td class="NEXUSNameNoWidth"><nexus:link href="ReportingForward.do?noReset&amp;refresh&amp;type=transaction" styleClass="NexusLink">Reports</nexus:link></td>
                </tr>
            </table>
            
        </center>
        <table class="NEXUS_BUTTON_TABLE" width="100%">
            <tr>
                <td>&nbsp;</td>
                <td class="NexusHeaderLink"><nexus:submit styleClass="button"><img src="images/icons/tick.png" class="button">Update</nexus:submit></td>
                <td class="NexusHeaderLink"><nexus:link href="ChoreographyDelete.do?choreographyName=${choreographyForm.choreographyName}" precondition="confirmDelete('Are you sure you want to delete this Choreography and all associated records?')" styleClass="button"><img src="images/icons/delete.png" class="button">Delete</nexus:link></td>
            </tr>
        </table>
    </html:form>
	<center>  
      <logic:messagesPresent>  
        <div class="NexusError"><html:errors/></div>  
      </logic:messagesPresent> 
      <logic:messagesPresent message="true">
        <html:messages id="msg" message="true">
          <div class="NexusMessage"><bean:write name="msg"/></div><br/>
        </html:messages>
      </logic:messagesPresent>
    </center>
