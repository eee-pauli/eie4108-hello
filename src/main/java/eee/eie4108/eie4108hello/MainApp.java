package eee.eie4108.eie4108hello;

import org.eclipse.jetty.server.CustomRequestLog;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Server;
import org.glassfish.jersey.jetty.JettyHttpContainerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.slf4j.LoggerFactory;

import java.net.URI;

public class MainApp {
  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MainApp.class);
  public static void main(String[] args) {
    try {
      final ResourceConfig config = new ResourceConfig()
                                        .register(CORSResponseFilter.class)
                                        // Add your resource class below
                                        .register(GreetingResource.class)
                                        .register(WaitListResource.class)
                                        .register(UserResource.class)
                                        .register(SomeResource.class);
      
      config.property(ServerProperties.WADL_FEATURE_DISABLE, true);
      String format = "%{client}a - %u %t '%r' %s %O '%{Referer}i' '%{User-Agent}i' '%C'";
      Server server = JettyHttpContainerFactory.createServer(URI.create("http://localhost:8080/"), config, false);
      RequestLog requestLog = new CustomRequestLog("request.log", format);
      server.setRequestLog(requestLog);
      server.start();
      
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        try {
          logger.info("Shutting down the application...");
          server.stop();
          logger.info("Done, exit.");
        } catch (Exception e) {
          logger.error(null, e);
        }
      }));
      
      logger.info("Application started. Stop the application using CTRL+C");
      
      // block and wait shut down signal, like CTRL+C
      Thread.currentThread().join();
      
    } catch (Exception ex) {
      logger.error(null, ex);
    }
    
  }
}
