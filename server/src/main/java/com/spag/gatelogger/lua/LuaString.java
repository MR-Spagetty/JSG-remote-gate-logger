package com.spag.gatelogger.lua;

import static com.spag.gatelogger.lua.SoftFlyweightUtil.clearUnusedRef;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

public class LuaString implements LuaObject {
  private static Map<String, SoftReference<LuaString>> cache = new HashMap<>();

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
    clearUnusedRef(cache);
    return cache.computeIfAbsent(value, val -> new SoftReference<>(new LuaString(val))).get();
  }

  @Override
  public String toString() {
    return "\"%s\"".formatted(this.value);
  }
}
