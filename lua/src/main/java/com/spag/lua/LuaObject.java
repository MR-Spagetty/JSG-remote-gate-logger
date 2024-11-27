package com.spag.lua;

public interface LuaObject {
  public static final LuaObject nil =
      new LuaObject() {
        @Override
        public String toString() {
          return "nil";
        }

        @Override
        public String type() {
          return "nil";
        }
      };

  String type();

  @Override
  String toString();
}
