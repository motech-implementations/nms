/**
 * DS_GetMotherData.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.motechproject.nms.mcts.soap;

public class DS_GetMotherData  implements java.io.Serializable {
    private java.lang.String id;

    private java.lang.String pwd;

    private java.lang.String fdate;

    private java.lang.String tdate;

    private java.lang.String sid;

    public DS_GetMotherData() {
    }

    public DS_GetMotherData(
           java.lang.String id,
           java.lang.String pwd,
           java.lang.String fdate,
           java.lang.String tdate,
           java.lang.String sid) {
           this.id = id;
           this.pwd = pwd;
           this.fdate = fdate;
           this.tdate = tdate;
           this.sid = sid;
    }


    /**
     * Gets the id value for this DS_GetMotherData.
     * 
     * @return id
     */
    public java.lang.String getId() {
        return id;
    }


    /**
     * Sets the id value for this DS_GetMotherData.
     * 
     * @param id
     */
    public void setId(java.lang.String id) {
        this.id = id;
    }


    /**
     * Gets the pwd value for this DS_GetMotherData.
     * 
     * @return pwd
     */
    public java.lang.String getPwd() {
        return pwd;
    }


    /**
     * Sets the pwd value for this DS_GetMotherData.
     * 
     * @param pwd
     */
    public void setPwd(java.lang.String pwd) {
        this.pwd = pwd;
    }


    /**
     * Gets the fdate value for this DS_GetMotherData.
     * 
     * @return fdate
     */
    public java.lang.String getFdate() {
        return fdate;
    }


    /**
     * Sets the fdate value for this DS_GetMotherData.
     * 
     * @param fdate
     */
    public void setFdate(java.lang.String fdate) {
        this.fdate = fdate;
    }


    /**
     * Gets the tdate value for this DS_GetMotherData.
     * 
     * @return tdate
     */
    public java.lang.String getTdate() {
        return tdate;
    }


    /**
     * Sets the tdate value for this DS_GetMotherData.
     * 
     * @param tdate
     */
    public void setTdate(java.lang.String tdate) {
        this.tdate = tdate;
    }


    /**
     * Gets the sid value for this DS_GetMotherData.
     * 
     * @return sid
     */
    public java.lang.String getSid() {
        return sid;
    }


    /**
     * Sets the sid value for this DS_GetMotherData.
     * 
     * @param sid
     */
    public void setSid(java.lang.String sid) {
        this.sid = sid;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DS_GetMotherData)) return false;
        DS_GetMotherData other = (DS_GetMotherData) obj;
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
              this.sid.equals(other.getSid())));
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
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(DS_GetMotherData.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">DS_GetMotherData"));
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
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
