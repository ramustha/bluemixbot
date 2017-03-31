package com.ramusthastudio.bluemixbot.model;

public class Message {
  private String type;
  private String id;
  private String text;

  public String type() { return type; }
  public String id() { return id; }
  public String text() { return text; }

  @Override public String toString() {
    return "Message{" +
        "type='" + type + "\n" +
        ", id='" + id + "\n" +
        ", text='" + text + "\n" +
        '}';
  }
}
