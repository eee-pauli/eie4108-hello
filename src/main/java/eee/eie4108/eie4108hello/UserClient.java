package eee.eie4108.eie4108hello;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;

public class UserClient {
  static Response addUser(String base_url, User user) {
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(base_url);
    Response res = target.path("/add")
                         .request(MediaType.APPLICATION_JSON)
                         .post(Entity.entity(user, MediaType.APPLICATION_JSON));
    if (res.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
      return res;
    }
    return null;
  }
  
  static List<User> getAllUsers(String base_url) {
    Client client = ClientBuilder.newClient();
    WebTarget target = client.target(base_url);
    Response res = target.path("/list")
                         .request(MediaType.APPLICATION_JSON)
                         .get();
    if (res.getStatusInfo().getFamily() == Response.Status.Family.SUCCESSFUL) {
      return res.readEntity(new GenericType<List<User>>() {});
    }
    return null;
  }
  
  public static void main(String[] args) {
    User newUser = new User("Alice", "alice@example.com", 10000);
    Response res = addUser("http://localhost:8080/user", newUser);
    if (res != null) {
      System.out.println("Created a new user \"Alice\".");
    } else {
      System.out.println("Could not create new user.");
    }
    List<User> listUser = getAllUsers("http://localhost:8080/user");
    System.out.println("Current users:");
    for (User user : listUser) {
      System.out.println(user);
    }
  }
}
