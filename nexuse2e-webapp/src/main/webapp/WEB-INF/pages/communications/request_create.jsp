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
<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<%@ taglib uri="/tags/struts-nested" prefix="nested" %>
<%@ taglib uri="/tags/struts-tiles" prefix="tiles" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>
<%@ taglib uri="/tags/nexus" prefix="nexus" %>

<% /*<nexus:helpBar helpDoc="documentation/SSL.htm"/> */ %>

    <center>
        <table class="NEXUS_TABLE" width="100%">
				    <tr>
				        <td>
				        	<nexus:crumbs/>
				        </td>
				    </tr>
				    <tr>
				        <td class="NEXUSScreenName">Certificate Request</td>
				    </tr>
				</table>
        
        <html:form action="RequestSaveRequest.do" method="POST"> 
        
    <table class="NEXUS_TABLE" width="100%">
            <tr>            
            <td colspan="2" class="NEXUSSection">Certificate Properties</td>            
            </tr>
            <tr>
                <td class="NEXUSName">RSA Key Length</td>
                <td class="NEXUSValue">
                	<html:select property="keyLength">
                		<html:option value="1024">1024 Bit</html:option>
                		<html:option value="2048">2048 Bit (default)</html:option>
                		<html:option value="4096">4096 Bit</html:option>
               		</html:select>
             	</td>
            </tr>
            <tr>
                <td class="NEXUSName">Common Name</td>
                <td class="NEXUSValue"><html:text property="commonName" size="50"/></td>
            </tr>
            <tr>
                <td class="NEXUSName">Organisation</td>
                <td class="NEXUSValue"><html:text property="organisation" size="50"/></td>
            </tr>
            <tr>
                <td class="NEXUSName">Organisation Unit</td>
                <td class="NEXUSValue"><html:text property="organisationUnit" size="50"/></td>
            </tr>
            <tr>
                <td class="NEXUSName">Country</td>
                <td class="NEXUSValue"><html:text property="countryCode" size="50"/></td>
            </tr>
            <tr>
                <td class="NEXUSName">State</td>
                <td class="NEXUSValue"><html:text property="state" size="50"/></td>
            </tr>
            <tr>
                <td class="NEXUSName">Location</td>
                <td class="NEXUSValue"><html:text property="location" size="50"/></td>
            </tr>
            <tr>
                <td class="NEXUSName">E-Mail</td>
                <td class="NEXUSValue"><html:text property="email" size="50"/></td>
            </tr>
            <tr>
                <td class="NEXUSName">Password</td>
                <td class="NEXUSValue"><html:password property="password" size="50"/></td>
            </tr>
            <tr>
                <td class="NEXUSName">Password (verify)</td>
                <td class="NEXUSValue"><html:password property="verifyPWD" size="50"/></td>
            </tr>
            
        </table>
        
        <center> 
          <logic:messagesPresent> 
                <div class="NexusError"><html:errors/></div> 
                </logic:messagesPresent>
            </center>
            
        <center>
            <table class="NEXUS_BUTTON_TABLE" width="100%">
                <tr>
                    <td>&nbsp;</td>
                    <td class="BUTTON_RIGHT" style="text-align: right;">
                    	<nobr><nexus:submit styleClass="button"><img src="images/icons/tick.png" class="button">Create Request</nexus:submit></nobr>
                    </td>
                </tr>
            </table>
        </center>
    </html:form>
    </center>