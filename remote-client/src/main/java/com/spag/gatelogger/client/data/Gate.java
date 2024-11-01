package com.spag.gatelogger.client.data;

public record Gate(String id, String name) {
  @Override
  public final String toString() {
    return "%s (%s)".formatted(name(), id());
  }
}
