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
<%@ taglib uri="/tags/struts-bean" prefix="bean"%>
<%@ taglib uri="/tags/struts-html" prefix="html"%>
<%@ taglib uri="/tags/struts-nested" prefix="nested"%>
<%@ taglib uri="/tags/struts-tiles" prefix="tiles"%>
<%@ taglib uri="/tags/struts-logic" prefix="logic"%>
<%@ taglib uri="/tags/nexus" prefix="nexus"%>

<nexus:fileUploadResponse>
<% /*<nexus:helpBar helpDoc="documentation/SSL.htm" /> */ %>

<center>
<table class="NEXUS_TABLE" width="100%">
	<tr>
		<td><nexus:crumbs /></td>
	</tr>
	<tr>
		<td class="NEXUSScreenName">Prepare Certificate Import</td>
	</tr>
</table>

<html:form action="RequestImportCertChain.do">
	<html:hidden property="accept" value="true"/>
	
	
	<table class="NEXUS_TABLE" width="100%">
		<tr>
			<td colspan="100%" class="NEXUSSection">Certificate Chain: </td>
		</tr>
		<logic:iterate id="cert" name="certificateChainImportForm" property="certChain">
		
		
		<table class="NEXUS_TABLE" width="100%">
            <tr>            
            <td colspan="2" class="NEXUSSection"></td>            
            </tr>
            <tr>
                <td class="NEXUSName">Common Name</td>
                <td class="NEXUSValue"><bean:write name="cert" property="commonName"/></td>
            </tr>
            <tr>
                <td class="NEXUSName">Organisation</td>
                <td class="NEXUSValue"><bean:write name="cert" property="organisation"/></td>
            </tr>
            <tr>
                <td class="NEXUSName">Organisation Unit</td>
                <td class="NEXUSValue"><bean:write name="cert" property="organisationUnit"/></td>
            </tr>
            <tr>
                <td class="NEXUSName">Country</td>
                <td class="NEXUSValue"><bean:write name="cert" property="country"/></td>
            </tr>
            <tr>
                <td class="NEXUSName">State</td>
                <td class="NEXUSValue"><bean:write name="cert" property="state"/></td>
            </tr>
            <tr>
                <td class="NEXUSName">Location</td>
                <td class="NEXUSValue"><bean:write name="cert" property="location"/></td>
            </tr>
            <tr>
                <td class="NEXUSName">E-Mail</td>
                <td class="NEXUSValue"><bean:write name="cert" property="email"/></td>
            </tr>
            <tr>
                <td class="NEXUSName">Not Valid Before</td>
                <td class="NEXUSValue"><bean:write name="cert" property="notBefore"/></td>
            </tr>
            <tr>
                <td class="NEXUSName">Not Valid After</td>
                <td class="NEXUSValue"><bean:write name="cert" property="notAfter"/></td>
            </tr>
            <tr>
                <td class="NEXUSName">Validity</td>
                <td class="NEXUSValue">
                <logic:equal name="cert" property="valid" value="Okay">
            <font color="green"><b><bean:write name="cert" property="valid"/></b></font> <bean:write name="cert" property="timeRemaining"/>
          </logic:equal>
          <logic:notEqual name="cert" property="valid" value="Okay">
            <font color="red"><b><bean:write name="cert" property="valid"/></b></font>
          </logic:notEqual>
          
                </td>
            </tr>
            <tr>
                <td class="NEXUSName">Fingerprint</td>
                <td class="NEXUSValue"><bean:write name="cert" property="fingerprint"/></td>
            </tr>
        </table>
        
        
        
	</logic:iterate>
	</table>
	
<logic:notEmpty name="certificateChainImportForm" property="caImports">
	<table class="NEXUS_TABLE" width="100%">
		<tr>
			<td colspan="100%" class="NEXUSSection">Missing CA certificate(s) - will be imported into CA keystore.</td>
		</tr>
		<logic:iterate id="cert" name="certificateChainImportForm" property="caImports">
		<table class="NEXUS_TABLE" width="100%">
            <tr>            
            <td colspan="2" class="NEXUSSection"></td>            
            </tr>
            <tr>
                <td class="NEXUSName">Common Name</td>
                <td class="NEXUSValue"><bean:write name="cert" property="commonName"/></td>
            </tr>
            <tr>
                <td class="NEXUSName">Organisation</td>
                <td class="NEXUSValue"><bean:write name="cert" property="organisation"/></td>
            </tr>
            <tr>
                <td class="NEXUSName">Organisation Unit</td>
                <td class="NEXUSValue"><bean:write name="cert" property="organisationUnit"/></td>
            </tr>
            <tr>
                <td class="NEXUSName">Country</td>
                <td class="NEXUSValue"><bean:write name="cert" property="country"/></td>
            </tr>
            <tr>
                <td class="NEXUSName">State</td>
                <td class="NEXUSValue"><bean:write name="cert" property="state"/></td>
            </tr>
            <tr>
                <td class="NEXUSName">Location</td>
                <td class="NEXUSValue"><bean:write name="cert" property="location"/></td>
            </tr>
            <tr>
                <td class="NEXUSName">E-Mail</td>
                <td class="NEXUSValue"><bean:write name="cert" property="email"/></td>
            </tr>
            <tr>
                <td class="NEXUSName">Not Valid Before</td>
                <td class="NEXUSValue"><bean:write name="cert" property="notBefore"/></td>
            </tr>
            <tr>
                <td class="NEXUSName">Not Valid After</td>
                <td class="NEXUSValue"><bean:write name="cert" property="notAfter"/></td>
            </tr>
            <tr>
                <td class="NEXUSName">Validity</td>
                <td class="NEXUSValue">
                <logic:equal name="cert" property="valid" value="Okay">
            <font color="green"><b><bean:write name="cert" property="valid"/></b></font> <bean:write name="cert" property="timeRemaining"/>
          </logic:equal>
          <logic:notEqual name="cert" property="valid" value="Okay">
            <font color="red"><b><bean:write name="cert" property="valid"/></b></font>
          </logic:notEqual>
          
                </td>
            </tr>
            <tr>
                <td class="NEXUSName">Fingerprint</td>
                <td class="NEXUSValue"><bean:write name="cert" property="fingerprint"/></td>
            </tr>
        </table>
	</logic:iterate>
	</table>
</logic:notEmpty>	
	
	<center><logic:messagesPresent>
		<div class="NexusError"><html:errors /></div>
	</logic:messagesPresent></center>

	<table class="NEXUS_BUTTON_TABLE" width="100%">
		<tr>
			<td>&nbsp;</td>
			<td class="BUTTON_RIGHT"><nexus:submit styleClass="button">
				<img src="images/icons/tick.png" name="SUBMIT" class="button">
			</nexus:submit></td>
			<td class="NexusHeaderLink">Accept</td>
		</tr>
	</table>
</html:form></center>
</nexus:fileUploadResponse>