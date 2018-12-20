package org.kie.server.remote.rest.prometheus;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.services.prometheus.PrometheusKieServerExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@Api(value = "")
@Path("prometheus")
public class MetricsResource {

    public static final Logger LOG = LoggerFactory.getLogger(MetricsResource.class);

    private CollectorRegistry registry;

    public MetricsResource() {
        this.registry = PrometheusKieServerExtension.registry;
    }

    @ApiOperation(value = "Retrieve prometheus metrics",
            response = ServiceResponse.class, code = 200)
    @ApiResponses(value = {@ApiResponse(code = 500, message = "Unexpected error"), @ApiResponse(code = 404, message = "")})
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response getModels() {

        LOG.info("Collecton Registry test: " + registry.hashCode());

        Enumeration<Collector.MetricFamilySamples> mfs = registry.metricFamilySamples();

        StreamingOutput stream = os -> {
            Writer writer = new BufferedWriter(new OutputStreamWriter(os));
            TextFormat.write004(writer, mfs);
            writer.flush();
        };

        return Response.ok(stream).build();
    }
}
