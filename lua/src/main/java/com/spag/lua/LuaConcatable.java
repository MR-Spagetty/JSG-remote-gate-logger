package com.spag.lua;

/**
 * Simple interface for implimenting the concatable proeprty of some types in lua
 *
 * @author MR_Spagetty
 */
public interface LuaConcatable {
  /**
   * Concats the given value on to the end of this value
   *
   * <p>unless overidden concatination always outouts a LuaString so that is how it has been
   * implimented here
   *
   * @param other the value to concatinate onto this value
   * @return the new concatednated value as
   */
  default LuaString concat(LuaConcatable other) {
    return LuaString.of(
        toString()
            + switch (other) {
              case LuaString string -> string.value;
              default -> other.toString();
            });
  }
}
