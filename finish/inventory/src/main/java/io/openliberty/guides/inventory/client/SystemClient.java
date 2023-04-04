package io.openliberty.guides.inventory.client;

import java.util.Properties;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "systemClient",
                     baseUri = "http://localhost:9080/system")
@Path("/properties")
public interface SystemClient extends AutoCloseable {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  Properties getProperties() throws ProcessingException;
}