package com.jboss.soap.service.acmedemo.impl;

import javax.xml.namespace.QName;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

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
       if ("Espa&#241;a".equals(headerContent) || "AT&T".equals(headerContent)) {
           message.getExchange().put("discount", true);
           return;
       }

       logger.info("No discount with coupon {}", headerContent);
    }


}
