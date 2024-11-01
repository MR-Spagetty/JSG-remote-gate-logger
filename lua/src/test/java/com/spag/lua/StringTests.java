package com.spag.lua;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class StringTests {
  @Test
  void basicString() {
    String s = "test";
    LuaString luaS = LuaString.of(s);
    assertEquals(s, luaS.value);
  }

  @Test
  void testToString() {
    String s = "test";
    LuaString luaS = LuaString.of(s);
    assertEquals('"' + s + '"', luaS.toString());
  }

  @Test
  void flyweightOneVal() {
    String s = "test";
    LuaString luaS1 = LuaString.of(s);
    LuaString luaS2 = LuaString.of(s);
    assert luaS1 == luaS2;
  }

  @Test
  void flyweightTwoVal() {
    String sA = "test";
    String sB = "test2";
    LuaString luaSA1 = LuaString.of(sA);
    LuaString luaSA2 = LuaString.of(sA);
    LuaString luaSB1 = LuaString.of(sB);
    LuaString luaSB2 = LuaString.of(sB);
    assert luaSA1 == luaSA2;
    assert luaSB1 == luaSB2;
  }

  @Test
  void flyweightThreeRef() {
    String s = "test";
    LuaString luaS1 = LuaString.of(s);
    LuaString luaS2 = LuaString.of(s);
    LuaString luaS3 = LuaString.of(s);
    assert luaS1 == luaS2;
    assert luaS1 == luaS3;
  }
}
