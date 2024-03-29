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
package org.nexuse2e.ui.form;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;

import org.apache.struts.action.ActionForm;
import org.bouncycastle.asn1.x509.X509Name;
import org.nexuse2e.NexusException;
import org.nexuse2e.configuration.CertificateType;
import org.nexuse2e.pojo.CertificatePojo;
import org.nexuse2e.pojo.PartnerPojo;
import org.nexuse2e.util.CertificateUtil;
import org.nexuse2e.util.EncryptionUtil;

/**
 * @author gesch
 * 
 */
public class CollaborationPartnerForm extends ActionForm {

    /**
     * 
     */
    // private static final Logger LOG = LogManager.getLogger( CollaborationPartnerForm.class );
    private static final long                 serialVersionUID = 6867989805361808373L;
    private String                            name;
    private String                            company;
    private int                               nxPartnerId      = 0;
    private String                            partnerId;
    private String                            partnerIdType;
    private int                               type;
    private String                            address1;
    private String                            address2;
    private String                            city;
    private String                            state;
    private String                            zip;
    private String                            country;
    private Date                              created;
    private Date                              lastModified;
    private Collection<String>                choreographies   = new ArrayList<String>();
    private Collection<PartnerPojo>           contacts         = new ArrayList<PartnerPojo>();
    private Collection<PartnerConnectionForm> connections      = new ArrayList<PartnerConnectionForm>();
    private Collection<Certificate>           certificates     = new ArrayList<Certificate>();

    public class Certificate {

        private String id;
        private int nxCertificateId;
        private String commonName;
        private String organisation;
        private String validity;
        private String issuer;
        private String remaining;
        private int remainingDayCount;

        public void setProperties(CertificatePojo cert) {

            try {
                byte[] data = cert.getBinaryData();
                X509Certificate x509 = null;

                if (cert.getType() == CertificateType.PARTNER.getOrdinal()) {
                    x509 = CertificateUtil.getX509Certificate(data);
                } else if (cert.getType() == CertificateType.LOCAL.getOrdinal()) {

                    try {
                        KeyStore jks = KeyStore.getInstance(CertificateUtil.DEFAULT_KEY_STORE, CertificateUtil.DEFAULT_JCE_PROVIDER);
                        jks.load(new ByteArrayInputStream(cert.getBinaryData()), EncryptionUtil.decryptString(cert.getPassword()).toCharArray());
                        if (jks != null) {

                            Enumeration<String> aliases = jks.aliases();
                            if (!aliases.hasMoreElements()) {
                                throw new NexusException("no certificate aliases found");
                            }
                            while (aliases.hasMoreElements()) {
                                String tempAlias = aliases.nextElement();
                                if (jks.isKeyEntry(tempAlias)) {
                                    java.security.cert.Certificate[] certArray = jks.getCertificateChain(tempAlias);
                                    if (certArray != null) {

                                        x509 = (X509Certificate) certArray[0];

                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        throw new NexusException(e);
                    }
                }
                if (x509 == null) {
                    throw new NexusException("X509Certificate is null");
                }

                setCommonName(CertificateUtil.getSubject(x509, X509Name.CN));
                setOrganisation(CertificateUtil.getSubject(x509, X509Name.O));
                setIssuer(CertificateUtil.getIssuer(x509, X509Name.O));

                String valid = "Okay";
                try {
                    x509.checkValidity();
                } catch (CertificateExpiredException e) {
                    valid = "Certificate has expired";
                } catch (CertificateNotYetValidException e) {
                    valid = "Certificate not valid yet";
                }
                setValidity(valid);
                setRemainingDayCount(CertificateUtil.getRemainingDayCount(x509));

                setRemaining(CertificateUtil.getRemainingValidity(x509));

            } catch (NexusException e) {
                e.printStackTrace();
            }

            setId(cert.getName());
            setNxCertificateId(cert.getNxCertificateId());
        }

        public String getId() {

            return id;
        }

        public void setId(String id) {

            this.id = id;
        }

        public int getNxCertificateId() {

            return nxCertificateId;
        }

        public void setNxCertificateId(int seqNo) {

            this.nxCertificateId = seqNo;
        }

        public String getCommonName() {

            return commonName;
        }

        public void setCommonName(String commonName) {

            this.commonName = commonName;
        }

        public String getIssuer() {

            return issuer;
        }

        public void setIssuer(String issuer) {

            this.issuer = issuer;
        }

        public String getOrganisation() {

            return organisation;
        }

        public void setOrganisation(String organisation) {

            this.organisation = organisation;
        }

        public String getValidity() {

            return validity;
        }

        public void setValidity(String validity) {

            this.validity = validity;
        }

        public String getRemaining() {

            return remaining;
        }

        public void setRemaining(String remaining) {

            this.remaining = remaining;
        }

        public int getRemainingDayCount() {
            return remainingDayCount;
        }

        private void setRemainingDayCount(int remainingDayCount) {
            this.remainingDayCount = remainingDayCount;
        }
    }

    public void cleanSettings() {

        setName(null);
        setCompany(null);
        setPartnerId(null);
        setPartnerIdType(null);
        setType(0);
        setAddress1(null);
        setAddress2(null);
        setCity(null);
        setState(null);
        setZip(null);
        setCountry(null);
        setCreated(null);
        setLastModified(null);
    }

    public void setProperties(PartnerPojo pojo) {

        setNxPartnerId(pojo.getNxPartnerId());
        setName(pojo.getName());
        setCompany(pojo.getCompanyName());
        setPartnerId(pojo.getPartnerId());
        setPartnerIdType(pojo.getPartnerIdType());
        setType(pojo.getType());
        setAddress1(pojo.getAddressLine1());
        setAddress2(pojo.getAddressLine2());
        setCity(pojo.getCity());
        setState(pojo.getState());
        setZip(pojo.getZip());
        setCountry(pojo.getCountry());
        setCreated(pojo.getCreatedDate());
        setLastModified(pojo.getModifiedDate());
    }

    public PartnerPojo getProperties(PartnerPojo pojo) {

        pojo.setNxPartnerId(getNxPartnerId());
        pojo.setCompanyName(getCompany());
        pojo.setPartnerId(getPartnerId());
        pojo.setPartnerIdType(getPartnerIdType());
        pojo.setAddressLine1(getAddress1());
        pojo.setAddressLine2(getAddress2());
        pojo.setCity(getCity());
        pojo.setState(getState());
        pojo.setZip(getZip());
        pojo.setCountry(getCountry());
        pojo.setName(getName());
        pojo.setType(getType());
        return pojo;
    }

    public void addCertificate(Certificate cert) {

        if (certificates == null) {
            certificates = new ArrayList<Certificate>();
        }
        certificates.add(cert);
    }

    public void addConnection(PartnerConnectionForm con) {

        if (connections == null) {
            connections = new ArrayList<PartnerConnectionForm>();
        }
        connections.add(con);
    }

    public void addChoreography(String choreographyId) {

        if (choreographies == null) {
            choreographies = new ArrayList<String>();
        }
        choreographies.add(choreographyId);
    }

    public String getAddress1() {

        return address1;
    }

    public void setAddress1(String address1) {

        this.address1 = address1;
    }

    public String getAddress2() {

        return address2;
    }

    public void setAddress2(String address2) {

        this.address2 = address2;
    }

    public Collection<Certificate> getCertificates() {

        return certificates;
    }

    public void setCertificates(Collection<Certificate> certificates) {

        this.certificates = certificates;
    }

    public Collection<String> getChoreographies() {

        return choreographies;
    }

    public void setChoreographies(Collection<String> choreographies) {

        this.choreographies = choreographies;
    }

    public String getCity() {

        return city;
    }

    public void setCity(String city) {

        this.city = city;
    }

    public String getCompany() {

        return company;
    }

    public void setCompany(String company) {

        this.company = company;
    }

    public Collection<PartnerConnectionForm> getConnections() {

        return connections;
    }

    public void setConnections(Collection<PartnerConnectionForm> connections) {

        this.connections = connections;
    }

    public Collection<PartnerPojo> getContacts() {

        return contacts;
    }

    public void setContacts(Collection<PartnerPojo> contacts) {

        this.contacts = contacts;
    }

    public String getCountry() {

        return country;
    }

    public void setCountry(String country) {

        this.country = country;
    }

    public String getPartnerId() {

        return partnerId;
    }

    public void setPartnerId(String partnerId) {

        this.partnerId = partnerId;
    }

    public String getState() {

        return state;
    }

    public void setState(String state) {

        this.state = state;
    }

    public int getType() {

        return type;
    }

    public void setType(int type) {

        this.type = type;
    }

    public String getZip() {

        return zip;
    }

    public void setZip(String zip) {

        this.zip = zip;
    }

    public Date getCreated() {

        return created;
    }

    public void setCreated(Date created) {

        this.created = created;
    }

    public Date getLastModified() {

        return lastModified;
    }

    public void setLastModified(Date lastModified) {

        this.lastModified = lastModified;
    }

    /**
     * @return the partnerIdType
     */
    public String getPartnerIdType() {

        return partnerIdType;
    }

    /**
     * @param partnerIdType
     *            the partnerIdType to set
     */
    public void setPartnerIdType(String partnerIdType) {

        this.partnerIdType = partnerIdType;
    }

    /**
     * @return the name
     */
    public String getName() {

        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {

        this.name = name;
    }

    /**
     * @return the nxPartnerId
     */
    public int getNxPartnerId() {

        return nxPartnerId;
    }

    /**
     * @param nxPartnerId
     *            the nxPartnerId to set
     */
    public void setNxPartnerId(int nxPartnerId) {

        this.nxPartnerId = nxPartnerId;
    }
}
