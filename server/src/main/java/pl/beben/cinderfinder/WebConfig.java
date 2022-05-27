package pl.beben.cinderfinder;

import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "pl.beben.ermatchmaker.controller")
public class WebConfig {

  // default is 30 seconds, set to 5 minutes because of long polling
  @Bean
  TomcatConnectorCustomizer asyncTimeoutCustomizer() {
    return connector -> connector.setAsyncTimeout(TimeUnit.MINUTES.toMillis(5l));
  }
  
}
