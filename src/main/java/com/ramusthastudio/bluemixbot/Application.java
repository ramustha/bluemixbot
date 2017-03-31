package com.ramusthastudio.bluemixbot;

import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import java.security.NoSuchAlgorithmException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@LineMessageHandler
public class Application extends SpringBootServletInitializer {
  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder app) {
    return app.sources(Application.class);
  }

  public static void main(String[] args) throws NoSuchAlgorithmException {
    SpringApplication.run(Application.class, args);
  }
}
