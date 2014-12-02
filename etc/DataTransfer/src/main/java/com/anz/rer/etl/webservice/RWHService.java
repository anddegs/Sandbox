package com.anz.rer.etl.webservice;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

/**
 * This class was generated by Apache CXF 2.7.0
 * 2013-04-16T10:53:59.552+08:00
 * Generated source version: 2.7.0
 * 
 */
@WebService(targetNamespace = "http://webservice.etl.rer.anz.com/", name = "RWHService")
@XmlSeeAlso({ObjectFactory.class})
public interface RWHService {

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "getRiskWareHouseCache", targetNamespace = "http://webservice.etl.rer.anz.com/", className = "com.anz.rer.etl.webservice.GetRiskWareHouseCache")
    @WebMethod
    @ResponseWrapper(localName = "getRiskWareHouseCacheResponse", targetNamespace = "http://webservice.etl.rer.anz.com/", className = "com.anz.rer.etl.webservice.GetRiskWareHouseCacheResponse")
    public java.util.List<com.anz.rer.etl.webservice.RwhInfo> getRiskWareHouseCache(
        @WebParam(name = "cobDate", targetNamespace = "")
        java.lang.Integer cobDate
    );

    @WebResult(name = "_return", targetNamespace = "")
    @RequestWrapper(localName = "getRWHCache", targetNamespace = "http://webservice.etl.rer.anz.com/", className = "com.anz.rer.etl.webservice.GetRWHCache")
    @WebMethod
    @ResponseWrapper(localName = "getRWHCacheResponse", targetNamespace = "http://webservice.etl.rer.anz.com/", className = "com.anz.rer.etl.webservice.GetRWHCacheResponse")
    public com.anz.rer.etl.webservice.GetRWHCacheResponse.Return getRWHCache(
        @WebParam(name = "cobDate", targetNamespace = "")
        java.lang.Integer cobDate
    );

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "getBusinessDate", targetNamespace = "http://webservice.etl.rer.anz.com/", className = "com.anz.rer.etl.webservice.GetBusinessDate")
    @WebMethod
    @ResponseWrapper(localName = "getBusinessDateResponse", targetNamespace = "http://webservice.etl.rer.anz.com/", className = "com.anz.rer.etl.webservice.GetBusinessDateResponse")
    public java.util.List<java.lang.Integer> getBusinessDate();

    @WebResult(name = "return", targetNamespace = "")
    @RequestWrapper(localName = "getRWInfoByPositionID", targetNamespace = "http://webservice.etl.rer.anz.com/", className = "com.anz.rer.etl.webservice.GetRWInfoByPositionID")
    @WebMethod
    @ResponseWrapper(localName = "getRWInfoByPositionIDResponse", targetNamespace = "http://webservice.etl.rer.anz.com/", className = "com.anz.rer.etl.webservice.GetRWInfoByPositionIDResponse")
    public com.anz.rer.etl.webservice.RwhInfo getRWInfoByPositionID(
        @WebParam(name = "cobDate", targetNamespace = "")
        java.lang.Integer cobDate,
        @WebParam(name = "positionID", targetNamespace = "")
        java.lang.Integer positionID
    );
}