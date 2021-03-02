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
package org.nexuse2e.tools.mapping.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author guido.esch
 */
public class XMLBlock {

    private String              blockID;
    private List<XMLBlockEntry> blockEntries;
    private int                 siblingSequence;

    /**
     * @param entry
     */
    public void addBlockEntry( XMLBlockEntry entry ) {

        if ( blockEntries == null ) {
            blockEntries = new ArrayList<XMLBlockEntry>();
        }
        blockEntries.add( entry );
    }

    /**
     * @param entryID
     * @return entry
     */
    public XMLBlockEntry getEntryByEntryID( String entryID ) {

        if ( blockEntries == null ) {
            blockEntries = new ArrayList<XMLBlockEntry>();
        }
        Iterator<XMLBlockEntry> i = blockEntries.iterator();
        while ( i.hasNext() ) {
            XMLBlockEntry entry = i.next();
            if ( entry.getEntryID().equals( entryID ) ) {
                return entry;
            }
        }
        return null;
    }

    /**
     * @return entries
     */
    public List<XMLBlockEntry> getBlockEntries() {

        return blockEntries;
    }

    /**
     * @param blockentries
     */
    public void setBlockEntries( List<XMLBlockEntry> blockentries ) {

        this.blockEntries = blockentries;
    }

    /**
     * @return id
     */
    public String getBlockID() {

        return blockID;
    }

    /**
     * @param blockID
     */
    public void setBlockID( String blockID ) {

        this.blockID = blockID;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer buffer = new StringBuffer();
        buffer.append( "Block:" ); //$NON-NLS-1$
        buffer.append( blockID + " SiblingSequence:" + siblingSequence + "\n" ); //$NON-NLS-1$
        if ( blockEntries != null ) {
            for ( XMLBlockEntry xmlEntry : blockEntries ) {
                buffer.append( "  " + xmlEntry ); //$NON-NLS-1$
                buffer.append( "\n" ); //$NON-NLS-1$
            }
        }

        return buffer.toString();
    }

    public int getSiblingSequence() {

        return siblingSequence;
    }

    public void setSiblingSequence( int siblingSequence ) {

        this.siblingSequence = siblingSequence;
    }
}