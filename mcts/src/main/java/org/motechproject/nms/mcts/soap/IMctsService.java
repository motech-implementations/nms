/**
 * IMctsService.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.motechproject.nms.mcts.soap;

public interface IMctsService extends java.rmi.Remote {
    public DS_GetChildDataResponseDS_GetChildDataResult DS_GetChildData(String id, String pwd, String fdate, String tdate, String sid, String did, String pid) throws java.rmi.RemoteException;
    public DS_GetMotherDataResponseDS_GetMotherDataResult DS_GetMotherData(String id, String pwd, String fdate, String tdate, String sid, String did, String pid) throws java.rmi.RemoteException;
    public DS_GetAnmAshaDataResponseDS_GetAnmAshaDataResult DS_GetAnmAshaData(String id, String pwd, String fdate, String tdate, String sid, String did, String pid) throws java.rmi.RemoteException;
}
