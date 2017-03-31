package com.ramusthastudio.bluemixbot.util;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.linecorp.bot.client.LineMessagingService;
import com.linecorp.bot.model.Multicast;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.action.PostbackAction;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TemplateMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.template.ConfirmTemplate;
import com.linecorp.bot.model.message.template.Template;
import com.linecorp.bot.model.profile.UserProfileResponse;
import com.linecorp.bot.model.response.BotApiResponse;
import com.ramusthastudio.bluemixbot.http.HeaderInterceptor;
import java.io.IOException;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.ConnectionSpec;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import static com.ramusthastudio.bluemixbot.util.StickerHelper.JAMES_STICKER_TWO_THUMBS;

public final class BotHelper {
  private static final Logger LOG = LoggerFactory.getLogger(BotHelper.class);

  public static final String DEFAULT_API_END_POINT = "https://api.line.me/";
  public static final long DEFAULT_CONNECT_TIMEOUT = 10_000;
  public static final long DEFAULT_READ_TIMEOUT = 10_000;
  public static final long DEFAULT_WRITE_TIMEOUT = 10_000;

  public static final String SOURCE_USER = "user";
  public static final String SOURCE_GROUP = "group";
  public static final String SOURCE_ROOM = "room";

  public static final String JOIN = "join";
  public static final String FOLLOW = "follow";
  public static final String UNFOLLOW = "unfollow";
  public static final String MESSAGE = "message";
  public static final String LEAVE = "leave";
  public static final String POSTBACK = "postback";
  public static final String BEACON = "beacon";

  public static final String MESSAGE_TEXT = "text";
  public static final String MESSAGE_IMAGE = "image";
  public static final String MESSAGE_VIDEO = "video";
  public static final String MESSAGE_AUDIO = "audio";
  public static final String MESSAGE_LOCATION = "location";
  public static final String MESSAGE_STICKER = "sticker";

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

  private static LineMessagingService lineServiceBuilder(String aChannelAccessToken) {
    OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder();
    for (Interceptor interceptor : defaultInterceptors(aChannelAccessToken)) {
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

    LOG.info("Starting new line messaging service...");
    return retrofit.create(LineMessagingService.class);
  }

  public static OkHttpClient.Builder enableTls12(OkHttpClient.Builder client) {
    try {
      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
          TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init((KeyStore) null);
      TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
      if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
        throw new IllegalStateException("Unexpected default trust managers:"
            + Arrays.toString(trustManagers));
      }
      X509TrustManager trustManager = (X509TrustManager) trustManagers[0];

      SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
      sslContext.init(null, new TrustManager[] {trustManager}, null);
      SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
      client.sslSocketFactory(sslSocketFactory, trustManager);

      ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
          .allEnabledTlsVersions()
          .allEnabledCipherSuites()
          .supportsTlsExtensions(true)
          .build();
      client.connectionSpecs(Collections.singletonList(cs));
    } catch (Exception exc) {
      LOG.error("Error while setting {}", exc.getMessage());
    }
    return client;
  }

  public static UserProfileResponse getUserProfile(String aChannelAccessToken,
      String aUserId) throws IOException {
    LOG.info("getUserProfile...");
    return lineServiceBuilder(aChannelAccessToken).getProfile(aUserId).execute().body();
  }

  public static Response<BotApiResponse> replayMessage(String aChannelAccessToken, String aReplayToken,
      String aMsg) throws IOException {
    TextMessage message = new TextMessage(aMsg);
    ReplyMessage pushMessage = new ReplyMessage(aReplayToken, message);
    LOG.info("replayMessage...");
    return lineServiceBuilder(aChannelAccessToken).replyMessage(pushMessage).execute();
  }

  public static Response<BotApiResponse> pushMessage(String aChannelAccessToken, String aUserId,
      String aMsg) throws IOException {
    TextMessage message = new TextMessage(aMsg);
    PushMessage pushMessage = new PushMessage(aUserId, message);
    LOG.info("pushMessage...");
    return lineServiceBuilder(aChannelAccessToken).pushMessage(pushMessage).execute();
  }

  public static Response<BotApiResponse> multicastMessage(String aChannelAccessToken, Set<String> aUserIds,
      String aMsg) throws IOException {
    TextMessage message = new TextMessage(aMsg);
    Multicast pushMessage = new Multicast(aUserIds, message);
    LOG.info("multicastMessage...");
    return lineServiceBuilder(aChannelAccessToken).multicast(pushMessage).execute();
  }

  public static Response<BotApiResponse> templateMessage(String aChannelAccessToken, String aUserId,
      Template aTemplate) throws IOException {
    TemplateMessage message = new TemplateMessage("Result", aTemplate);
    PushMessage pushMessage = new PushMessage(aUserId, message);
    LOG.info("templateMessage...");
    return lineServiceBuilder(aChannelAccessToken).pushMessage(pushMessage).execute();
  }

  public static Response<BotApiResponse> stickerMessage(String aChannelAccessToken, String aUserId,
      StickerHelper.StickerMsg aSt) throws IOException {
    StickerMessage message = new StickerMessage(aSt.pkgId(), aSt.id());
    PushMessage pushMessage = new PushMessage(aUserId, message);
    LOG.info("stickerMessage...");
    return lineServiceBuilder(aChannelAccessToken).pushMessage(pushMessage).execute();
  }

  public static void greetingMessageGroup(String aChannelAccessToken, String aUserId) throws IOException {
    String greeting = "Hi manteman\n";
    greeting += "Makasih aku udah di invite disini!\n";
    greeting += "Aku Zodi, bot yang bisa membaca zodiak dari nama dan tanggal lahir, ";
    greeting += "buat kamu yang pengen tau ramalan zodiak, percintaan, keuangan kamu hari ini caranya gampang, ";
    greeting += "kamu tinggal tulis aja nama dan tanggal lahir kamu seperti ini : ramalan dadang 27-03-1991\n\n";
    greeting += "Kalau kamu suka dengan aku, bantuin aku donk supaya punya banyak teman, ini id aku @yjb9380i";
    stickerMessage(aChannelAccessToken, aUserId, new StickerHelper.StickerMsg(JAMES_STICKER_TWO_THUMBS));
    pushMessage(aChannelAccessToken, aUserId, greeting);
  }

  public static void greetingMessage(String aChannelAccessToken, String aUserId) throws IOException {
    UserProfileResponse userProfile = getUserProfile(aChannelAccessToken, aUserId);
    String greeting = "Hi " + userProfile.getDisplayName() + "\n";
    greeting += "Makasih udah nambahin aku sebagai teman!\n";
    greeting += "Aku Zodi, bot yang bisa membaca zodiak dari nama dan tanggal lahir, ";
    greeting += "buat kamu yang pengen tau ramalan zodiak, percintaan, keuangan kamu hari ini caranya gampang, ";
    greeting += "kamu tinggal tulis aja nama dan tanggal lahir kamu seperti ini : ramalan dadang 27-03-1991\n\n";
    greeting += "Kalau kamu suka dengan aku, bantuin aku donk supaya punya banyak teman, ini id aku @yjb9380i";
    stickerMessage(aChannelAccessToken, aUserId, new StickerHelper.StickerMsg(JAMES_STICKER_TWO_THUMBS));
    pushMessage(aChannelAccessToken, aUserId, greeting);
  }

  public static void unfollowMessage(String aChannelAccessToken, String aUserId) throws IOException {
    UserProfileResponse userProfile = getUserProfile(aChannelAccessToken, aUserId);
    String greeting = "Hi " + userProfile.getDisplayName() + "\n";
    greeting += "Kenapa kamu unfollow aku? jahat !!!";
    pushMessage(aChannelAccessToken, aUserId, greeting);
  }

  public static void instructionTweetsMessage(String aChannelAccessToken, String aUserId) throws IOException {
    UserProfileResponse userProfile = getUserProfile(aChannelAccessToken, aUserId);
    String greeting = "Hi " + userProfile.getDisplayName() + "\n";
    greeting += "Kamu tinggal tulis aja nama dan tanggal lahir kamu seperti ini : ramalan dadang 27-03-1991";
    pushMessage(aChannelAccessToken, aUserId, greeting);
  }

  public static Response<BotApiResponse> confirmTwitterMessage(String aChannelAccessToken, String aUserId, String aMsg, String aDataYes, String aDataNo) throws IOException {
    ConfirmTemplate template = new ConfirmTemplate(aMsg, Arrays.asList(
        new PostbackAction("Bener", aDataYes),
        new PostbackAction("Salah", aDataNo)
    ));
    return templateMessage(aChannelAccessToken, aUserId, template);
  }

  public static int generateRandom(int min, int max) {
    Random r = new Random();
    return r.nextInt(max - min) + min;
  }

  public static String predictWord(String aText, String aFind) {
    Pattern word = Pattern.compile(aFind);
    Matcher match = word.matcher(aText);
    String result = "";
    while (match.find()) {
      String predictAfterKey = removeAnySymbol(aText.substring(match.end(), aText.length())).trim();

      if (predictAfterKey.length() > 0) {
        if (predictAfterKey.contains(" ")) {
          String[] predictAfterKeySplit = predictAfterKey.split(" ");
          result = predictAfterKeySplit[0];
        } else {
          result = predictAfterKey;
        }
        return result;
      }
    }
    return result;
  }

  public static String removeAnySymbol(String s) {
    Pattern pattern = Pattern.compile("[^a-z A-Z^0-9]");
    Matcher matcher = pattern.matcher(s);
    return matcher.replaceAll(" ");
  }
}
