/**
 * RetDMDashCaptionResponse.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package org.motechproject.nms.rch.soap;

public class RetDMDashCaptionResponse  implements java.io.Serializable {
    private java.lang.String retDMDashCaptionResult;

    public RetDMDashCaptionResponse() {
    }

    public RetDMDashCaptionResponse(
           java.lang.String retDMDashCaptionResult) {
           this.retDMDashCaptionResult = retDMDashCaptionResult;
    }


    /**
     * Gets the retDMDashCaptionResult value for this RetDMDashCaptionResponse.
     * 
     * @return retDMDashCaptionResult
     */
    public java.lang.String getRetDMDashCaptionResult() {
        return retDMDashCaptionResult;
    }


    /**
     * Sets the retDMDashCaptionResult value for this RetDMDashCaptionResponse.
     * 
     * @param retDMDashCaptionResult
     */
    public void setRetDMDashCaptionResult(java.lang.String retDMDashCaptionResult) {
        this.retDMDashCaptionResult = retDMDashCaptionResult;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof RetDMDashCaptionResponse)) return false;
        RetDMDashCaptionResponse other = (RetDMDashCaptionResponse) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.retDMDashCaptionResult==null && other.getRetDMDashCaptionResult()==null) || 
             (this.retDMDashCaptionResult!=null &&
              this.retDMDashCaptionResult.equals(other.getRetDMDashCaptionResult())));
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
        if (getRetDMDashCaptionResult() != null) {
            _hashCode += getRetDMDashCaptionResult().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(RetDMDashCaptionResponse.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("http://tempuri.org/", ">RetDMDashCaptionResponse"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("retDMDashCaptionResult");
        elemField.setXmlName(new javax.xml.namespace.QName("http://tempuri.org/", "RetDMDashCaptionResult"));
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
