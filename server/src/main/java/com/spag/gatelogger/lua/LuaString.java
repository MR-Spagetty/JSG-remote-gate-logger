package com.spag.gatelogger.lua;

import java.util.HashMap;
import java.util.Map;

public class LuaString implements LuaObject {
  private static Map<String, LuaString> cache = new HashMap<String, LuaString>();

  private LuaString(String value) {
    this.value = value;
  }

  public LuaString concat(LuaConcatable other) {
    return LuaString.of(
        this.value
            + switch (other) {
              case LuaString otherString -> otherString.value;
              default -> other.toString();
            });
  }

  public final String value;

  public static LuaString of(String value) {
    return cache.computeIfAbsent(value, val -> new LuaString(val));
  }

  @Override
  public String toString() {
    return "\"%s\"".formatted(this.value);
  }
}
