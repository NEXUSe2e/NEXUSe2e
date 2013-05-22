
/*
 * 
 */

package org.nexuse2e.service.ws.aggateway.wsdl;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.Service;

/**
 * This class was generated by Apache CXF 2.3.2
 * 2013-04-24T15:21:51.899+02:00
 * Generated source version: 2.3.2
 * 
 */


@WebServiceClient(name = "AgGatewayDocumentExchange", 
                  wsdlLocation = "classpath:org/nexuse2e/integration/AgGateway.wsdl",
                  targetNamespace = "urn:aggateway:names:ws:docexchange") 
public class AgGatewayDocumentExchange extends Service {

    public final static URL WSDL_LOCATION;

    public final static QName SERVICE = new QName("urn:aggateway:names:ws:docexchange", "AgGatewayDocumentExchange");
    public final static QName AgGatewayDocumentExchangePort = new QName("urn:aggateway:names:ws:docexchange", "AgGatewayDocumentExchangePort");
    static {
        URL url = null;
        try {
            url = new URL("classpath:org/nexuse2e/integration/AgGateway.wsdl");
        } catch (MalformedURLException e) {
            System.err.println("Can not initialize the default wsdl from classpath:org/nexuse2e/integration/AgGateway.wsdl");
            // e.printStackTrace();
        }
        WSDL_LOCATION = url;
    }

    public AgGatewayDocumentExchange(URL wsdlLocation) {
        super(wsdlLocation, SERVICE);
    }

    public AgGatewayDocumentExchange(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public AgGatewayDocumentExchange() {
        super(WSDL_LOCATION, SERVICE);
    }
    

    /**
     * 
     * @return
     *     returns DocExchangePortType
     */
    @WebEndpoint(name = "AgGatewayDocumentExchangePort")
    public DocExchangePortType getAgGatewayDocumentExchangePort() {
        return super.getPort(AgGatewayDocumentExchangePort, DocExchangePortType.class);
    }

    /**
     * 
     * @param features
     *     A list of {@link javax.xml.ws.WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns DocExchangePortType
     */
    @WebEndpoint(name = "AgGatewayDocumentExchangePort")
    public DocExchangePortType getAgGatewayDocumentExchangePort(WebServiceFeature... features) {
        return super.getPort(AgGatewayDocumentExchangePort, DocExchangePortType.class, features);
    }

}
