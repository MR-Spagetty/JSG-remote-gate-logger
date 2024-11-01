package com.spag.lua;

import static com.spag.lua.SoftFlyweightUtil.clearUnusedRef;

import java.lang.ref.SoftReference;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class LuaNum implements LuaObject {
  private static final Map<String, SoftReference<LuaNum>> cache = new HashMap<>();
  public final BigDecimal value;

  private LuaNum(BigDecimal value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return this.value.toString();
  }

  public static LuaNum of(String data) {
    clearUnusedRef(cache);
    return cache
        .computeIfAbsent(data, val -> new SoftReference<>(new LuaNum(new BigDecimal(val))))
        .get();
  }
}
