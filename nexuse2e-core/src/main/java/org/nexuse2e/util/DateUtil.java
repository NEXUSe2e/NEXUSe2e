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
package org.nexuse2e.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author gesch
 *
 */
public class DateUtil {

    public static final String  dbFormat      = "yyyyMMddHHmmssSSS";
    
    /**
     * Converts local Date to displayable String using the given Timezone and a optional pattern 
     * 
     * @param time
     * @param timeZone java TimeZone Ids (e.g. GMT+8)
     * @param pattern format pattern (SimpleDateFormat) (null = default)
     * @return display formated String
     */
    public static String localTimeToTimezone( Date time, String timeZone, String pattern ) {

        try {
            if ( pattern == null ) {
                pattern = "yyyy-MM-dd HH:mm:ss.SSS z";
            }

            SimpleDateFormat sdf = new SimpleDateFormat( pattern );
            if ( timeZone != null && !timeZone.equals( "" ) ) {
                sdf.setTimeZone( TimeZone.getTimeZone( timeZone ) );
            }
            return sdf.format( time );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return "#" + time + "#";
    }

    /**
     *  Static method returns the current date in String format used by Nexus 3.x.
     *  @returns java.lang.String Representings Local Date String
     */
    public static String getFormatedNowString() {

        Date date = new Date();
        SimpleDateFormat databaseDateFormat = new SimpleDateFormat( dbFormat );
        String stringDate = databaseDateFormat.format( date );
        return stringDate;
    }
    
    /**
     * @param createdDate
     * @param endDate
     * @return rounded human readable String, e.g '~6 Seconds' or '~8 years'
     */
    public static String getDiffTimeRounded( Date createdDate, Date endDate ) {

        long createMils = createdDate.getTime();
        long endMils = endDate.getTime();

        String diff = "";
        if ( createMils < endMils ) {
            long div = endMils - createMils;
            long sec = 1000;
            long remSecs = div / sec;
            if ( remSecs > 180 ) {
                long min = 1000 * 60;
                long remMins = div / min;
                if ( remMins > 180 ) {
                    long hour = 1000 * 60 * 60;
                    long remHours = div / hour;
                    if ( remHours > 72 ) {
                        long day = hour * 24;
                        long remDays = div / day;
                        if ( remDays > 14 ) {
                            long week = day * 7;
                            long remWeeks = div / week;
                            if ( remWeeks > 8 ) {
                                long month = day * 30;
                                long remMonth = div / month;
                                if ( remMonth > 24 ) {
                                    long year = month * 12;
                                    long remYears = div / year;
                                    diff = "~ " + remYears + " years";
                                } else {
                                    diff = "~ " + remMonth + " months";
                                }
                            } else {
                                diff = "~ " + remWeeks + " weeks";
                            }
                        } else {
                            diff = "~ " + remDays + " days";
                        }
                    } else {
                        diff = "~ " + remHours + " hours";
                    }
                } else {
                    diff = "~ " + remMins + " minutes";
                }
            } else {
                diff = "~ " + remSecs + " seconds";
            }
        }
        return diff;
    }
}
