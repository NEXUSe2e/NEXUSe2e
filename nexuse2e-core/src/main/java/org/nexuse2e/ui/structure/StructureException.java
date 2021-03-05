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
package org.nexuse2e.ui.structure;

import org.nexuse2e.NexusException;

/**
 * A StructureException can be thrown during the build of the page structure.
 * @author Sebastian Schulze
 * @date 11.12.2006
 */
public class StructureException extends NexusException {

    /**
     * 
     */
    private static final long serialVersionUID = 2873947570995799843L;

    public StructureException( String message ) {

        super( message );
    }

    public StructureException( String message, Exception nested ) {

        super( message, nested );
    }

    public StructureException( Exception nested ) {

        super( nested );
    }
}
