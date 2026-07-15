# Swagger → OpenAPI 3 Migration Notes

Migration of `kie-server-rest-swagger` from `io.swagger` (Swagger 2.x / javax) to `io.swagger.core.v3` (OpenAPI 3.x / jakarta).

---

## Context

The module implements the Swagger/OpenAPI integration for KIE Server REST endpoints. It was originally written against `io.swagger` v2 dependencies using the `javax` namespace. The migration targets:

- **Namespace**: `javax.*` → `jakarta.*`
- **Swagger library**: `io.swagger:swagger-*` (v2) → `io.swagger.core.v3:swagger-*-jakarta` (v3)
- **Spec format**: Swagger 2.0 → OpenAPI 3.0

### `pom.xml` dependencies after migration

```xml
<dependency>
  <groupId>io.swagger.core.v3</groupId>
  <artifactId>swagger-jaxrs2-jakarta</artifactId>
</dependency>
<dependency>
  <groupId>io.swagger.core.v3</groupId>
  <artifactId>swagger-annotations</artifactId>
</dependency>
<dependency>
  <groupId>io.swagger.core.v3</groupId>
  <artifactId>swagger-core-jakarta</artifactId>
</dependency>
<dependency>
  <groupId>io.swagger.core.v3</groupId>
  <artifactId>swagger-models-jakarta</artifactId>
</dependency>
```

---

## Q1 — Equivalent of `AbstractSpecFilter.isOperationAllowed()`

### Old API (Swagger 2)

```java
// io.swagger.core.filter.AbstractSpecFilter
public boolean isOperationAllowed(
    io.swagger.models.Operation operation,
    io.swagger.model.ApiDescription api,
    Map<String, List<String>> params,
    Map<String, String> cookies,
    Map<String, List<String>> headers)
```

### New API (OpenAPI 3)

```java
// io.swagger.v3.core.filter.AbstractSpecFilter
public Optional<Operation> filterOperation(
    io.swagger.v3.oas.models.Operation operation,
    io.swagger.v3.core.model.ApiDescription api,
    Map<String, List<String>> params,
    Map<String, String> cookies,
    Map<String, List<String>> headers)
```

### Key differences

| Aspect | Swagger 2 | OpenAPI 3 |
|---|---|---|
| Method name | `isOperationAllowed` | `filterOperation` |
| Return type | `boolean` | `Optional<Operation>` |
| Allow semantic | `return true` | `return Optional.of(operation)` |
| Deny semantic | `return false` | `return Optional.empty()` |
| Operation type | `io.swagger.models.Operation` | `io.swagger.v3.oas.models.Operation` |
| Vendor extensions | `getVendorExtensions()` | `getExtensions()` |
| Filter interface | `io.swagger.core.filter.SwaggerSpecFilter` | `io.swagger.v3.core.filter.OpenAPISpecFilter` |
| Abstract base | `io.swagger.core.filter.AbstractSpecFilter` | `io.swagger.v3.core.filter.AbstractSpecFilter` |

### Migration example

```java
// BEFORE (Swagger 2)
OpenAPISpecFilter filter = new AbstractSpecFilter() {
    @Override
    public boolean isOperationAllowed(Operation operation, ApiDescription api,
            Map<String, List<String>> params, Map<String, String> cookies,
            Map<String, List<String>> headers) {
        return operation.getVendorExtensions().containsKey(SWAGGER_EXTENSION_READ_ONLY);
    }
};

// AFTER (OpenAPI 3)
private static class ReadOnlySpecFilter extends AbstractSpecFilter {
    @Override
    public Optional<Operation> filterOperation(Operation operation, ApiDescription api,
            Map<String, List<String>> params, Map<String, String> cookies,
            Map<String, List<String>> headers) {
        return operation.getExtensions() != null
            && operation.getExtensions().containsKey(SWAGGER_EXTENSION_READ_ONLY)
                ? Optional.of(operation)
                : Optional.empty();
    }
}
```

> **Note:** The filter must be a **named class** (not an anonymous inner class) because v3 registers it by class name string. See Q2.

---

## Q2 — Equivalent of `FilterFactory.setFilter(SwaggerSpecFilter)`

### Old API (Swagger 2)

```java
import io.swagger.config.FilterFactory;

FilterFactory.setFilter(filter); // static singleton registry
```

### New API (OpenAPI 3)

`FilterFactory` **does not exist** in `io.swagger.v3`. The filter is registered on the `OpenApiContext` via `SwaggerConfiguration.filterClass()`.

```java
import io.swagger.v3.jaxrs2.integration.JaxrsOpenApiContextBuilder;
import io.swagger.v3.oas.integration.SwaggerConfiguration;

try {
    new JaxrsOpenApiContextBuilder<>()
        .openApiConfiguration(
            new SwaggerConfiguration()
                .filterClass(ReadOnlySpecFilter.class.getName())
        )
        .buildContext(true);
} catch (OpenApiConfigurationException e) {
    throw new RuntimeException("Failed to register OpenAPI filter", e);
}
```

### Key differences

| Swagger 2 | OpenAPI 3 |
|---|---|
| `io.swagger.config.FilterFactory` | **Removed** — does not exist |
| `FilterFactory.setFilter(instance)` | `new SwaggerConfiguration().filterClass(ClassName.class.getName())` |
| `FilterFactory.getFilter()` | `OpenApiContext.getInstance().getOpenApiConfiguration().getFilterClass()` |
| Accepts filter **instance** | Requires filter **class name** (must be a named, instantiable class) |

> **Consequence:** The filter cannot be an anonymous inner class. It must be extracted to a named static inner class or top-level class so it can be referenced by `ClassName.class.getName()`.

---

## Q3 — Equivalent of `io.swagger.jaxrs.listing.ApiListingResource`

### Old API (Swagger 2)

```java
import io.swagger.jaxrs.listing.ApiListingResource;

@Path("server/swagger.{type:json|yaml}")
public class KieApiListingResource extends ApiListingResource { }
```

`ApiListingResource` was a pre-built JAX-RS resource in `swagger-jaxrs` that served `/swagger.json` and `/swagger.yaml` by internally calling `ScannerFactory` and `FilterFactory`.

### New API (OpenAPI 3)

`ApiListingResource` **does not exist** in `io.swagger.v3`. The replacement is `io.swagger.v3.jaxrs2.integration.resources.OpenApiResource`, shipped in `swagger-jaxrs2-jakarta`.

To preserve the legacy `server/swagger.{type}` path for backward compatibility, subclass `OpenApiResource` and override the endpoint:

```java
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.core.util.Json;
import com.fasterxml.jackson.module.jakarta.xmlbind.JakartaXmlBindAnnotationModule;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

@Path("server/swagger.{type:json|yaml}")
public class KieApiListingResource extends OpenApiResource {

    public KieApiListingResource() {
        Json.mapper().registerModule(new JakartaXmlBindAnnotationModule());
    }

    @GET
    @Produces({MediaType.APPLICATION_JSON, "application/yaml"})
    public Response getOpenApi(@Context HttpHeaders headers,
                               @Context UriInfo uriInfo,
                               @PathParam("type") String type) throws Exception {
        String acceptType = "yaml".equalsIgnoreCase(type)
            ? "application/yaml"
            : MediaType.APPLICATION_JSON;
        return super.getOpenApi(headers, uriInfo, acceptType);
    }
}
```

To also expose the standard `/openapi` path, register `OpenApiResource` directly alongside the legacy resource:

```java
components.add(new KieApiListingResource()); // backward compat: server/swagger.{json|yaml}
components.add(new OpenApiResource());        // standard:        /openapi.{json|yaml}
```

### Key differences

| Aspect | Swagger 2 | OpenAPI 3 |
|---|---|---|
| Class | `io.swagger.jaxrs.listing.ApiListingResource` | `io.swagger.v3.jaxrs2.integration.resources.OpenApiResource` |
| Default path | `/swagger.json`, `/swagger.yaml` | `/openapi.json`, `/openapi.yaml`, `/openapi` |
| Spec format produced | Swagger 2.0 | OpenAPI 3.0 |
| Internal wiring | `ScannerFactory`, `FilterFactory` | `OpenApiContext`, `SwaggerConfiguration` |
| `SwaggerSerializers` provider | Required | **Removed** — not needed in v3 |

> **Also remove:** `io.swagger.v3.jaxrs2.SwaggerSerializers` — this class does not exist in v3. The `OpenApiResource` handles serialisation internally via standard JAX-RS content negotiation.

---

## Q4 — Equivalent of `io.swagger.annotations.@Api`

`@Api` belongs to the **Swagger 2.x annotation set** (`io.swagger:swagger-annotations` v1.x). In `io.swagger.core.v3:swagger-annotations` the entire annotation model was redesigned around OpenAPI 3.0. There is no direct one-to-one rename.

### Full annotation mapping

| Swagger 2 (`io.swagger.annotations`) | OpenAPI 3 (`io.swagger.v3.oas.annotations`) | Scope |
|---|---|---|
| `@Api` | `@Tag` (on class) + `@OpenAPIDefinition` (global) | Class-level grouping |
| `@ApiOperation` | `@Operation` | Method-level description |
| `@ApiParam` | `@Parameter` | Parameter description |
| `@ApiResponse` | `@ApiResponse` (**different package**) | Single response |
| `@ApiResponses` | `@ApiResponses` (**different package**) | Multiple responses |
| `@ApiModel` | `@Schema` (on class) | Model/schema description |
| `@ApiModelProperty` | `@Schema` (on field/method) | Schema property |
| `@ApiImplicitParam` | `@Parameter` | Non-JAX-RS parameter |
| `@ApiImplicitParams` | `@Parameters` | Container for parameters |
| `@Authorization` | `@SecurityRequirement` | Security scheme reference |
| `@AuthorizationScope` | Folded into `@SecurityRequirement` | OAuth scope |
| `@SwaggerDefinition` | `@OpenAPIDefinition` | Global spec metadata |

### `@Api` specifically

```java
// BEFORE (Swagger 2)
import io.swagger.annotations.Api;

@Api(value = "KIE Server Jobs", tags = "jobs", description = "Job scheduling operations")
@Path("/server/jobs")
public class JobResource { }

// AFTER (OpenAPI 3)
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "jobs", description = "Job scheduling operations")
@Path("/server/jobs")
public class JobResource { }
```

- The `value` attribute (legacy UI class name) has **no equivalent** — drop it.
- The `tags` attribute maps directly to `@Tag(name = ...)`.

### Global tag declarations

```java
// AFTER (OpenAPI 3) — on JAX-RS Application class
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;

@OpenAPIDefinition(
    info = @Info(title = "KIE Server API", version = "7.81"),
    tags = {
        @Tag(name = "jobs",    description = "Job scheduling operations"),
        @Tag(name = "rules",   description = "Rules evaluation operations"),
        @Tag(name = "process", description = "Process management operations")
    }
)
public class KieServerApplication extends Application { }
```

### Hiding a resource

```java
// BEFORE (Swagger 2)
@Api(hidden = true)
public class InternalResource { }

// AFTER (OpenAPI 3) — per method only
@Operation(hidden = true)
public Response internalMethod() { }
```

There is no class-level `hidden` in v3. To exclude an entire resource, use the `OpenAPISpecFilter` (`ReadOnlySpecFilter` in this project).

---

## File state after migration

| File | Status |
|---|---|
| `pom.xml` | ✅ Fully migrated to `io.swagger.core.v3` |
| `ReadOnlySwaggerDecoration.java` | ✅ Migrated — `AbstractOpenAPIExtension`, `io.swagger.v3.oas.models.Operation`, `addExtension()` |
| `KieApiListingResource.java` | ✅ Migrated — extends `OpenApiResource`, `io.swagger.v3.core.util.Json` |
| `SwaggerRestApplicationComponentsService.java` | ⚠️ Nearly complete — `SwaggerSerializers` import/usage still present (v2 artefact, must be removed) |
| `ReadOnlySwaggerDecorationTest.java` | ✅ Migrated — `io.swagger.v3.oas.models.Operation`, `getExtensions()` |
