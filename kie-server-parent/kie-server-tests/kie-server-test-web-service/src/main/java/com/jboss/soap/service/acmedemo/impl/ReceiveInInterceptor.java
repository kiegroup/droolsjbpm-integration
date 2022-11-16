package com.jboss.soap.service.acmedemo.impl;

import java.io.IOException;
import java.io.InputStream;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.helpers.IOUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.io.CachedOutputStream;
import org.apache.cxf.phase.Phase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// This interceptor is designed to throw a Fault exception in case of unescaped characters (out of CDATA) are received in the first phase
// (before other interceptors) to simulate servers like IIS which have problems in these scenarios

public class ReceiveInInterceptor extends AbstractSoapInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ReceiveInInterceptor.class);
    private String receivedMsg;
    
    public ReceiveInInterceptor() {
        super(Phase.RECEIVE);
    }

    @Override
    public void handleMessage(SoapMessage message) throws Fault {
        try {
            receivedMsg = getMessage(message, message.getContent(InputStream.class));
        } catch (IOException e) {
            logger.error("IOException", e);
        }

        logger.debug("Received message: {}", receivedMsg);

        //We are not interested in treating other messages but those containing "Coupon"
        if (!receivedMsg.contains("Coupon")) {
            return;
        }
        
        if (receivedMsg.contains("&lt;")){
            throw new Fault(new Exception("Incorrect unescaped chars"));
        }
    }

    private String getMessage(SoapMessage message, InputStream is) throws IOException {
       try (CachedOutputStream cos = new CachedOutputStream()) {
            IOUtils.copy(is, cos);
            message.setContent (InputStream.class, cos.getInputStream());
            return IOUtils.toString(cos.getInputStream());
       }
    }
}