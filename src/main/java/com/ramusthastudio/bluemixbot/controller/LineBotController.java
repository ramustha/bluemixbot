package com.ramusthastudio.bluemixbot.controller;

import com.google.gson.Gson;
import com.linecorp.bot.client.LineSignatureValidator;
import com.ramusthastudio.bluemixbot.model.Events;
import com.ramusthastudio.bluemixbot.model.Message;
import com.ramusthastudio.bluemixbot.model.Payload;
import com.ramusthastudio.bluemixbot.model.Postback;
import com.ramusthastudio.bluemixbot.model.Source;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import static com.ramusthastudio.bluemixbot.util.BotHelper.FOLLOW;
import static com.ramusthastudio.bluemixbot.util.BotHelper.JOIN;
import static com.ramusthastudio.bluemixbot.util.BotHelper.LEAVE;
import static com.ramusthastudio.bluemixbot.util.BotHelper.MESSAGE;
import static com.ramusthastudio.bluemixbot.util.BotHelper.MESSAGE_TEXT;
import static com.ramusthastudio.bluemixbot.util.BotHelper.POSTBACK;
import static com.ramusthastudio.bluemixbot.util.BotHelper.SOURCE_GROUP;
import static com.ramusthastudio.bluemixbot.util.BotHelper.SOURCE_ROOM;
import static com.ramusthastudio.bluemixbot.util.BotHelper.SOURCE_USER;
import static com.ramusthastudio.bluemixbot.util.BotHelper.UNFOLLOW;
import static com.ramusthastudio.bluemixbot.util.BotHelper.greetingMessage;
import static com.ramusthastudio.bluemixbot.util.BotHelper.greetingMessageGroup;
import static com.ramusthastudio.bluemixbot.util.BotHelper.pushMessage;
import static com.ramusthastudio.bluemixbot.util.BotHelper.replayMessage;
import static com.ramusthastudio.bluemixbot.util.BotHelper.unfollowMessage;

@RestController
@RequestMapping(value = "/linebot")
public class LineBotController {
  private static final Logger LOG = LoggerFactory.getLogger(LineBotController.class);

  @Autowired
  @Qualifier("line.bot.channelSecret")
  String fChannelSecret;
  @Autowired
  @Qualifier("line.bot.channelToken")
  String fChannelAccessToken;
  @RequestMapping(value = "/callback", method = RequestMethod.POST)
  public ResponseEntity<String> callback(
      @RequestHeader("X-Line-Signature") String aXLineSignature,
      @RequestBody String aPayload) {

    LOG.info("XLineSignature: {} ", aXLineSignature);
    LOG.info("Payload: {} ", aPayload);

    LOG.info("The Signature is: {} ", (aXLineSignature != null && aXLineSignature.length() > 0) ? aXLineSignature : "N/A");
    final boolean valid = new LineSignatureValidator(fChannelSecret.getBytes()).validateSignature(aPayload.getBytes(), aXLineSignature);
    LOG.info("The Signature is: {} ", valid ? "valid" : "tidak valid");

    LOG.info("Start getting payload ");
    try {

      Gson gson = new Gson();
      Payload payload = gson.fromJson(aPayload, Payload.class);
      Events event = payload.events()[0];

      String eventType = event.type();
      String replayToken = event.replyToken();
      Source source = event.source();
      long timestamp = event.timestamp();
      Message message = event.message();
      Postback postback = event.postback();

      String userId = source.userId();
      String sourceType = source.type();

      LOG.info("source type : {} ", sourceType);
      switch (sourceType) {
        case SOURCE_USER:
          sourceUserProccess(eventType, replayToken, timestamp, message, postback, userId);
          break;
        case SOURCE_GROUP:
          sourceGroupProccess(eventType, replayToken, postback, message, source);
          break;
        case SOURCE_ROOM:
          // sourceGroupProccess(eventType, replayToken, postback, message, source);
          break;
      }
    } catch (Exception ae) {
      LOG.error("Erro process payload : {} ", ae.getMessage());
    }
    return new ResponseEntity<>(HttpStatus.OK);
  }
  private void sourceGroupProccess(String aEventType, String aReplayToken, Postback aPostback, Message aMessage, Source aSource) {
    LOG.info("event : {} ", aEventType);
    try {
      switch (aEventType) {
        case LEAVE:
          unfollowMessage(fChannelAccessToken, aSource.groupId());
          break;
        case JOIN:
          LOG.info("Greeting Message");
          greetingMessageGroup(fChannelAccessToken, aSource.groupId());
          break;
        case MESSAGE:
          if (aMessage.type().equals(MESSAGE_TEXT)) {
            String text = aMessage.text();
            replayMessage(fChannelAccessToken, aReplayToken, text);
          } else {
            pushMessage(fChannelAccessToken, aSource.groupId(), "Aku gak ngerti nih, " +
                "aku ini cuma bot yang bisa membaca ramalan zodiak, jadi jangan tanya yang aneh aneh dulu yah");
          }
          break;
        case POSTBACK:
          break;
      }
    } catch (IOException aE) { LOG.error("Message {}", aE.getMessage()); }
  }

  private void sourceUserProccess(String aEventType, String aReplayToken, long aTimestamp, Message aMessage, Postback aPostback, String aUserId) {
    LOG.info("event : {} ", aEventType);
    try {
      switch (aEventType) {
        case UNFOLLOW:
          unfollowMessage(fChannelAccessToken, aUserId);
          break;
        case FOLLOW:
          LOG.info("Greeting Message");
          greetingMessage(fChannelAccessToken, aUserId);
          break;
        case MESSAGE:
          if (aMessage.type().equals(MESSAGE_TEXT)) {
            String text = aMessage.text();
            replayMessage(fChannelAccessToken, aReplayToken, text);
          } else {
            pushMessage(fChannelAccessToken, aUserId, "Aku gak ngerti nih, " +
                "aku ini cuma bot yang bisa membaca ramalan zodiak, jadi jangan tanya yang aneh aneh dulu yah");
          }
          break;
        case POSTBACK:
          break;
      }
    } catch (IOException aE) { LOG.error("Message {}", aE.getMessage()); }
  }

}
