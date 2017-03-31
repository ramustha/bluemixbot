package com.ramusthastudio.bluemixbot.http;

import java.io.IOException;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

class NewHeaderInterceptor implements Interceptor {
  private static final String USER_AGENT =
      "line-botsdk-java/" + NewHeaderInterceptor.class.getPackage().getImplementationVersion();
  private final String channelToken;

  NewHeaderInterceptor(String channelToken) {
    this.channelToken = channelToken;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request().newBuilder()
        .addHeader("Authorization", "Bearer " + channelToken)
        .addHeader("User-Agent", USER_AGENT)
        .build();
    return chain.proceed(request);
  }
}
