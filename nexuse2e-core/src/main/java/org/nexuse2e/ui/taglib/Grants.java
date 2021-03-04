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

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.nexuse2e.Engine;
import org.nexuse2e.ui.security.AccessController;
import org.nexuse2e.ui.security.AccessController.ParsedRequest;
import org.nexuse2e.ui.structure.ParentalStructureNode;
import org.nexuse2e.ui.structure.StructureException;
import org.nexuse2e.ui.structure.StructureNode;
import org.nexuse2e.ui.structure.StructureService;


/**
 * Displays the menu structure and the assigned grants.
 * @author Sebastian Schulze
 * @date 22.08.2007
 */
public class Grants extends TagSupport {
    private static final long     serialVersionUID            = 1L;
    
    private Map<String,Set<ParsedRequest>> allowedRequests;

    /* (non-Javadoc)
     * @see javax.servlet.jsp.tagext.TagSupport#doStartTag()
     */
    @Override
    public int doStartTag() throws JspException {

        try {
            List<StructureNode> nodes = ( (StructureService) Engine.getInstance().getBeanFactory().getBean( "structureService" ) ).getMenuSkeleton();
            StringBuffer sb = new StringBuffer();
            
            if ( allowedRequests != null ) {
                // add wildcard
                sb.append( "<div style=\"padding-left: 0; background-color: #D8D8E8\">" );
                sb.append( "\t<input type=\"checkbox\" name=\"__grant:*\"" + ( allowedRequests.containsKey( "*" ) ? " checked" : "" ) + "> <span style=\"font-style: italic;\">WILDCARD (grant full access)</span>\n" );
                sb.append( "</div>" );
                iterateTree( nodes, sb, 0, allowedRequests );
            } else {
                throw new JspException( new IllegalArgumentException( "The attribute grantsMap must not be null!" ) );
            }
            
            pageContext.getOut().write( sb.toString() );
                
        } catch ( StructureException e ) {
            throw new JspException( e );
        } catch ( IOException e ) {
            throw new JspException( e );
        }
        return SKIP_BODY;
    }
    
    /**
     * Method to print structure tree recursively.
     * @param nodes The nodes of the structure.
     * @param sb The buffer to append the HTML output.
     * @param indent Number of steps for HTML code indentation.
     * @param allowedRequests A map of allowed requests.
     */
    protected void iterateTree( List<StructureNode> nodes, StringBuffer sb, int indent, Map<String,Set<ParsedRequest>> allowedRequests ) {
        if ( nodes != null ) {
            for ( StructureNode node : nodes ) {
                String target = node.getTarget();
                ParsedRequest pr = AccessController.parseRequestUrl( target );
                sb.append( "<div style=\"padding-left: " + indent * 20 + "; background-color: " + ( indent % 2 == 0 ? "#D0D0E0" : "#D8D8E8" ) + "\">" );
                sb.append( "\t<input type=\"checkbox\" name=\"__grant:" + target + "\""
                        + ( AccessController.hasAccess( allowedRequests, target, pr.getRequestParameters(), true ) ? " checked" : "" )
                        + "> " + node.getLabel() + "\n" );
                sb.append( "</div>" );
                if ( node instanceof ParentalStructureNode ) {
                    ParentalStructureNode parentNode = (ParentalStructureNode) node;
                    iterateTree( parentNode.getChildren(), sb, indent + 1, allowedRequests );
                }
            }
        }
    }        

    /**
     * @return the grantsMap
     */
    public Map<String,Set<ParsedRequest>> getAllowedRequests() {
    
        return allowedRequests;
    }

    
    /**
     * @param allowedRequests the allowed requests to set
     */
    public void setAllowedRequests( Map<String,Set<ParsedRequest>> allowedRequests ) {
    
        this.allowedRequests = allowedRequests;
    }

    
}
