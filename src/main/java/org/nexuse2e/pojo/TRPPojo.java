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

// Generated 12.12.2006 10:29:52 by Hibernate Tools 3.2.0.beta6a

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * TRPPojo generated by hbm2java
 */
@XmlType(name = "TRPType")
@XmlAccessorType(XmlAccessType.NONE)
public class TRPPojo implements NEXUSe2ePojo {

    /**
     * 
     */
    private static final long serialVersionUID = -2851100866951449106L;

    // Fields    
    private int               nxTRPId;
    private String            protocol;
    private String            version;
    private String            transport;
    private String            adapterClassName;
    private Date              createdDate;
    private Date              modifiedDate;
    private int               modifiedNxUserId;

    // Constructors

    /** default constructor */
    public TRPPojo() {
        createdDate = new Date();
        modifiedDate = createdDate;
    }

    /** constructor without adapter class name */
    public TRPPojo(
            String protocol,
            String version,
            String transport,
            Date createdDate,
            Date modifiedDate,
            int modifiedNxUserId ) {
        this( protocol, version, transport, null, createdDate, modifiedDate, modifiedNxUserId );
    }
    
    
    /** full constructor */
    public TRPPojo(
            String protocol,
            String version,
            String transport,
            String adapterClassName,
            Date createdDate,
            Date modifiedDate,
            int modifiedNxUserId ) {

        this.protocol = protocol;
        this.version = version;
        this.transport = transport;
        this.adapterClassName = adapterClassName;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
        this.modifiedNxUserId = modifiedNxUserId;
    }

    // Property accessors
    @XmlAttribute
    public int getNxTRPId() {

        return this.nxTRPId;
    }

    public void setNxTRPId( int nxTRPId ) {

        this.nxTRPId = nxTRPId;
    }

    public int getNxId() {
        return nxTRPId;
    }
    
    public void setNxId( int nxId ) {
        this.nxTRPId = nxId;
    }
    
    @XmlAttribute
    public String getProtocol() {

        return this.protocol;
    }

    public void setProtocol( String protocol ) {

        this.protocol = protocol;
    }

    @XmlAttribute
    public String getVersion() {

        return this.version;
    }

    public void setVersion( String version ) {

        this.version = version;
    }

    @XmlAttribute
    public String getTransport() {

        return this.transport;
    }

    public void setTransport( String transport ) {

        this.transport = transport;
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

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( Object obj ) {

        if ( !( obj instanceof TRPPojo ) ) {
            return false;
        }

        if ( nxTRPId == 0 ) {
            return super.equals( obj );
        }

        return nxTRPId == ( (TRPPojo) obj ).nxTRPId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        if ( nxTRPId == 0 ) {
            return super.hashCode();
        }

        return nxTRPId;
    }

    
    @XmlAttribute
    public String getAdapterClassName() {
    
        return adapterClassName;
    }

    
    public void setAdapterClassName( String adapterClassName ) {
    
        this.adapterClassName = adapterClassName;
    }
    
}
