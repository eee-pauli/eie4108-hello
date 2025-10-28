package eee.eie4108.eie4108hello;

import jakarta.ws.rs.*;

import java.util.LinkedList;
import java.util.Queue;

@Path("/waitlist")
public class WaitListResource {
  private static final Queue<String> queue = new LinkedList<>();
  
  @GET @Path("/") @Produces("text/plain")
  public String top() {
    return queue.toString();
  }
  
  @POST @Path("/poll") @Produces("text/plain")
  public String poll() {
    // Retrieves and removes the head of this queue
    if (!queue.isEmpty()) {
      return queue.poll();
    }
    return "<empty>";
  }
  
  @POST @Path("/") @Produces("text/plain")
  public String push(@QueryParam("item") String item) {
    if (item != null && item.isEmpty()) {
      return "false";
    }
    boolean result = queue.offer(item);
    return String.valueOf(result);
  }
}
