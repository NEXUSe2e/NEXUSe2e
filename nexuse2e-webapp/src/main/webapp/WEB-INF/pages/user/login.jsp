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
<%@ taglib uri="/tags/nexus" prefix="nexus"%>
<%@ taglib uri="/tags/struts-html" prefix="html"%>
<%@ taglib uri="/tags/struts-logic" prefix="logic"%>

<center>

<table class="NEXUS_TABLE" style="width:300px;margin-top:100px">
	<tr>
		<td class="NEXUSScreenName">Login</td>
	</tr>
</table>

<form name="loginForm" method="post" action="LoginCheck.do" autocomplete="off">
<table class="NEXUS_TABLE" style="width:300px;">
	<tr>
		<td class="NEXUSName">Login name</td>
		<td class="NEXUSValue"><input id="loginName" type="text"
			name="user" value=""> <script>
  				// set focus to login name input field
  				dojo.addOnLoad( function() {
  					document.getElementById('loginName').focus();
  				});
  			</script></td>
	</tr>
	<tr>
		<td class="NEXUSName">Password</td>
		<td class="NEXUSValue"><input type="password" name="pass" value="">
		</td>
	</tr>
</table>
<table class="NEXUS_BUTTON_TABLE" style="width:300px;">
	<tr>
		<td>&nbsp;</td>
		<td class="NexusHeaderLink"><input type="reset"
			style="display: none;"> <a
			href="javascript: document.forms['loginForm'].reset();"
			class="button"><img src="images/icons/arrow_rotate_anticlockwise.png" class="button">Reset</a>
		</td>
		<td><input type="submit" style="display: none;"> <a
			href="#" onClick="document.forms['loginForm'].submit();"
			class="button"><img src="images/icons/tick.png" class="button">Log
		in</a></td>
	</tr>
</table>
<table class="NEXUS_BUTTON_TABLE" style="width:300px;">
	<tr>
		<td>${NEXUSe2e_version}</td>
	</tr>
</table>
</form>
</center>

<logic:messagesPresent>
	<div class="NexusError"><html:errors /></div>
</logic:messagesPresent>
