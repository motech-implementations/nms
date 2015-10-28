/**
 * DS_GetMotherDataResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.motechproject.nms.mcts.soap;

public class DS_GetMotherDataResponse  implements java.io.Serializable {
    private org.motechproject.nms.mcts.soap.DS_GetMotherDataResponseDS_GetMotherDataResult DS_GetMotherDataResult;

    public DS_GetMotherDataResponse() {
    }

    public DS_GetMotherDataResponse(
           org.motechproject.nms.mcts.soap.DS_GetMotherDataResponseDS_GetMotherDataResult DS_GetMotherDataResult) {
           this.DS_GetMotherDataResult = DS_GetMotherDataResult;
    }


    /**
     * Gets the DS_GetMotherDataResult value for this DS_GetMotherDataResponse.
     * 
     * @return DS_GetMotherDataResult
     */
    public org.motechproject.nms.mcts.soap.DS_GetMotherDataResponseDS_GetMotherDataResult getDS_GetMotherDataResult() {
        return DS_GetMotherDataResult;
    }


    /**
     * Sets the DS_GetMotherDataResult value for this DS_GetMotherDataResponse.
     * 
     * @param DS_GetMotherDataResult
     */
    public void setDS_GetMotherDataResult(org.motechproject.nms.mcts.soap.DS_GetMotherDataResponseDS_GetMotherDataResult DS_GetMotherDataResult) {
        this.DS_GetMotherDataResult = DS_GetMotherDataResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DS_GetMotherDataResponse)) return false;
        DS_GetMotherDataResponse other = (DS_GetMotherDataResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.DS_GetMotherDataResult==null && other.getDS_GetMotherDataResult()==null) || 
             (this.DS_GetMotherDataResult!=null &&
              this.DS_GetMotherDataResult.equals(other.getDS_GetMotherDataResult())));
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
        if (getDS_GetMotherDataResult() != null) {
            _hashCode += getDS_GetMotherDataResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(DS_GetMotherDataResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">DS_GetMotherDataResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("DS_GetMotherDataResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "DS_GetMotherDataResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">>DS_GetMotherDataResponse>DS_GetMotherDataResult"));
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
