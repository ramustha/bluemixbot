package com.ramusthastudio.bluemixbot.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.linecorp.bot.client.LineMessagingService;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.BotApiResponse;
import com.ramusthastudio.bluemixbot.http.HeaderInterceptor;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

public final class exTesting {
  private static final Logger LOG = LoggerFactory.getLogger(BotHelper.class);

  public static final String DEFAULT_API_END_POINT = "https://api.line.me/";
  public static final String TOKEN = "ERiW4aOEr0LmiOrh83L/D+eupR0SbrJKDHpT0Aj23B2Qk7jmmieRb4jWWARTnUGq9MYNCFu4FBXA+vtlKeINCnqXrwiQZgZ/VOjDNnJePG9778oxs1ahVfRd6k9u8x/b/LkPp2hEkHvJ5bYp2wo9kwdB04t89/1O/w1cDnyilFU=";
  public static final String USERID = "U22bbf173a4a690b7b728db5c72e03374";
  public static final long DEFAULT_CONNECT_TIMEOUT = 10_000;
  public static final long DEFAULT_READ_TIMEOUT = 10_000;
  public static final long DEFAULT_WRITE_TIMEOUT = 10_000;
  public static void main(String[] args) {
    OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
    for (Interceptor interceptor : defaultInterceptors(TOKEN)) {
      okHttpClientBuilder.addInterceptor(interceptor);
    }
    okHttpClientBuilder
        .connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.MILLISECONDS)
        .readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.MILLISECONDS)
        .writeTimeout(DEFAULT_WRITE_TIMEOUT, TimeUnit.MILLISECONDS);

    final OkHttpClient okHttpClient = okHttpClientBuilder.build();

    Retrofit.Builder builder = createDefaultRetrofitBuilder();
    builder.client(okHttpClient);
    builder.baseUrl(DEFAULT_API_END_POINT);
    final Retrofit retrofit = builder.build();
    LineMessagingService lineService = retrofit.create(LineMessagingService.class);
    try {
      retrofit2.Response<UserProfileResponse> profile = lineService.getProfile(USERID).execute();
      LOG.info("getProfile mesage {} code {}", profile.message(), profile.code());

      TextMessage message = new TextMessage("testing");
      PushMessage pushMessage = new PushMessage(USERID, message);

      lineService.pushMessage(pushMessage).execute();
      LOG.info("pushMessage mesage {} code {}", profile.message(), profile.code());
    } catch (IOException aE) {
      aE.printStackTrace();
    }
  }

  private static Retrofit.Builder createDefaultRetrofitBuilder() {
    final ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // Register JSR-310(java.time.temporal.*) module and read number as millsec.
    objectMapper.registerModule(new JavaTimeModule())
        .configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);

    return new Retrofit.Builder()
        .addConverterFactory(JacksonConverterFactory.create(objectMapper));
  }

  private static List<Interceptor> defaultInterceptors(String channelToken) {
    final Logger slf4jLogger = LoggerFactory.getLogger("com.linecorp.bot.client.wire");
    final HttpLoggingInterceptor httpLoggingInterceptor =
        new HttpLoggingInterceptor(message -> slf4jLogger.info("{}", message));
    httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

    return Arrays.asList(
        new HeaderInterceptor(channelToken),
        httpLoggingInterceptor
    );
  }
}
