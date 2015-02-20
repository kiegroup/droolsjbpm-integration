package org.kie.server.client;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.kie.server.api.marshalling.MarshallingException;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.impl.KieServicesClientImpl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

public class KieServicesClientErrorHandlingTest extends BaseKieServicesClientTest {

    private static BaseMatcher<MarshallingException> serializationExceptionMatcher =
            new BaseMatcher<MarshallingException>() {
                @Override
                public boolean matches(Object o) {
                    return o instanceof MarshallingException;
                }

                @Override
                public void describeTo(Description description) {
                    description.appendText( "SerializationException" );
                }
            };

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void testError404Handling() {
        expectedEx.expect( KieServicesException.class );
        expectedEx.expectMessage( "Error code: 404" );
        stubFor(
                get( urlEqualTo( "/containers" ) )
                        .withHeader( "Accept", equalTo( "application/xml" ) )
                        .willReturn(
                                aResponse()
                                        .withStatus( 404 )
                                        .withBody( "Resource not found!" )
                        ) );
        KieServicesClient client = new KieServicesClientImpl( config );
        client.listContainers();
    }

    @Test
    public void testError500Handling() {
        expectedEx.expect( KieServicesException.class );
        expectedEx.expectMessage( "Error code: 500");
        stubFor(get(urlEqualTo("/containers"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                                .withStatus(500)
                                .withBody("Internal server error!")
                ));
        KieServicesClient client = new KieServicesClientImpl(config);
        client.listContainers();
    }

    @Test
    public void testXmlDeserializationErrorHandling() {
        expectedEx.expect(KieServicesException.class);
        expectedEx.expectCause(serializationExceptionMatcher);
        stubFor(get(urlEqualTo("/containers"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/xml")
                                .withBody("Some gibberish that can't be parsed by client!")
                ));
        KieServicesClient client = new KieServicesClientImpl(config);
        client.listContainers();
    }

    @Test
    public void testJsonDeserializationErrorHandling() {
        expectedEx.expect(KieServicesException.class);
        expectedEx.expectCause(serializationExceptionMatcher);
        stubFor(get(urlEqualTo("/containers"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("Some gibberish that can't be parsed by client!")
                ));
        KieServicesConfiguration configJSON = config.clone();
        configJSON.setMarshallingFormat( MarshallingFormat.JSON );
        KieServicesClient client = new KieServicesClientImpl(configJSON);
        client.listContainers();
    }

}
