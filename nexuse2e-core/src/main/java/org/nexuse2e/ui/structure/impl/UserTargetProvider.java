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
/**
 * *#* �2006 The Tamalpais Group, Inc., Xioma *+*
 */
package org.nexuse2e.ui.structure.impl;

import java.util.ArrayList;
import java.util.List;

import org.nexuse2e.configuration.Constants;
import org.nexuse2e.configuration.EngineConfiguration;
import org.nexuse2e.pojo.UserPojo;
import org.nexuse2e.ui.structure.ParentalStructureNode;
import org.nexuse2e.ui.structure.StructureNode;
import org.nexuse2e.ui.structure.TargetProvider;

/**
 * {@link TargetProvider} that provides nodes for all known users.
 * @author Sebastian Schulze
 * @date 25.01.2007
 */
public class UserTargetProvider implements TargetProvider {

    public List<StructureNode> getStructure(
            StructureNode pattern, ParentalStructureNode parent, EngineConfiguration engineConfiguration ) {

        List<StructureNode> list = new ArrayList<StructureNode>();

        List<UserPojo> users = engineConfiguration.getUsers( Constants.COMPARATOR_USER_BY_NAME );

        for ( UserPojo user : users ) {
            if ( user.isVisible() ) {
                list.add( new PageNode( pattern.getTarget() + "?nxUserId=" + user.getNxUserId(), user.getLastName() + ", "
                        + user.getFirstName() + ( user.getMiddleName() != null ? " " + user.getMiddleName() : "" ),
                        pattern.getIcon() ) );
            }
        }

        return list;
    }

}
