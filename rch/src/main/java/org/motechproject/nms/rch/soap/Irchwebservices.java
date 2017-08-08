/**
 * Irchwebservices.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.motechproject.nms.rch.soap;

public interface Irchwebservices extends java.rmi.Remote {
    public org.motechproject.nms.rch.soap.DS_DataResponseDS_DataResult DS_Data(java.lang.String projectID, java.lang.String ID, java.lang.String securecode, java.lang.String fromdate, java.lang.String toDate, java.lang.String STID, java.lang.String typeofdata, java.lang.String DTID) throws java.rmi.RemoteException;
    public java.lang.String retDMDashCaption() throws java.rmi.RemoteException;
}
