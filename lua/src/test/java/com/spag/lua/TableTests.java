package com.spag.lua;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class TableTests {
  @Test
  void basicAdd() {
    LuaTable table = new LuaTable();
    var s = LuaString.of("test");
    table.add(s);
    var b = LuaBool.of("true");
    table.add(b);
    var b2 = LuaBool.of("false");
    table.add(b2);
    var i = LuaNum.of("123");
    table.add(i);
    var f = LuaNum.of("1.23");
    table.add(f);
    var zero = LuaNum.of("0");
    table.add(zero);
    var zeroFloat = LuaNum.of("0.123");
    table.add(zeroFloat);
    table.add(LuaObject.nil);

    assertEquals(s, table.get(1));
    assertEquals(b, table.get(2));
    assertEquals(b2, table.get(3));
    assertEquals(i, table.get(4));
    assertEquals(f, table.get(5));
    assertEquals(zero, table.get(6));
    assertEquals(zeroFloat, table.get(7));
    assertEquals(LuaObject.nil, table.get(8));
  }

  @Test
  void put() {

    LuaTable table = new LuaTable();
    var s = LuaString.of("test");
    table.put("String", s);
    var b = LuaBool.of("true");
    table.put("T", b);
    var b2 = LuaBool.of("false");
    table.put("F", b2);
    var i = LuaNum.of("123");
    table.put("int", i);
    var f = LuaNum.of("1.23");
    table.put("float", f);
    var zero = LuaNum.of("0");
    table.put("zero", zero);
    var zeroFloat = LuaNum.of("0.123");
    table.put("zero float", zeroFloat);
    table.put("nil", LuaObject.nil);

    assertEquals(s, table.get("String"));
    assertEquals(b, table.get("T"));
    assertEquals(b2, table.get("F"));
    assertEquals(i, table.get("int"));
    assertEquals(f, table.get("float"));
    assertEquals(zero, table.get("zero"));
    assertEquals(zeroFloat, table.get("zero float"));
    assertEquals(LuaObject.nil, table.get("nil"));
    assertEquals(LuaObject.nil, table.get("not found"));
  }

  @Test
  void emptyTableToString() {
    LuaTable table = new LuaTable();
    assertEquals("{}", table.toString());
  }

  @Test
  void nilOnlyTableToString() {
    LuaTable table = new LuaTable();
    table.put("whatever", LuaObject.nil);
    assertEquals("{}", table.toString());
  }

  @Test
  void stringEqual() {
    String date = "03/02/70 04:28:08";
    String id = "1eb0a1e1-9a12-41e9-a297-76bd6485d70d";
    String start =
        "{\""
            + date
            + "\","
            + "id=\""
            + id
            + "\","
            + "data={\"init\",hasDHD=false,dialed=\"[]\",status=\"idle\",name=\"Chulak\"},"
            +"two={threee={4}}}";

    LuaTable intermed = LuaTable.fromString(start);
    assertEquals(LuaString.of(date), intermed.get(1));
    assertEquals(LuaString.of(id), intermed.get("id"));
    LuaTable data = (LuaTable) intermed.get("data");
    assertEquals(LuaString.of("init"), data.get(1));
    assertEquals(LuaBool.False, data.get("hasDHD"));
    assertEquals(LuaString.of("[]"), data.get("dialed"));
    assertEquals(LuaString.of("idle"), data.get("status"));
    assertEquals(LuaString.of("Chulak"), data.get("name"));
    assertEquals(start, intermed.toString());
  }

  @Test
  void nilToEmpty() {
    String init = "{wow=nil,a=nil,nil=nil}";
    assertEquals("{}", LuaTable.fromString(init).toString());
  }

  @Test
  void indexOnlyTableToString() {
    LuaTable table = new LuaTable();
    table.add(LuaString.of("test"));
    table.add(LuaBool.of("true"));
    table.add(LuaBool.of("false"));
    table.add(LuaNum.of("123"));
    table.add(LuaNum.of("1.23"));
    table.add(LuaNum.of("0"));
    table.add(LuaNum.of("0.123"));
    table.add(LuaObject.nil);
    table.add(new LuaTable());
    assertEquals("{\"test\",true,false,123,1.23,0,0.123,nil,{}}", table.toString());
  }
}
