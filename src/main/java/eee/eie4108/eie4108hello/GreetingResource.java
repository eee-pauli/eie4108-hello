package eee.eie4108.eie4108hello;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

@Path("/greeting")
public class GreetingResource {
  @GET
  @Path("/")
  @Produces("text/plain")
  public String hello() {
      return "Hello, World!";
  }
  
  @GET
  @Path("/hello/{name}")
  @Produces("text/plain")
  public String hello2(@PathParam("name") String name) {
    return "Hello, " + name + "!";
  }
}