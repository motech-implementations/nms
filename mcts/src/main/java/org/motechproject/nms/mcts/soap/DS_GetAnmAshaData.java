/**
 * DS_GetAnmAshaData.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.motechproject.nms.mcts.soap;

public class DS_GetAnmAshaData  implements java.io.Serializable {
    private String id;

    private String pwd;

    private String fdate;

    private String tdate;

    private String sid;

    private String did;

    private String pid;

    public DS_GetAnmAshaData() {
    }

    public DS_GetAnmAshaData(
           String id,
           String pwd,
           String fdate,
           String tdate,
           String sid,
           String did,
           String pid) {
           this.id = id;
           this.pwd = pwd;
           this.fdate = fdate;
           this.tdate = tdate;
           this.sid = sid;
           this.did = did;
           this.pid = pid;
    }


    /**
     * Gets the id value for this DS_GetAnmAshaData.
     * 
     * @return id
     */
    public String getId() {
        return id;
    }


    /**
     * Sets the id value for this DS_GetAnmAshaData.
     * 
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }


    /**
     * Gets the pwd value for this DS_GetAnmAshaData.
     * 
     * @return pwd
     */
    public String getPwd() {
        return pwd;
    }


    /**
     * Sets the pwd value for this DS_GetAnmAshaData.
     * 
     * @param pwd
     */
    public void setPwd(String pwd) {
        this.pwd = pwd;
    }


    /**
     * Gets the fdate value for this DS_GetAnmAshaData.
     * 
     * @return fdate
     */
    public String getFdate() {
        return fdate;
    }


    /**
     * Sets the fdate value for this DS_GetAnmAshaData.
     * 
     * @param fdate
     */
    public void setFdate(String fdate) {
        this.fdate = fdate;
    }


    /**
     * Gets the tdate value for this DS_GetAnmAshaData.
     * 
     * @return tdate
     */
    public String getTdate() {
        return tdate;
    }


    /**
     * Sets the tdate value for this DS_GetAnmAshaData.
     * 
     * @param tdate
     */
    public void setTdate(String tdate) {
        this.tdate = tdate;
    }


    /**
     * Gets the sid value for this DS_GetAnmAshaData.
     * 
     * @return sid
     */
    public String getSid() {
        return sid;
    }


    /**
     * Sets the sid value for this DS_GetAnmAshaData.
     * 
     * @param sid
     */
    public void setSid(String sid) {
        this.sid = sid;
    }


    /**
     * Gets the did value for this DS_GetAnmAshaData.
     * 
     * @return did
     */
    public String getDid() {
        return did;
    }


    /**
     * Sets the did value for this DS_GetAnmAshaData.
     * 
     * @param did
     */
    public void setDid(String did) {
        this.did = did;
    }


    /**
     * Gets the pid value for this DS_GetAnmAshaData.
     * 
     * @return pid
     */
    public String getPid() {
        return pid;
    }


    /**
     * Sets the pid value for this DS_GetAnmAshaData.
     * 
     * @param pid
     */
    public void setPid(String pid) {
        this.pid = pid;
    }

    private Object __equalsCalc = null;
    public synchronized boolean equals(Object obj) {
        if (!(obj instanceof DS_GetAnmAshaData)) return false;
        DS_GetAnmAshaData other = (DS_GetAnmAshaData) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.id==null && other.getId()==null) || 
             (this.id!=null &&
              this.id.equals(other.getId()))) &&
            ((this.pwd==null && other.getPwd()==null) || 
             (this.pwd!=null &&
              this.pwd.equals(other.getPwd()))) &&
            ((this.fdate==null && other.getFdate()==null) || 
             (this.fdate!=null &&
              this.fdate.equals(other.getFdate()))) &&
            ((this.tdate==null && other.getTdate()==null) || 
             (this.tdate!=null &&
              this.tdate.equals(other.getTdate()))) &&
            ((this.sid==null && other.getSid()==null) || 
             (this.sid!=null &&
              this.sid.equals(other.getSid()))) &&
            ((this.did==null && other.getDid()==null) || 
             (this.did!=null &&
              this.did.equals(other.getDid()))) &&
            ((this.pid==null && other.getPid()==null) || 
             (this.pid!=null &&
              this.pid.equals(other.getPid())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getId() != null) {
            _hashCode += getId().hashCode();
        }
        if (getPwd() != null) {
            _hashCode += getPwd().hashCode();
        }
        if (getFdate() != null) {
            _hashCode += getFdate().hashCode();
        }
        if (getTdate() != null) {
            _hashCode += getTdate().hashCode();
        }
        if (getSid() != null) {
            _hashCode += getSid().hashCode();
        }
        if (getDid() != null) {
            _hashCode += getDid().hashCode();
        }
        if (getPid() != null) {
            _hashCode += getPid().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(DS_GetAnmAshaData.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">DS_GetAnmAshaData"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("id");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "id"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("pwd");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "pwd"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("fdate");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "fdate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("tdate");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "tdate"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("sid");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "sid"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("did");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "did"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("pid");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "pid"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setMinOccurs(0);
        elemField.setNillable(true);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           String mechType,
           Class _javaType,
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           String mechType,
           Class _javaType,
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
