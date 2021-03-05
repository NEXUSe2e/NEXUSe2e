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
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ taglib uri="/tags/nexus" prefix="nexus"%>

<% /*<nexus:helpBar helpDoc="documentation/SSL.htm" /> */ %>

<nexus:fileUploadResponse>

<logic:equal name="protectedFileAccessForm" property="status" value="3">
	<script type="text/javascript">
window.open('DataSaveAs.do?type=cacerts','Save as...')
</script>
</logic:equal>

<center>
<table class="NEXUS_TABLE" width="100%">
	<tr>
		<td><nexus:crumbs /></td>
	</tr>
	<tr>
		<td class="NEXUSScreenName">CA Certificates</td>
	</tr>
</table>

<table class="NEXUS_TABLE">
	<tr>
		<td colspan="100%" class="NEXUSSection">Certificates</td>
	</tr>

	<logic:iterate id="cert" name="collection">
		<tr>
			<td class="NEXUSName"><nexus:link styleClass="NexusLink"
				href="CACertificateView.do?nxCertificateId=${cert.nxCertificateId}">
				<logic:empty name="cert" property="alias">n/a</logic:empty>
				<logic:notEmpty name="cert" property="alias"><bean:write name="cert" property="alias" /></logic:notEmpty>
			</nexus:link></td>
			<td class="NEXUSName"><bean:write name="cert"
				property="commonName" /></td>
			<td class="NEXUSValue"><logic:equal name="cert" property="valid"
				value="Okay">
				<font color="green"><b><bean:write name="cert"
					property="valid" /></b></font>
				<bean:write name="cert" property="timeRemaining" />
			</logic:equal> <logic:notEqual name="cert" property="valid" value="Okay">
				<font color="red"><b><bean:write name="cert"
					property="valid" /></b></font>
			</logic:notEqual></td>
			<td class="NEXUSValue"><bean:write name="cert" property="fingerprint"/></td>
		</tr>
	</logic:iterate>
</table>

<table class="NEXUS_BUTTON_TABLE">
	<tr>
		<td class="NexusHeaderLink" style="text-align: right;"><nexus:link
			href="CACertificateAddSingleCert.do" styleClass="button">
			<img src="images/icons/medal_gold_add.png" class="button">Add CA Certificate</nexus:link>
		</td>
		<td class="NexusHeaderLink" style="text-align: right;"><nexus:link
			href="CACertificateImportKeyStore.do" styleClass="button">
			<img src="images/icons/server_key.png" class="button">Import CA KeyStore</nexus:link>
		</td>
		<td class="NexusHeaderLink" style="text-align: right;"><nexus:link
			href="CACertificateExportKeyStore.do" styleClass="button">
			<img src="images/icons/disk.png" class="button">Export CA KeyStore to Filesystem</nexus:link>
		</td>
	</tr>
</table>
</center>
</nexus:fileUploadResponse>