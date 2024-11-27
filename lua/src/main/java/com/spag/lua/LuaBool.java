package com.spag.lua;

/**
 * Simple enum for representing the Booleans from lua
 *
 * @author MR_Spagetty
 */
public enum LuaBool implements LuaObject {
  True,
  False;

  @Override
  public String toString() {
    return super.toString().toLowerCase();
  }

  /**
   * Gets the java boolean value equivilent of the LuaBool constant
   *
   * @return java boolean equivilent
   */
  public boolean get() {
    return this == True;
  }

  /**
   * converts Strings to the approprite LuaBool constant
   *
   * <p>for use in parseing serialized lua data
   *
   * @param value the string to encode
   * @return the LuaBool constant
   * @throws IllegalArgumentException if an invalid string is given (ie. not "true" or "false")
   */
  static LuaBool of(String value) {
    return switch (value) {
      case "true" -> True;
      case "false" -> False;
      default -> throw new IllegalArgumentException("Invalid boolean value: " + value);
    };
  }

  /**
   * converts a java boolean to the equivilent LuaBool representation
   * @param value the java boolean
   * @return the LuaBool constant equivilent
   */
  public static LuaBool of(boolean value) {
    return value ? True : False;
  }

  @Override
  public String type() {
    return "LuaBool";
  }
}
