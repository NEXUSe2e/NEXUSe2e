package org.nexuse2e.service.ws.aggateway.wsdl;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * This class was generated by Apache CXF 2.3.2
 * 2011-02-03T15:44:34.213+01:00
 * Generated source version: 2.3.2
 * 
 */
 
@WebService(targetNamespace = "urn:aggateway:names:ws:docexchange", name = "DocExchangePortType")
@XmlSeeAlso({ObjectFactory.class})
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
public interface DocExchangePortType {

    @WebResult(name = "outboundData", targetNamespace = "urn:aggateway:names:ws:docexchange", partName = "result")
    @WebMethod(action = "execute")
    public OutboundData execute(
        @WebParam(partName = "parameters", name = "inboundData", targetNamespace = "urn:aggateway:names:ws:docexchange")
        InboundData parameters
    ) throws DocExchangeFault;
}