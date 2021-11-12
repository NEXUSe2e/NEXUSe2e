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
package org.nexuse2e;

public enum MessageBackendStatus {
    // PROCESSED_OUTBOUND is a legacy status and is nut being set anymore
    FAILED(-1), UNKNOWN(0), PROCESSED(1), PROCESSED_OUTBOUND(2);

    int ordinal = 0;

    MessageBackendStatus(int ordinal) {
        this.ordinal = ordinal;
    }

    public int getOrdinal() {
        return this.ordinal;
    }

    public static MessageBackendStatus getByOrdinal(int ordinal) {
        if (-1 <= ordinal) {
            for (MessageBackendStatus oneType : MessageBackendStatus.values()) {
                if (oneType.getOrdinal() == ordinal) {
                    return oneType;
                }
            }
        }
        throw new IllegalArgumentException("Parameter must be the ordinal of a valid MessageStatus!");
    }
}