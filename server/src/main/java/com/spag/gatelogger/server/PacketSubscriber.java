package com.spag.gatelogger.server;

import com.spag.lua.LuaTable;
import java.util.function.Consumer;
import java.util.function.Predicate;

public interface PacketSubscriber {
  public Predicate<LuaTable> classifier();

  public Consumer<LuaTable> handler();
}
