package org.kie.server.remote.rest.swagger;

import javax.ws.rs.Path;

import io.swagger.annotations.Api;
import io.swagger.jaxrs.listing.ApiListingResource;

@Api("kie-swagger")
@Path("server/swagger.{type:json|yaml}")
public class KieApiListingResource extends ApiListingResource {
	
}
