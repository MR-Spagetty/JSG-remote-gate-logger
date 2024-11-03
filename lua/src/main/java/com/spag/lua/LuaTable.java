package com.spag.lua;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class LuaTable implements LuaObject {
  public static final String braceRegex = "\\{((?:[^{}]*\\{[^{}]*\\})*[^{}]*?)\\}";
  public static final Pattern bracePat = Pattern.compile(braceRegex);
  static final String numRegex = "[1-9][0-9]*\\.[0-9]+" + "|0\\.[0-9]+" + "|0|[1-9][0-9]*";
  public static final Pattern numPat = Pattern.compile(numRegex);
  static final String stringRegex = "\\\".*?[^\\\\]\\\"";
  public static final Pattern stringPat = Pattern.compile(stringRegex);
  public static final Pattern indexed =
      Pattern.compile(
          "\\G\\s*("
              + braceRegex
              + "|"
              + numRegex
              + "|"
              + stringRegex
              + "|"
              + LuaObject.nil.toString()
              + "|true|false)\\s*,?\\s*");
  public static final Pattern keyed =
      Pattern.compile(
          "\\G\\s*(?:(\\w+)|\\[\\\"(.+)\\\"])=("
              + braceRegex
              + "|"
              + numRegex
              + "|"
              + stringRegex
              + "|"
              + LuaObject.nil.toString()
              + "|true|false)\\s*,?\\s*");
  private Map<String, LuaObject> dataByKey = new LinkedHashMap<>();

  private List<LuaObject> dataByIndex = new ArrayList<>();

  public void ipairs(BiConsumer<Integer, LuaObject> iterator) {
    IntStream.range(
            1, Math.min(this.dataByIndex.indexOf(LuaObject.nil) + 1, this.dataByIndex.size()))
        .forEach(i -> iterator.accept(i, this.dataByIndex.get(i - 1)));
  }

  private void clearNilAssociatedKeys() {
    dataByKey.entrySet().stream().toList().stream()
        .filter(e -> e.getValue() == LuaObject.nil)
        .map(e -> e.getKey())
        .forEach(dataByKey::remove);
  }

  public void pairs(BiConsumer<String, LuaObject> iterator) {
    clearNilAssociatedKeys();
    IntStream.range(1, dataByIndex.size())
        .forEach(i -> iterator.accept("" + i, this.dataByIndex.get(i)));
    dataByKey.entrySet().forEach(e -> iterator.accept(e.getKey(), e.getValue()));
  }

  public Stream<LuaObject> stream() {
    return this.dataByIndex.stream();
  }

  public Stream<LuaObject> parallelStream() {
    return this.dataByIndex.parallelStream();
  }

  public LuaObject get(String key) {
    return dataByKey.getOrDefault(key, LuaObject.nil);
  }

  public LuaObject put(String key, LuaObject value) {
    try {
      return this.dataByKey.put(key, value);
    } finally {
      clearNilAssociatedKeys();
    }
  }

  public LuaObject get(int index) {
    if (index < 1 || index > this.dataByIndex.size()) {
      return LuaObject.nil;
    }
    return this.dataByIndex.get(index - 1);
  }

  public void add(LuaObject value) {
    this.dataByIndex.add(value);
  }

  public void add(int index, LuaObject value) {
    this.dataByIndex.add(index - 1, value);
  }

  public int size() {
    return this.dataByIndex.size();
  }

  @Override
  public String toString() {
    if (this.dataByIndex.isEmpty() && this.dataByKey.isEmpty()) {
      return "{}";
    }
    StringBuilder out = new StringBuilder();
    out.append("{");
    this.dataByIndex.forEach(v -> out.append(v + ","));
    this.dataByKey
        .entrySet()
        .forEach(
            e ->
                out.append(
                    "%s=%s,"
                        .formatted(
                            e.getKey().contains(" ") ? "[\"" + e.getKey() + "\"]" : e.getKey(),
                            e.getValue().toString())));
    out.setCharAt(out.lastIndexOf(","), '}');
    return out.toString();
  }

  public static LuaTable fromString(String data) {
    Matcher match = bracePat.matcher(data);
    if (!match.matches()) {
      throw new IllegalArgumentException("Invalid Lua table: " + data);
    }
    LuaTable out = new LuaTable();
    data = match.group(1);
    Matcher indexedValues = indexed.matcher(data);
    Matcher keyedValues = keyed.matcher(data);
    int end = 0;
    while (indexedValues.find(end) || keyedValues.find(end)) {
      while (indexedValues.find(end)) {
        out.add(parseObject(indexedValues.group(1)));
        end = indexedValues.end();
      }
      while (keyedValues.find(end)) {
        String key = keyedValues.group(1);
        String value = keyedValues.group(3);
        out.put(key, parseObject(value));
        end = indexedValues.end();
      }
    }
    return out;
  }

  private static LuaObject parseObject(String data) {
    Objects.requireNonNull(data, "Lua data may not be null");
    if (data.startsWith("{")) {
      return fromString(data);
    } else if (stringPat.matcher(data).matches()) {
      return LuaString.of(data.substring(1, data.length() - 1));
    } else if (numPat.matcher(data).matches()) {
      return LuaNum.of(data);
    } else if (data.equals("true") || data.equals("false")) {
      return LuaBool.of(data);
    } else if (data.equals(LuaObject.nil.toString())) {
      return LuaObject.nil;
    }
    throw new IllegalArgumentException("Unrecognised type detected: " + data);
  }

  public LuaTable merge(LuaTable b) {
    Stream.concat(stream(), b.stream()).forEach(this::add);
    Stream.concat(
            this.dataByKey.entrySet().parallelStream()
                .filter(e -> !b.dataByKey.containsKey(e.getKey())),
            b.dataByKey.entrySet().parallelStream())
        .forEach(e -> put(e.getKey(), e.getValue()));
    return this;
  }

  public static LuaTable merge(LuaTable a, LuaTable b) {
    LuaTable out = new LuaTable();
    out.merge(a);
    out.merge(b);
    return out;
  }

  public LuaObject replace(int i, LuaObject newElm) {
    return this.dataByIndex.set(i - 1, newElm);
  }
}
