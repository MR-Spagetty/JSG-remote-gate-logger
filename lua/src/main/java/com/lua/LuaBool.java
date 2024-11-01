package com.spag.lua;

public enum LuaBool implements LuaObject {
  True,
  False;

  @Override
  public String toString() {
    return super.toString().toLowerCase();
  }

  public boolean get() {
    return this == True;
  }

  static LuaBool of(String value) {
    return switch (value) {
      case "true" -> True;
      case "false" -> False;
      default -> throw new IllegalArgumentException("Invalid boolean value: " + value);
    };
  }
}
