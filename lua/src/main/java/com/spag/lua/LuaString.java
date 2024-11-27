package com.spag.lua;

import static com.spag.lua.SoftFlyweightUtil.clearExpiredRefs;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

/**
 * LuaString is a representation of the String type in lua that theoretically preserves all the
 * properties of Lua's strings such as that all strings with the same content are the same string
 * this property is achieved through a soft flyweight
 *
 * @author MR_Spagetty
 */
public class LuaString implements LuaObject, LuaConcatable {
  private static Map<String, SoftReference<LuaString>> cache = new HashMap<>();

  private LuaString(String value) {
    this.value = value;
  }

  @Override
  public LuaString concat(LuaConcatable other) {
    return LuaString.of(
        this.value
            + switch (other) {
              case LuaString otherString -> otherString.value;
              default -> other.toString();
            });
  }

  public final String value;

  /**
   * Gets the LuaString representing the given value, should it not already exist a new LuaString
   * with the given value will be created
   *
   * @param value the value to obtain the LuaString of
   * @return the LuaString
   */
  public static LuaString of(String value) {
    clearExpiredRefs(cache);
    return cache.computeIfAbsent(value, val -> new SoftReference<>(new LuaString(val))).get();
  }

  @Override
  public String toString() {
    return "\"%s\"".formatted(this.value);
  }

  @Override
  public String type() {
    return "LuaString";
  }
}
