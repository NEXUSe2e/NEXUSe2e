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

import java.util.List;

import org.nexuse2e.configuration.EngineConfiguration;

/**
 * This interface provides structured data to render the page topology. 
 * 
 * @author Sebastian Schulze
 */

public interface TargetProvider {

    /**
     * Returns the instances of a given pattern node.
     * @param pattern The pattern node.
     * @param parent The node that will be set as parent of the returned instances.
     * @param engineConfiguration The applicable engine configuration
     * @return The instances of the given <code>pattern</code>.
     *         The returned instances are expected to be of the same subtype as the given <code>pattern</code>.
     */
    List<StructureNode> getStructure(
            StructureNode pattern, ParentalStructureNode parent, EngineConfiguration engineConfiguration );
}
