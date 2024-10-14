package com.spag.gatelogger.lua;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class LuaTable implements LuaObject {
  static final Pattern brace = Pattern.compile("/\\{((?:[^{}]*\\{[^{}]*\\})*[^{}]*?)\\}/");
  static final Pattern num =
      Pattern.compile("[1-9][0-9]*\\\\.[0-9]+" + "|0\\.[0-9]+" + "|0|[1-9][0-9]*");
  static final Pattern string = Pattern.compile("\\\\\\\".*?[^\\\\\\\\]\\\\\\\"");
  static final Pattern indexed =
      Pattern.compile(
          "^("
              + brace.toString()
              + "|"
              + num.toString()
              + "|"
              + string.toString()
              + "|"
              + LuaObject.nil.toString()
              + "),?");
  static final Pattern keyed =
      Pattern.compile(
          "^(?:(\\w+)|\\[\\\"(.+)\\\"])=("
              + brace.toString()
              + "|"
              + num.toString()
              + "|"
              + string.toString()
              + "|"
              + LuaObject.nil.toString()
              + ")");
  private Map<String, LuaObject> dataByKey = new HashMap<>();

  private List<LuaObject> dataByIndex = new ArrayList<>();

  public void ipairs(BiConsumer<Integer, LuaObject> iterator) {
    IntStream.range(
            1, Math.min(this.dataByIndex.indexOf(LuaObject.nil) + 1, this.dataByIndex.size()))
        .forEach(i -> iterator.accept(i, this.dataByIndex.get(i - 1)));
  }

  private void clearNilAssociatedKeys() {
    dataByKey.entrySet().stream()
        .filter(e -> e.getValue() == LuaObject.nil)
        .map(e -> e.getKey())
        .forEach(dataByKey::remove);
  }

  public void pairs(BiConsumer<String, LuaObject> iterator) {
    clearNilAssociatedKeys();
    IntStream.range(0, dataByIndex.size())
        .forEach(i -> iterator.accept("" + i, this.dataByIndex.get(i)));
    dataByKey.entrySet().forEach(e -> iterator.accept(e.getKey(), e.getValue()));
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
    StringBuilder out = new StringBuilder();
    out.append("{");
    this.dataByIndex.forEach(v -> out.append(v.toString() + ","));
    pairs(
        (k, v) ->
            out.append("%s=%s,".formatted(k.contains(" ") ? "[\"" + k + "\"]" : k, v.toString())));
    out.setCharAt(out.lastIndexOf(","), '}');
    return out.toString();
  }

  public static LuaTable fromString(String data) {
    Matcher match = brace.matcher(data);
    if (!match.matches()) {
      throw new IllegalArgumentException("Invalid Lua table");
    }
    LuaTable out = new LuaTable();
    data = match.group(0);
    int end = 0;
    Matcher indexedValues = indexed.matcher(data);
    while (indexedValues.find()) {
      out.add(parseObject(indexedValues.group()));
      end = indexedValues.end();
    }
    Matcher keyedValues = keyed.matcher(data.substring(end));
    while (keyedValues.find()) {
      String key = keyedValues.group(1);
      String value = keyedValues.group(2);
      out.put(key, parseObject(value));
    }
    return out;
  }

  private static LuaObject parseObject(String data) {
    if (data.startsWith("{")) {
      return fromString(data);
    }else if (string.matcher(data).matches()){
      return LuaString.of(data.substring(1, data.length()));
    }else if (num.matcher(data).matches()){
      return LuaNum.of(data);
    }
    return null;
  }
}
