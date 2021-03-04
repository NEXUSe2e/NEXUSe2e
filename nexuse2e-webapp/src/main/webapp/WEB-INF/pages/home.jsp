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
<%@ page import="java.util.Set" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.io.File" %>
<%@ page import="org.nexuse2e.Engine" %>
<%@ page import="org.hibernate.internal.SessionFactoryImpl" %>
<%@ page import="org.hibernate.service.jdbc.connections.internal.DatasourceConnectionProviderImpl" %>
<%@ page import="com.mchange.v2.c3p0.ComboPooledDataSource" %>
<%@ page import="org.springframework.beans.BeansException" %>

<%@ taglib uri="/tags/struts-bean" prefix="bean"%>
<%@ taglib uri="/tags/struts-html" prefix="html"%>
<%@ taglib uri="/tags/struts-nested" prefix="nested"%>
<%@ taglib uri="/tags/struts-tiles" prefix="tiles"%>
<%@ taglib uri="/tags/struts-logic" prefix="logic"%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c"%>
<%@ taglib uri="/tags/nexus" prefix="nexus"%>

<% /*<nexus:helpBar helpDoc="documentation/NEXUSe2e.html" /> */ %>

<table class="NEXUS_TABLE" width="100%">
	<tr>
		<td><nexus:crumbs /></td>
	</tr>
	<tr>
		<td class="NEXUSScreenName">NEXUSe2e</td>
	</tr>
</table>

<table class="NEXUS_TABLE" width="100%">
	<tr>
		<td class="NEXUSSection">Property</td>
		<td class="NEXUSSection">Value</td>
	</tr>
	<tr>
		<td class="NEXUSNameNoWidth">NEXUSe2e Version</td>
		<td class="NEXUSNameNoWidth">${NEXUSe2e_version}</td>
	</tr>
	<tr>
		<td class="NEXUSNameNoWidth">NEXUSe2e configuration used</td>
		<td class="NEXUSNameNoWidth">${NEXUSe2e_configpath}</td>
	</tr>
	<tr>
		<td class="NEXUSNameNoWidth">Java Version</td>
		<td class="NEXUSNameNoWidth">${java_version}</td>
	</tr>
	<tr>
		<td class="NEXUSNameNoWidth">Java Home</td>
		<td class="NEXUSNameNoWidth">${java_home}</td>
	</tr>
	<tr>
		<td class="NEXUSNameNoWidth">Java Classpath</td>
		<td class="NEXUSNameNoWidth">${java_classpath}</td>
	</tr>
	<tr>
		<td class="NEXUSNameNoWidth">Service Uptime</td>
		<td class="NEXUSNameNoWidth">${service_uptime}</td>
	</tr>
	<tr>
		<td class="NEXUSNameNoWidth">Engine Uptime</td>
		<td class="NEXUSNameNoWidth">${engine_uptime}</td>
	</tr>
	<tr>
		<td class="NEXUSNameNoWidth">License</td>
		<td class="NEXUSNameNoWidth"><a href="html/lgpl.html" target="#blank">GNU Lesser General Public License</a> (LGPL), Version 2.1</td>
	</tr>
	<tr>
		<td class="NEXUSNameNoWidth">JVM Charset Encoding</td>
		<td class="NEXUSNameNoWidth"><%= java.nio.charset.Charset.defaultCharset().name() %></td>
	</tr>
	<tr>
		<td class="NEXUSNameNoWidth">NEXUSe2e Charset Encoding</td>
		<td class="NEXUSNameNoWidth"><%= org.nexuse2e.Engine.getInstance().getDefaultCharEncoding() %></td>
	</tr>
	<tr>
		<td class="NEXUSNameNoWidth">JVM Default Timezone</td>
		<td class="NEXUSNameNoWidth"><%= java.util.TimeZone.getDefault().getID() %></td>
	</tr>
	<tr>
		<td class="NEXUSNameNoWidth">JVM Maximum Memory (b)</td>
		<td class="NEXUSNameNoWidth"><%= String.valueOf(Runtime.getRuntime().maxMemory()) %></td>
	</tr>
	<tr>
		<td class="NEXUSNameNoWidth">Cipher limitations / JCE status</td>
		<td class="NEXUSNameNoWidth"><%= org.nexuse2e.Engine.getInstance().getJCEInstalledStatus() %></td>
	</tr>

	<tr>
		<td class="NEXUSNameNoWidth">Database Dialect</td>
		<td class="NEXUSNameNoWidth"><%= ((SessionFactoryImpl) Engine.getInstance().getBeanFactory().getBean("hibernateSessionFactory")).getDialect() %></td>
	</tr>
	<tr>
		<td class="NEXUSNameNoWidth">Database Driver Class</td>
		<td class="NEXUSNameNoWidth">
			<%
				try {
					String driverClass = ((ComboPooledDataSource)((DatasourceConnectionProviderImpl)((SessionFactoryImpl)Engine.getInstance().getBeanFactory().getBean("hibernateSessionFactory")).getConnectionProvider()).getDataSource()).getDriverClass();
					out.print(driverClass);
				} catch (Exception e) {
					out.print("n.a.");
				}
			%>
		</td>
	</tr>
	<tr>
		<td class="NEXUSNameNoWidth">Database URL</td>
		<td class="NEXUSNameNoWidth">
			<%
				try {
					String dburl = ((ComboPooledDataSource)((DatasourceConnectionProviderImpl)((SessionFactoryImpl) Engine.getInstance().getBeanFactory().getBean("hibernateSessionFactory")).getConnectionProvider()).getDataSource()).getJdbcUrl();
					out.print(dburl);
				} catch (Exception e) {
					out.print("n.a.");
				}
			%>
		</td>
	</tr>

	<tr>
        <td class="NEXUSNameNoWidth">JVM Parameters</td>
        <td class="NEXUSNameNoWidth">
            <div style="max-height: 200px;white-space: normal;overflow: auto;">
            <%
                try {
                    Set<Map.Entry<Object,Object>> paramSet = System.getProperties().entrySet();
                    for (Map.Entry param : paramSet) {
                        out.print(param.getKey()+"="+param.getValue()+"<br>");
                    }
                } catch (Exception e) {
                    %>
                    n.a.
                    <%
                }
            %>
            </div>
        </td>
    </tr>
    <tr>
        <td class="NEXUSNameNoWidth">Libraries</td>
        <td class="NEXUSNameNoWidth">
            <div style="max-height: 200px;white-space: normal;overflow: auto;">
				<table class="NEXUS_TABLE" width="100%">
					<td style="width:40%" class="NEXUSSection">Filename</td>
					<td style="width:30%" class="NEXUSSection">Version</td>
					<td style="width:30%" class="NEXUSSection">Size (kb)</td>
				<%
                    try {
						File libDir = new File(System.getProperty("webapp.root"),"WEB-INF/lib");
						if(libDir.isDirectory()) {
							File[] files = libDir.listFiles();
							for (File file : files) {
							%>
								<tr>
							<%
								String ver = "na";
								String name = file.getName();
								if(name.endsWith(".jar")) {
									name = name.substring(0,name.length()-4);
									if(name.lastIndexOf("-") > 0) {
										ver = name.substring(name.lastIndexOf("-")+1, name.length());
										name = name.substring(0,name.lastIndexOf("-"));
									}
								}
								String size = ""+file.length()/1000d;
							out.print("<td class=\"NEXUSNameNoWidth\">"+name+"</td><td class=\"NEXUSNameNoWidth\">"+ver+"</td><td class=\"NEXUSNameNoWidth\">"+size+"</td>");
							%>
								</tr>
							<%
							}
						} else {
							out.print("no lib dir found");
						}

                    } catch (Exception e) {
                %>
                n.a.
                <%
                    }
                %>
                </table>
            </div>
        </td>
    </tr>
</table>
<table colspan="4" class="NEXUS_TABLE" width="100%">

	<tr>
		<td class="NEXUSSection">Instances</td>
		
	</tr>
</table>

	<nested:iterate id="instance" name="instances">
		
		
			<table class="NEXUS_TABLE">
				<tr>
					<td class="NEXUSSection" style="width:100px;">
						Label:
					</td>
					<td class="NEXUSSection">
						${instance.label}
					</td>
				</tr>
				<bean:size id="size" name="instance" property="commands"/>
				<% 
				int filler = 0;
					if(size < 4) {
						filler = 4-size;
						size = 4;
					} 
				%>
				<nested:iterate id="command" indexId="counter" name="instance" property="commands">
					<tr>
						<% if(counter == 0) {%>
						<td style="text-align: center;background-color:white;" rowspan="<%=size %>" class="NEXUSNameNoWidth">
							<img src="images/icons/icon-lights-${instance.statusColor}.png" border="0" alt="" class="button">
						</td>
						<% } %>
						
						<td class="NEXUSNameNoWidth">
							<nexus:link href="InstanceController.do?instanceId=${instance.id}&commandId=${command.name}" styleClass="NexusLink">
            					${command.label}
            				</nexus:link>
						</td>
					</tr>
				</nested:iterate>
				<% if (filler > 0){
					for(int i = 0; i < filler; i++){%>
					<tr>
						<td class="NEXUSNameNoWidth">&nbsp;
						</td>
					</tr>
					<%	
					}
				}
				%>
			</table>
			
		
	</nested:iterate>
${description}
<center><logic:messagesPresent>
	<div class="NexusError"><html:errors /></div>
</logic:messagesPresent></center>
