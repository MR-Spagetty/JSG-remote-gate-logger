package com.spag.gatelogger.server.data;

import com.spag.gatelogger.lua.LuaBool;
import com.spag.gatelogger.lua.LuaString;
import com.spag.gatelogger.lua.LuaTable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Gate {
  private static final Map<String, Gate> cache = new HashMap<>();
  public final String id;
  private String name;
  List<LuaTable> gateData = new ArrayList<>();
  List<LuaTable> modemData = new ArrayList<>();
  List<LuaTable> otherData = new ArrayList<>();

  Map<GateType, Glyph[]> gateAddresses = new HashMap<>();
  Glyph[] dialedAddress = new Glyph[9];
  private boolean hasDHD = false;

  private Gate(String id) {
    this.id = id;
  }

  private Gate(String id, String name) {
    this(id);
    this.name = name;
  }

  public String name() {
    return this.name;
  }

  public void name(String newName) {
    this.name = newName;
  }

  /*example init packet:
  {"03/02/70 04:28:08",id="1eb0a1e1-9a12-41e9-a297-76bd6485d70d",data={"init",hasDHD=false,dialed="[]",status="idle",name="Chulak"}} */
  public static Gate of(LuaTable init) {
    Gate gate =
        cache.computeIfAbsent(
            ((LuaString) init.get("id")).value,
            id -> new Gate(id, ((LuaString) (((LuaTable) init.get("data")).get("name"))).value));
    gate.hasDHD = ((LuaBool) (((LuaTable) init.get("data")).get("hasDHD"))).get();
    return gate;
  }
}
