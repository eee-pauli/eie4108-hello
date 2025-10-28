package eee.eie4108.eie4108hello;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;

@Path("/user")
public class UserResource {
  private static ArrayList<User> users = new ArrayList<>();
  
  @POST
  @Path("/add")
  @Consumes(MediaType.APPLICATION_JSON)
  public void add(User user) {
    System.out.println(user.toString());
    users.add(user);
  }
  
  @GET
  @Path("/list")
  @Produces(MediaType.APPLICATION_JSON)
  public ArrayList<User> getUsers() {
    return users;
  }
}
