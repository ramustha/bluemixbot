package com.ramusthastudio.bluemixbot.model;

public class Events {
  private String type;
  private String replyToken;
  private Source source;
  private long timestamp;
  private Message message;
  private Postback postback;

  public String type() { return type; }
  public String replyToken() { return replyToken; }
  public Source source() { return source; }
  public long timestamp() { return timestamp; }
  public Message message() { return message; }
  public Postback postback() { return postback; }

  @Override public String toString() {
    return "Events{" +
        "type='" + type + "\n" +
        ", replyToken='" + replyToken + "\n" +
        ", source=" + source + "\n" +
        ", timestamp=" + timestamp + "\n" +
        ", message=" + message + "\n" +
        ", postback=" + postback + "\n" +
        '}';
  }
}
