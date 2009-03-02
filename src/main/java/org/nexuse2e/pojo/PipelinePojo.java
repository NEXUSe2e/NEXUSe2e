/**
 *  NEXUSe2e Business Messaging Open Source
 *  Copyright 2000-2009, Tamgroup and X-ioma GmbH
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation version 2.1 of
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
package org.nexuse2e.pojo;

// Generated 17.11.2006 11:17:45 by Hibernate Tools 3.2.0.beta6a

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

/**
 * PipelinePojo generated by hbm2java
 */
@XmlType(name = "PipelineType")
@XmlAccessorType(XmlAccessType.NONE)
public class PipelinePojo implements NEXUSe2ePojo {

    /**
     * 
     */
    private static final long       serialVersionUID = -6721613192309857469L;

    // Fields    
    private int                     nxPipelineId;
    private TRPPojo                 trp;
    private boolean                 outbound;
    private boolean                 frontend;
    private Date                    createdDate;
    private Date                    modifiedDate;
    private int                     modifiedNxUserId;
    private String                  description;
    private String                  name;
    @XmlElementWrapper(name = "Pipelets")
    @XmlElement(name = "Pipelet")
    private Collection<PipeletPojo> pipelets;
    
    private int                     nxTrpId;

    // Constructors

    /** default constructor */
    public PipelinePojo() {
        createdDate = new Date();
        modifiedDate = createdDate;
    }

    /** minimal constructor */
    public PipelinePojo( boolean outbound, Date createdDate, Date modifiedDate,
            int modifiedNxUserId, String name ) {

        this.outbound = outbound;
        
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
        this.modifiedNxUserId = modifiedNxUserId;
        this.name = name;
    }

    /** full constructor */
    public PipelinePojo( boolean outbound, Date createdDate,
            Date modifiedDate, int modifiedNxUserId, String description, String name, Collection<PipeletPojo> pipelets ) {

        this.outbound = outbound;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
        this.modifiedNxUserId = modifiedNxUserId;
        this.description = description;
        this.name = name;
        this.pipelets = pipelets;
    }
    
    /**
     * Creates a <code>PipelinePojo</code> that is a copy of the given pipeline.
     * @param pipeline The object to create a copy of.
     */
    public PipelinePojo( PipelinePojo pipeline ) {
        this(
                pipeline.outbound,
                pipeline.createdDate,
                pipeline.modifiedDate,
                pipeline.modifiedNxUserId,
                pipeline.description,
                pipeline.name,
                pipeline.pipelets );
    }

    // Property accessors
    @XmlAttribute
    public int getNxPipelineId() {

        return this.nxPipelineId;
    }

    public void setNxPipelineId( int nxPipelineId ) {

        this.nxPipelineId = nxPipelineId;
    }

    public int getNxId() {
        return nxPipelineId;
    }
    
    public void setNxId( int nxId ) {
        this.nxPipelineId = nxId;
    }
    
    @XmlAttribute
    public boolean isOutbound() {

        return this.outbound;
    }

    public void setOutbound( boolean outbound ) {

        this.outbound = outbound;
    }

    public Date getCreatedDate() {

        return this.createdDate;
    }

    public void setCreatedDate( Date createdDate ) {

        this.createdDate = createdDate;
    }

    public Date getModifiedDate() {

        return this.modifiedDate;
    }

    public void setModifiedDate( Date modifiedDate ) {

        this.modifiedDate = modifiedDate;
    }

    public int getModifiedNxUserId() {

        return this.modifiedNxUserId;
    }

    public void setModifiedNxUserId( int modifiedNxUserId ) {

        this.modifiedNxUserId = modifiedNxUserId;
    }

    @XmlAttribute
    public String getDescription() {

        return this.description;
    }

    public void setDescription( String description ) {

        this.description = description;
    }

    @XmlAttribute
    public String getName() {

        return this.name;
    }

    public void setName( String name ) {

        this.name = name;
    }

    public Collection<PipeletPojo> getPipelets() {

        return this.pipelets;
    }

    public Collection<PipeletPojo> getForwardPipelets() {

        List<PipeletPojo> list = new ArrayList<PipeletPojo>();
        if (pipelets != null) {
            for (PipeletPojo p : pipelets) {
                if (p.isForward()) {
                    list.add( p );
                }
            }
        }
        return list;
    }

    public Collection<PipeletPojo> getReturnPipelets() {

        List<PipeletPojo> list = new ArrayList<PipeletPojo>();
        if (pipelets != null) {
            for (PipeletPojo p : pipelets) {
                if (!p.isForward()) {
                    list.add( p );
                }
            }
        }
        return list;
    }

    public void setPipelets( Collection<PipeletPojo> pipelets ) {

        this.pipelets = pipelets;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( Object obj ) {

        if ( !( obj instanceof PipelinePojo ) ) {
            return false;
        }

        if ( nxPipelineId == 0 ) {
            return super.equals( obj );
        }

        return nxPipelineId == ( (PipelinePojo) obj ).nxPipelineId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        if ( nxPipelineId == 0 ) {
            return super.hashCode();
        }

        return nxPipelineId;
    }

    @XmlAttribute
    public boolean isFrontend() {

        return frontend;
    }

    public void setFrontend( boolean frontend ) {

        this.frontend = frontend;
    }

    public TRPPojo getTrp() {

        return trp;
    }

    public void setTrp( TRPPojo trp ) {

        this.trp = trp;
    }
    
    /**
     * Required for JAXB
     * @return
     */
    @XmlAttribute
    public int getNxTrpId() {
        if ( this.trp != null ) {
            return this.trp.getNxTRPId();

        }
        return nxTrpId;
    }
    
    /**
     * Required for JAXB
     * @param trpId
     */
    public void setNxTrpId( int trpId ) {
        this.nxTrpId = trpId;
    }
    
    public boolean isFrontendInbound() {
        return isFrontend() && !isOutbound();
    }

    public boolean isFrontendOutbound() {
        return isFrontend() && isOutbound();
    }
    
    public boolean isBackendInbound() {
        return !isFrontend() && !isOutbound();
    }
    
    public boolean isBackendOutbound() {
        return !isFrontend() && isOutbound();
    }
}
