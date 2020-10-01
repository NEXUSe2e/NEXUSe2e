/**
 *  NEXUSe2e Business Messaging Open Source
 *  Copyright 2000-2009, Tamgroup and X-ioma GmbH
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation version 2.1 of
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
package org.nexuse2e.ui.ajax;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.nexuse2e.Engine;
import org.nexuse2e.configuration.EngineConfiguration;
import org.nexuse2e.pojo.UserPojo;
import org.nexuse2e.ui.action.NexusE2EAction;
import org.nexuse2e.ui.ajax.dojo.TreeProvider;

/**
 * @author Sebastian Schulze
 * @date 13.12.2006
 */
public class AjaxServlet extends HttpServlet {

    /**
     * 
     */
    private static final long   serialVersionUID = -6583444281593200091L;

    private static final Logger LOG              = Logger.getLogger( AjaxServlet.class );

    private static final String PATH_MENU        = "/menu";

    private static final String PATH_COMMANDS    = "/commands";

    private static final String PATH_LOGIN       = "/login";

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException,
            IOException {

        setHeaders(response);
        LOG.trace( "PROCESSING REQUEST" );

        try {
            String result = null;
            if ( PATH_MENU.equals( request.getPathInfo() ) ) {
                result = new TreeProvider().handleRequest( request );
            } else if ( PATH_COMMANDS.equals( request.getPathInfo() ) ) {
                result = new TreeProvider().handleRequest( request );
            } else if (PATH_LOGIN.equals(request.getPathInfo())) {
                result = "{ \"name\" : 5 }";
            } else {
                LOG.warn( "Unknown path requested: path=" + request.getPathInfo() );
            }

            if ( result != null ) {
                LOG.trace( "Result: " + result );
                response.setContentType( "text/json" );
                response.setStatus( HttpServletResponse.SC_OK );
                Writer writer = response.getWriter();
                writer.write( result );
                writer.flush();
            } else {
                response.setStatus( HttpServletResponse.SC_NOT_FOUND );
            }
        } catch ( JSONException e ) {
            LOG.error( e );
            response.setStatus( HttpServletResponse.SC_BAD_REQUEST );
        }

        LOG.trace( "RETURNING REQUEST" );
        // super.doGet( request, response );
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setHeaders(response);
        String data = readAll(request.getInputStream());

        EngineConfiguration engineConfig = Engine.getInstance().getCurrentConfiguration();
        UserPojo userInstance = engineConfig.getUserByLoginName("admin");
        if (userInstance != null) {
            HttpSession session = request.getSession();
            session.setAttribute(NexusE2EAction.ATTRIBUTE_USER, userInstance);
            response.setStatus(200);
        } else {
            super.doPost(request, response);
        }
    }

    private static String readAll(InputStream in) throws IOException {
        StringBuilder sb = new StringBuilder();
        InputStreamReader inr = new InputStreamReader(in, StandardCharsets.UTF_8);
        BufferedReader br = new BufferedReader(inr);
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    public String getStringRepresentation( HttpServletRequest request ) {

        StringBuffer sb = new StringBuffer();

        sb.append( "\n" );
        sb.append( "Protocol: " + request.getProtocol() );
        sb.append( "\n" );
        sb.append( "Method: " + request.getMethod() );
        sb.append( "\n" );
        sb.append( "ContextPath: " + request.getContextPath() );
        sb.append( "\n" );
        sb.append( "PathInfo: " + request.getPathInfo() );
        sb.append( "\n" );
        sb.append( "RequestURL: " + request.getRequestURL() );
        sb.append( "\n" );
        sb.append( "QueryString: " + request.getQueryString() );
        sb.append( "\n" );
        sb.append( "ContentType: " + request.getContentType() );
        sb.append( "\n" );
        sb.append( "--- Headers ---" );
        sb.append( "\n" );
        Enumeration<?> headerNames = request.getHeaderNames();
        while ( headerNames.hasMoreElements() ) {
            String currName = (String) headerNames.nextElement();
            sb.append( currName + ": " + request.getHeader( currName ) );
            sb.append( "\n" );
        }
        sb.append( "--- Attributes ---" );
        sb.append( "\n" );
        Enumeration<?> attributeNames = request.getAttributeNames();
        while ( attributeNames.hasMoreElements() ) {
            String currName = (String) attributeNames.nextElement();
            sb.append( currName + ": " + request.getAttribute( currName ) );
            sb.append( "\n" );
        }
        sb.append( "--- Parameters ---" );
        sb.append( "\n" );
        Enumeration<?> paramNames = request.getParameterNames();
        while ( paramNames.hasMoreElements() ) {
            String currName = (String) paramNames.nextElement();
            sb.append( currName + ": " + request.getParameter( currName ) );
            sb.append( "\n" );
        }
        sb.append( "\n" );

        return sb.toString();
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setHeaders(resp);
        super.doOptions(req, resp);
    }

    private void setHeaders(HttpServletResponse response) {
        // TODO Only for dev environment
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "Content-Language, Content-Type, Accept, Accept-Language");
    }
}
