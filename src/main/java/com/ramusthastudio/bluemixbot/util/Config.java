package com.ramusthastudio.bluemixbot.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

@Configuration
@PropertySource("classpath:application.properties")
public class Config {
  final
  Environment mEnv;
  @Autowired public Config(Environment mEnv) {this.mEnv = mEnv;}

  @Bean(name = "line.bot.channelSecret")
  public String getChannelSecret() { return mEnv.getProperty("line.bot.channelSecret"); }
  @Bean(name = "line.bot.channelToken")
  public String getChannelAccessToken() { return mEnv.getProperty("line.bot.channelToken"); }
}
