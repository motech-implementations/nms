/**
 * RchwebservicesLocator.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.motechproject.nms.rch.soap;

public class RchwebservicesLocator extends org.apache.axis.client.Service implements org.motechproject.nms.rch.soap.Rchwebservices {

    public RchwebservicesLocator() {
    }


    public RchwebservicesLocator(org.apache.axis.EngineConfiguration config) {
        super(config);
    }

    public RchwebservicesLocator(java.lang.String wsdlLoc, javax.xml.namespace.QName sName) throws javax.xml.rpc.ServiceException {
        super(wsdlLoc, sName);
    }

    // Use to get a proxy class for BasicHttpBinding_Irchwebservices
    private java.lang.String BasicHttpBinding_Irchwebservices_address = "http://rchrpt.nhm.gov.in/RCH_WS/rchwebservices.svc";

    public java.lang.String getBasicHttpBinding_IrchwebservicesAddress() {
        return BasicHttpBinding_Irchwebservices_address;
    }

    // The WSDD service name defaults to the port name.
    private java.lang.String BasicHttpBinding_IrchwebservicesWSDDServiceName = "BasicHttpBinding_Irchwebservices";

    public java.lang.String getBasicHttpBinding_IrchwebservicesWSDDServiceName() {
        return BasicHttpBinding_IrchwebservicesWSDDServiceName;
    }

    public void setBasicHttpBinding_IrchwebservicesWSDDServiceName(java.lang.String name) {
        BasicHttpBinding_IrchwebservicesWSDDServiceName = name;
    }

    public org.motechproject.nms.rch.soap.Irchwebservices getBasicHttpBinding_Irchwebservices() throws javax.xml.rpc.ServiceException {
       java.net.URL endpoint;
        try {
            endpoint = new java.net.URL(BasicHttpBinding_Irchwebservices_address);
        }
        catch (java.net.MalformedURLException e) {
            throw new javax.xml.rpc.ServiceException(e);
        }
        return getBasicHttpBinding_Irchwebservices(endpoint);
    }

    public org.motechproject.nms.rch.soap.Irchwebservices getBasicHttpBinding_Irchwebservices(java.net.URL portAddress) throws javax.xml.rpc.ServiceException {
        try {
            org.motechproject.nms.rch.soap.BasicHttpBinding_IrchwebservicesStub _stub = new org.motechproject.nms.rch.soap.BasicHttpBinding_IrchwebservicesStub(portAddress, this);
            _stub.setPortName(getBasicHttpBinding_IrchwebservicesWSDDServiceName());
            _stub.setTimeout(1800000);
            _stub._setProperty("axis.connection.timeout", 1800000);
            _stub._setProperty(org.apache.axis.client.Call.CONNECTION_TIMEOUT_PROPERTY, 1800000);
            _stub._setProperty(org.apache.axis.components.net.DefaultCommonsHTTPClientProperties.CONNECTION_DEFAULT_CONNECTION_TIMEOUT_KEY, 1800000);
            _stub._setProperty(org.apache.axis.components.net.DefaultCommonsHTTPClientProperties.CONNECTION_DEFAULT_SO_TIMEOUT_KEY, 1800000);
            _stub._setProperty(org.apache.axis.components.net.DefaultSocketFactory.CONNECT_TIMEOUT, 1800000);
            return _stub;
        }
        catch (org.apache.axis.AxisFault e) {
            return null;
        }
    }

    public void setBasicHttpBinding_IrchwebservicesEndpointAddress(java.lang.String address) {
        BasicHttpBinding_Irchwebservices_address = address;
    }

    /**
     * For the given interface, get the stub implementation.
     * If this service has no port for the given interface,
     * then ServiceException is thrown.
     */
    public java.rmi.Remote getPort(Class serviceEndpointInterface) throws javax.xml.rpc.ServiceException {
        try {
            if (org.motechproject.nms.rch.soap.Irchwebservices.class.isAssignableFrom(serviceEndpointInterface)) {
                org.motechproject.nms.rch.soap.BasicHttpBinding_IrchwebservicesStub _stub = new org.motechproject.nms.rch.soap.BasicHttpBinding_IrchwebservicesStub(new java.net.URL(BasicHttpBinding_Irchwebservices_address), this);
                _stub.setPortName(getBasicHttpBinding_IrchwebservicesWSDDServiceName());
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
        if ("BasicHttpBinding_Irchwebservices".equals(inputPortName)) {
            return getBasicHttpBinding_Irchwebservices();
        }
        else  {
            java.rmi.Remote _stub = getPort(serviceEndpointInterface);
            ((org.apache.axis.client.Stub) _stub).setPortName(portName);
            return _stub;
        }
    }

    public javax.xml.namespace.QName getServiceName() {
        return new javax.xml.namespace.QName("http://tempuri.org/", "rchwebservices");
    }

    private java.util.HashSet ports = null;

    public java.util.Iterator getPorts() {
        if (ports == null) {
            ports = new java.util.HashSet();
            ports.add(new javax.xml.namespace.QName("http://tempuri.org/", "BasicHttpBinding_Irchwebservices"));
        }
        return ports.iterator();
    }

    /**
    * Set the endpoint address for the specified port name.
    */
    public void setEndpointAddress(java.lang.String portName, java.lang.String address) throws javax.xml.rpc.ServiceException {
        
if ("BasicHttpBinding_Irchwebservices".equals(portName)) {
            setBasicHttpBinding_IrchwebservicesEndpointAddress(address);
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
