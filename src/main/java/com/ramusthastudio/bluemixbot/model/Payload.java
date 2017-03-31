package com.ramusthastudio.bluemixbot.model;

import java.util.Arrays;

public class Payload {
  private Events[] events;

  public Events[] events() {
    return events;
  }

  @Override public String toString() {
    return "Payload{" +
        "events=" + Arrays.toString(events) +
        '}';
  }
}
