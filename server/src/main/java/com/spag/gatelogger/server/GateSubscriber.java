package com.spag.gatelogger.server;

import java.util.function.Consumer;
import java.util.function.Predicate;

import com.spag.lua.*;

public record GateSubscriber(Predicate<LuaTable> classifier, Consumer<LuaTable> handler) {

  public GateSubscriber(String type, Consumer<LuaTable> handler) {
    this(p -> ((LuaString) ((LuaTable) p.get("data")).get(1)).value.equals(type), handler);
  }
}
