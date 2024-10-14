package com.spag.gatelogger.lua;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class LuaNum implements LuaObject {
  private static final Map<String, LuaNum> cache = new HashMap<>();
  public final BigDecimal value;

  private LuaNum(BigDecimal value) {
    this.value = value;
  }


  public static LuaNum of(String data) {
    return cache.computeIfAbsent(data, val -> new LuaNum(new BigDecimal(val)));
  }
}
