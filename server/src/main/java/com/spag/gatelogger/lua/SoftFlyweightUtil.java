package com.spag.gatelogger.lua;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.Map.Entry;

public class SoftFlyweightUtil {
  static <T>void clearUnusedRef(Map<String, SoftReference<T>> cache) {
    cache.entrySet().stream().toList().stream()
        .filter(e -> e.getValue().get() == null)
        .map(Entry::getKey)
        .forEach(cache::remove);
    ;
  }
}
