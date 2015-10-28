/**
 * DS_GetAnmAshaDataResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.motechproject.nms.mcts.soap;

public class DS_GetAnmAshaDataResponse  implements java.io.Serializable {
    private org.motechproject.nms.mcts.soap.DS_GetAnmAshaDataResponseDS_GetAnmAshaDataResult DS_GetAnmAshaDataResult;

    public DS_GetAnmAshaDataResponse() {
    }

    public DS_GetAnmAshaDataResponse(
           org.motechproject.nms.mcts.soap.DS_GetAnmAshaDataResponseDS_GetAnmAshaDataResult DS_GetAnmAshaDataResult) {
           this.DS_GetAnmAshaDataResult = DS_GetAnmAshaDataResult;
    }


    /**
     * Gets the DS_GetAnmAshaDataResult value for this DS_GetAnmAshaDataResponse.
     * 
     * @return DS_GetAnmAshaDataResult
     */
    public org.motechproject.nms.mcts.soap.DS_GetAnmAshaDataResponseDS_GetAnmAshaDataResult getDS_GetAnmAshaDataResult() {
        return DS_GetAnmAshaDataResult;
    }


    /**
     * Sets the DS_GetAnmAshaDataResult value for this DS_GetAnmAshaDataResponse.
     * 
     * @param DS_GetAnmAshaDataResult
     */
    public void setDS_GetAnmAshaDataResult(org.motechproject.nms.mcts.soap.DS_GetAnmAshaDataResponseDS_GetAnmAshaDataResult DS_GetAnmAshaDataResult) {
        this.DS_GetAnmAshaDataResult = DS_GetAnmAshaDataResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DS_GetAnmAshaDataResponse)) return false;
        DS_GetAnmAshaDataResponse other = (DS_GetAnmAshaDataResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.DS_GetAnmAshaDataResult==null && other.getDS_GetAnmAshaDataResult()==null) || 
             (this.DS_GetAnmAshaDataResult!=null &&
              this.DS_GetAnmAshaDataResult.equals(other.getDS_GetAnmAshaDataResult())));
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
        if (getDS_GetAnmAshaDataResult() != null) {
            _hashCode += getDS_GetAnmAshaDataResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(DS_GetAnmAshaDataResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">DS_GetAnmAshaDataResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("DS_GetAnmAshaDataResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "DS_GetAnmAshaDataResult"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">>DS_GetAnmAshaDataResponse>DS_GetAnmAshaDataResult"));
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
