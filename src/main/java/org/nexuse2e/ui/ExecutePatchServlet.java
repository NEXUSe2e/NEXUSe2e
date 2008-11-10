package org.nexuse2e.ui;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.nexuse2e.patch.Patch;
import org.nexuse2e.patch.PatchException;
import org.nexuse2e.patch.PatchReporter;
import org.nexuse2e.ui.form.PatchManagementForm;

/**
 * This <code>Servlet</code> executes a patch, delegating the patch output directly to the
 * <code>ServletOutputStream</code>.
 *
 * @author Jonas Reese
 * @version $LastChangedRevision:  $ - $LastChangedDate:  $ by $LastChangedBy:  $
 */
public class ExecutePatchServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static Logger LOG = Logger.getLogger( ExecutePatchServlet.class );

    
    
    @Override
    public void service( HttpServletRequest request, HttpServletResponse res ) throws ServletException, IOException {

        Patch patch = null;
        int index = Integer.parseInt( request.getParameter( "index" ) );
        
        PatchManagementForm form = (PatchManagementForm) request.getSession().getAttribute( "patchManagementForm" );
        
        if (form != null) {
            List<Patch> patches = form.getPatchBundles().getPatches();
            if (index >= 0 && index < patches.size()) {
                patch = patches.get( index );
            }
        }
        
        if (patch == null) {
            throw new ServletException( "Patch could not be located" );
        }
        
        final PrintWriter pw = res.getWriter();
        pw.println( "<html><head><title>" + patch.getPatchName() + " output</title>" +
                "<script type=\"text/javascript\">var lockit=setInterval(\"window.scrollTo(0,2000000000)\",10)</script></head>" +
                "<body onLoad=\"clearInterval(lockit)\"><pre>" );
        patch.setPatchReporter( new PatchReporter() {
            public void detail( String message ) {
                String s = " DETAIL: " + message;
                pw.println( s );
                pw.flush();
                LOG.trace( s );
            }

            public void error( String message ) {
                String s = "  ERROR: " + message;
                pw.println( s );
                pw.flush();
                LOG.error( s );
            }

            public void fatal( String message ) {
                String s = "  FATAL: " + message;
                pw.println( s );
                pw.flush();
                LOG.fatal( s );
            }

            public void info( String message ) {
                String s = "   INFO: " + message;
                pw.println( s );
                pw.flush();
                LOG.info( s );
            }

            public void warning( String message ) {
                String s = "WARNING: " + message;
                pw.println( s );
                pw.flush();
                LOG.warn( s );
            }
            
        } );
        try {
            patch.executePatch();
        } catch (PatchException e) {
            throw new ServletException( e );
        }
        pw.println( "</pre></body></html>" );
    }
}
