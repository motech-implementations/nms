/**
 * MctsServiceLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.motechproject.nms.mcts.soap;

public class MctsServiceLocator extends org.apache.axis.client.Service implements org.motechproject.nms.mcts.soap.MctsService {

    public MctsServiceLocator() {
    }


    public MctsServiceLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public MctsServiceLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for basicEndpoint
    private java.lang.String basicEndpoint_address = "http://nrhm-mctsrpt.nic.in/All_States_WCF_Service/MctsService.svc/nrhm-mctsrpt.nic.in";

    public java.lang.String getbasicEndpointAddress() {
        return basicEndpoint_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String basicEndpointWSDDServiceName = "basicEndpoint";

    public java.lang.String getbasicEndpointWSDDServiceName() {
        return basicEndpointWSDDServiceName;
    }

    public void setbasicEndpointWSDDServiceName(java.lang.String name) {
        basicEndpointWSDDServiceName = name;
    }

    public org.motechproject.nms.mcts.soap.IMctsService getbasicEndpoint() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(basicEndpoint_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getbasicEndpoint(endpoint);
    }

    public org.motechproject.nms.mcts.soap.IMctsService getbasicEndpoint(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            org.motechproject.nms.mcts.soap.BasicEndpointStub _stub = new org.motechproject.nms.mcts.soap.BasicEndpointStub(portAddress, this);
            _stub.setPortName(getbasicEndpointWSDDServiceName());
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setbasicEndpointEndpointAddress(java.lang.String address) {
        basicEndpoint_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (org.motechproject.nms.mcts.soap.IMctsService.class.isAssignableFrom(serviceEndpointInterface)) {
                org.motechproject.nms.mcts.soap.BasicEndpointStub _stub = new org.motechproject.nms.mcts.soap.BasicEndpointStub(new java.net.URL(basicEndpoint_address), this);
                _stub.setPortName(getbasicEndpointWSDDServiceName());
                return _stub;
            }
        }
        catch (java.lang.Throwable t) {
            throw new javax.xml.rpc.ServiceException(t);
        }
        throw new javax.xml.rpc.ServiceException("There is no stub implementation for the interface:  " + (serviceEndpointInterface == null ? "null" : serviceEndpointInterface.getName()));
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(javax.xml.namespace.QName portName, Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        if (portName == null) {
            return getPort(serviceEndpointInterface);
        }
        java.lang.String inputPortName = portName.getLocalPart();
        if ("basicEndpoint".equals(inputPortName)) {
            return getbasicEndpoint();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://tempuri.org/", "MctsService");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://tempuri.org/", "basicEndpoint"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("basicEndpoint".equals(portName)) {
            setbasicEndpointEndpointAddress(address);
        }
        else 
{ // Unknown Port Name
            throw new javax.xml.rpc.ServiceException(" Cannot set Endpoint Address for Unknown Port" + portName);
        }
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(javax.xml.namespace.QName portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        setEndpointAddress(portName.getLocalPart(), address);
    }

}
