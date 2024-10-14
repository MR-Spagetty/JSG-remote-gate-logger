package com.spag.gatelogger.lua;

public interface LuaObject {
  public static final LuaObject nil = new LuaObject() {
    @Override
    public String toString() {
      return "nil";
    }
  };

  @Override
  String toString();
}
