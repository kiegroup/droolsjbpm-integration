
package com.jboss.soap.service.acmedemo;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.jboss.soap.service.acmedemo package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ListAvailablePlanes_QNAME = new QName("http://service.soap.jboss.com/AcmeDemo/", "listAvailablePlanes");
    private final static QName _ListAvailablePlanesResponse_QNAME = new QName("http://service.soap.jboss.com/AcmeDemo/", "listAvailablePlanesResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.jboss.soap.service.acmedemo
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link BookFlights }
     * 
     */
    public BookFlights createBookFlights() {
        return new BookFlights();
    }

    /**
     * Create an instance of {@link BookFlightsResponse }
     * 
     */
    public BookFlightsResponse createBookFlightsResponse() {
        return new BookFlightsResponse();
    }

    /**
     * Create an instance of {@link CancelBooking }
     * 
     */
    public CancelBooking createCancelBooking() {
        return new CancelBooking();
    }

    /**
     * Create an instance of {@link CancelBookingResponse }
     * 
     */
    public CancelBookingResponse createCancelBookingResponse() {
        return new CancelBookingResponse();
    }

    /**
     * Create an instance of {@link ListAvailablePlanes }
     * 
     */
    public ListAvailablePlanes createListAvailablePlanes() {
        return new ListAvailablePlanes();
    }

    /**
     * Create an instance of {@link ListAvailablePlanesResponse }
     * 
     */
    public ListAvailablePlanesResponse createListAvailablePlanesResponse() {
        return new ListAvailablePlanesResponse();
    }

    /**
     * Create an instance of {@link FlightRequest }
     * 
     */
    public FlightRequest createFlightRequest() {
        return new FlightRequest();
    }

    /**
     * Create an instance of {@link Flight }
     * 
     */
    public Flight createFlight() {
        return new Flight();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ListAvailablePlanes }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.soap.jboss.com/AcmeDemo/", name = "listAvailablePlanes")
    public JAXBElement<ListAvailablePlanes> createListAvailablePlanes(ListAvailablePlanes value) {
        return new JAXBElement<ListAvailablePlanes>(_ListAvailablePlanes_QNAME, ListAvailablePlanes.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ListAvailablePlanesResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://service.soap.jboss.com/AcmeDemo/", name = "listAvailablePlanesResponse")
    public JAXBElement<ListAvailablePlanesResponse> createListAvailablePlanesResponse(ListAvailablePlanesResponse value) {
        return new JAXBElement<ListAvailablePlanesResponse>(_ListAvailablePlanesResponse_QNAME, ListAvailablePlanesResponse.class, null, value);
    }

}
