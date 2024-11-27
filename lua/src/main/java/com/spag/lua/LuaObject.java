package com.spag.lua;

/**
 * Simple interface that all lua type classes extend
 *
 * @author MR_Spagetty
 */
public interface LuaObject {
  /**
   * The lua nul object, equivilent to java's null, is the object that is given when a non existnant
   * value is accessed in lua
   */
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

  /**
   * The name of the lua type of this LuaObject.
   *
   * <p>useful for error messages
   *
   * @return the short String anme of the type
   */
  String type();

  @Override
  String toString();
}
