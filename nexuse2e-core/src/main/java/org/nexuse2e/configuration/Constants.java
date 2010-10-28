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
package org.nexuse2e.configuration;

import org.nexuse2e.pojo.CertificatePojo;
import org.nexuse2e.pojo.ComponentPojo;
import org.nexuse2e.pojo.ConversationPojo;
import org.nexuse2e.pojo.PartnerPojo;
import org.nexuse2e.pojo.PipeletPojo;
import org.nexuse2e.pojo.PipelinePojo;
import org.nexuse2e.pojo.RolePojo;
import org.nexuse2e.pojo.UserPojo;

/**
 * @author gesch
 *
 */
public class Constants extends org.nexuse2e.Constants {

    public static int          CERTIFICATE_FORMAT_PEM              = 1;
    public static int          CERTIFICATE_FORMAT_DER              = 2;

    public static int          PARTNER_TYPE_ALL                    = 0;
    public static int          PARTNER_TYPE_LOCAL                  = 1;
    public static int          PARTNER_TYPE_PARTNER                = 2;

    // to be done

    public static int          CERTIFICATE_TYPE_ALL                = 0;
    public static int          CERTIFICATE_TYPE_LOCAL              = 1;            // server identities, complete p12, private, public and ca chain.
    public static int          CERTIFICATE_TYPE_PARTNER            = 2;            // x509 cert
    public static int          CERTIFICATE_TYPE_CA                 = 3;            // ca certs and intermediate
    public static int          CERTIFICATE_TYPE_REQUEST            = 4;            // pkcs10 request (one per time) 
    public static int          CERTIFICATE_TYPE_PRIVATE_KEY        = 5;            // private key, matching pkcs10
    public static int          CERTIFICATE_TYPE_CACERT_METADATA    = 6;            // password cakeystore
    @Deprecated
    public static int          CERTIFICATE_TYPE_CERT_PART          = 7;            // not finished p12, containing private key and ca signed certificate
    public static int          CERTIFICATE_TYPE_STAGING            = 8;            // complete p12, ready for promote

    // Security settings 

    public static final int    DEFAULT_RSA_KEY_LENGTH              = 1024;
    public static final String DEFAULT_DIGITAL_SIGNATURE_ALGORITHM = "SHA1withRSA";
    public static final String DEFAULT_KEY_ALGORITHM               = "RSA";
    public static final String DEFAULT_CERT_TYPE                   = "X.509";
    public static final String DEFAULT_KEY_STORE                   = "PKCS12";
    public static final String DEFAULT_JCE_PROVIDER                = "BC";

    /**
     * Used to store incomplete Certificate chain parts in rcp client
     */
    //    public static int CERTIFICATE_TYPE_CLIENT_CERT     = 9;
    public static enum ComponentType {
        ALL(0), PIPELET(1), LOGGER(2), SERVICE(3);

        private final int value;

        public int getValue() {

            return value;
        }

        ComponentType( int value ) {

            this.value = value;
        }
    }

    public static int PIPELINE_TYPE_ALL      = 0;
    public static int PIPELINE_TYPE_INBOUND  = 1;
    public static int PIPELINE_TYPE_OUTBOUND = 2;

    public static enum ParameterType {
        UNKNOWN(0, Object.class), STRING(1, String.class), PASSWORD(2, String.class), ENUMERATION(3,
                EnumerationParameter.class), LIST(4, ListParameter.class), BOOLEAN(5, Boolean.class), SERVICE(6,
                String.class);

        private final int   value;
        private final Class<?> type;

        public int getValue() {

            return value;
        }

        public Class<?> getType() {

            return type;
        }

        ParameterType( int value, Class<?> type ) {

            this.value = value;
            this.type = type;
        }
    }

    public static GenericComparator<PipelinePojo> PIPELINECOMPARATOR =
        new GenericComparator<PipelinePojo>( "nxPipelineId", true );
    public static GenericComparator<PartnerPojo> PARTNERCOMPARATOR =
        new GenericComparator<PartnerPojo>( "partnerId", true );
    public static GenericComparator<ComponentPojo> COMPONENTCOMPARATOR =
        new GenericComparator<ComponentPojo>( "type;name", true );
    public static GenericComparator<ComponentPojo> COMPONENT_NAME_COMPARATOR =
        new GenericComparator<ComponentPojo>( "name", true );
    public static GenericComparator<PipeletPojo> PIPELETCOMPARATOR =
        new GenericComparator<PipeletPojo>( "position", true );
    public static GenericComparator<CertificatePojo> CERTIFICATECOMPARATOR =
        new GenericComparator<CertificatePojo>( "name", true );
    public static GenericComparator<ConversationPojo> CONVERTSATIONCOMPARATOR =
        new GenericComparator<ConversationPojo>( "createdDate", false );
    public static GenericComparator<UserPojo> COMPARATOR_USER_BY_NAME   =
        new GenericComparator<UserPojo>( "lastName;firstName;middleName", true );
    public static GenericComparator<RolePojo> COMPARATOR_ROLE_BY_NAME =
        new GenericComparator<RolePojo>( "name", true );

}