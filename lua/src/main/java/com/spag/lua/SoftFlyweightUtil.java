package com.spag.lua;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Simple abstract class for containing utility methods for SofrFlyweights (flyweights that use
 * softreferences)
 *
 * @author MR_Spagetty
 */
public abstract class SoftFlyweightUtil {
  /**
   * clears elements from the given cache where the reference is expired
   *
   * @param <T> the type of the objects being stored in the cache
   * @param cache the cache to clear the references from
   */
  static <T> void clearExpiredRefs(Map<String, SoftReference<T>> cache) {
    cache.entrySet().stream().toList().stream()
        .filter(e -> e.getValue().get() == null)
        .map(Entry::getKey)
        .forEach(cache::remove);
    ;
  }
}
