package com.jboss.soap.service.acmedemo.impl;

import java.io.StringReader;
import java.util.List;
import javax.xml.xpath.XPathFactory;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;

import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

public class HeaderInInterceptor extends AbstractSoapInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(HeaderInInterceptor.class);
    
    public HeaderInInterceptor() {
        super(Phase.USER_PROTOCOL);
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
       Header header = message.getHeader(new QName("http://acme-travel.com", "Coupon"));
       
       if (header == null) {
         logger.info("No intercepted headers");
         return;
       }
       
       String headerContent = ((Element) header.getObject()).getTextContent();
       logger.info("headerContent is {}", headerContent);

       //DumbEscapeHandler modifies chars over US-ASCII
       if ("Espa&#241;a".equals(headerContent)) {
           message.getExchange().put("discount", true);
           return;
       }
       
       XPathFactory xpathFactory = XPathFactory.newInstance();
       XPath xpath = xpathFactory.newXPath();

       InputSource source = new InputSource(new StringReader(headerContent));
           
       try {
         String couponNumber = xpath.evaluate("/Coupon/Number", source);
         if ("AT&T".equals(couponNumber)) {
           message.getExchange().put("discount", true);
         } else {
           logger.info("No discount with coupon {}", couponNumber);
         }
       } catch (XPathExpressionException e) {
         logger.error("Exception while parsing headers", e);
       }
    }


}
