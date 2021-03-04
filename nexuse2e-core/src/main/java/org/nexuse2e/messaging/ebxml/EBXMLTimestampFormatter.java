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
package org.nexuse2e.messaging.ebxml;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.nexuse2e.NexusException;
import org.nexuse2e.messaging.TimestampFormatter;
import org.nexuse2e.util.DateTime;

public class EBXMLTimestampFormatter implements TimestampFormatter {

    public String getTimestamp( Date time ) {

        SimpleDateFormat ebXMLDateFormat = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss'Z'" );
        ebXMLDateFormat.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
        return ebXMLDateFormat.format( time );
    }

    public Date getTimestamp( String time ) throws NexusException {

        try {
            DateTime date = new DateTime( time );
            return date.toDate();
        } catch ( ParseException e ) {
            try {
                SimpleDateFormat ebXMLDateFormat = new SimpleDateFormat( "yyyyMMdd'T'HHmmss.SSS'Z'" );
                return ebXMLDateFormat.parse( time );
            } catch ( ParseException pe3 ) {
                throw new NexusException( "Error while parsing timestamp:", e );
            }
        }
    }

    /**
     * @param args
     */
    public static void main( String[] args ) {

        if ( args != null && args.length > 0 ) {
            try {
                System.out.println( "Input: " + args[0] );
                System.out.println( "parsed date: " + new EBXMLTimestampFormatter().getTimestamp( args[0] ) );
            } catch ( NexusException e ) {
                e.printStackTrace();
            }
        } else {
            System.err.println( "Wrong number of parameters. Usage: EBXMLTimestampFormatter <date string>" );
            return;
        }
    }
}
