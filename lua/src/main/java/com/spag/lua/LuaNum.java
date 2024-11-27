package com.spag.lua;

import static com.spag.lua.SoftFlyweightUtil.clearExpiredRefs;

import java.lang.ref.SoftReference;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * LuaNum is the representation of lua's number type which can represent both integers and decimals
 * like the LuaString type this attempts to maintain all observable properties of the number type in
 * lua
 *
 * @author MR_Spagetty
 */
public class LuaNum implements LuaObject, LuaConcatable {
  private static final Map<String, SoftReference<LuaNum>> cache = new HashMap<>();
  public final BigDecimal value;

  private LuaNum(BigDecimal value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return this.value.toString();
  }

  /**
   * Gets the LuaNum representing the given value, should it not already exist a new LuaNum
   * with the given value will be created
   *
   * @param value the value to obtain the LuaNum of
   * @return the LuaNum
   */
  public static LuaNum of(String data) {
    clearExpiredRefs(cache);
    return cache
        .computeIfAbsent(data, val -> new SoftReference<>(new LuaNum(new BigDecimal(val))))
        .get();
  }

  @Override
  public String type() {
    return "LuaNum";
  }
}
