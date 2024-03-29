/**
 * NEXUSe2e Business Messaging Open Source
 * Copyright 2000-2021, direkt gruppe GmbH
 * <p>
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation version 3 of
 * the License.
 * <p>
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.nexuse2e.configuration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nexuse2e.NexusException;

import java.net.UnknownHostException;

public class Nexus3xUUIDGenerator implements IdGenerator {

    private static Logger LOG = LogManager.getLogger(Nexus3xUUIDGenerator.class);

    public String getId() throws NexusException {

        String uuid = null;
        String uid = null;
        String host = null;

        try {
            host = java.net.InetAddress.getLocalHost().getHostName();
            uid = new java.rmi.server.UID().toString();
            uuid = host + "/" + uid;
        } catch (UnknownHostException e) {
            LOG.error("Error while creating MessageId:" + e.getMessage());
            throw new NexusException("unable to create UUID", e);
        }

        return uuid;
    }

}
