package com.jboss.soap.service.acmedemo.impl;

import java.math.BigDecimal;
import java.util.Random;

import javax.jws.WebService;

import org.apache.cxf.phase.PhaseInterceptorChain;

import com.jboss.soap.service.acmedemo.AcmeDemoInterface;
import com.jboss.soap.service.acmedemo.Flight;
import com.jboss.soap.service.acmedemo.FlightRequest;

@org.apache.cxf.interceptor.InInterceptors (interceptors = {"com.jboss.soap.service.acmedemo.impl.ReceiveInInterceptor", "com.jboss.soap.service.acmedemo.impl.HeaderInInterceptor"})
@WebService(serviceName = "AcmeDemoService", endpointInterface = "com.jboss.soap.service.acmedemo.AcmeDemoInterface", targetNamespace = "http://service.soap.jboss.com/AcmeDemo/")
public class AcmeDemoInterfaceImpl implements AcmeDemoInterface {

    public Flight listAvailablePlanes(FlightRequest in) {
        String startCity = in.getStartCity();
        String endCity = in.getEndCity();
        BigDecimal outboundBD = new BigDecimal(525);

        Flight outbound = new Flight();
        outbound.setCompany("EasyJet");
        outbound.setPlaneId(12345);
        
        if (PhaseInterceptorChain.getCurrentMessage()!= null &&
            PhaseInterceptorChain.getCurrentMessage().getExchange() != null &&
            PhaseInterceptorChain.getCurrentMessage().getExchange().get("discount") != null &&
            (boolean) PhaseInterceptorChain.getCurrentMessage().getExchange().get("discount"))
            outbound.setRatePerPerson(new BigDecimal(100));
        else 
            outbound.setRatePerPerson(outboundBD);
        outbound.setStartCity(startCity);
        outbound.setTargetCity(endCity);
        outbound.setTravelDate(in.getStartDate());

        System.out.println("OUTBOUND FLIGHT variables set");

        return outbound;
    }

    public String bookFlights(String in) {
        System.out.println("SUCCESS: Your flights are now reserved.");
        System.out.println();

        SessionIdentifierGenerator bookingRef = new SessionIdentifierGenerator();
        String refNum = bookingRef.nextSessionId();

        System.out.println("Your RESERVATION NUMBER is: " + refNum);

        return refNum;
    }

    public int cancelBooking(String in) {
        int cancelCharge = 0;
        final Random random = new Random();

        if (in == null)
            throw new IllegalArgumentException("No booking found");

        cancelCharge = random.nextInt((10 - 5) + 1) + 5;

        return cancelCharge;
    }
}
