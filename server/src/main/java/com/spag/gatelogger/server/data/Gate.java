package com.spag.gatelogger.server.data;

import com.spag.lua.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Gate {
  private static final Map<String, Gate> cache = new HashMap<>();
  public final String id;
  private String name;
  private GateType type;
  List<LuaTable> gateData = new ArrayList<>();
  List<LuaTable> modemData = new ArrayList<>();
  List<LuaTable> otherData = new ArrayList<>();

  public Map<GateType, Glyph[]> gateAddresses = new HashMap<>();
  public Glyph[] dialedAddress = new Glyph[9];
  private boolean hasDHD = false;

  private Gate(String id) {
    this.id = id;
  }

  private Gate(String id, String name, GateType type) {
    this(id);
    this.name = name;
    this.type = type;
  }

  private Gate(String id, LuaTable data) {
    this(id);
    Optional<LuaObject> name = Optional.of(data.get("name")).filter(elm -> elm == LuaObject.nil);
    if (name.isPresent()) {
      LuaObject nameO = name.get();
      if (nameO instanceof LuaString nameString) {
        this.name = nameString.value;
      } else {
        throw new DataFormatException(
            "Expected a LuaString for name but got: \"%s\"".formatted(nameO.type()));
      }
    }
    Optional<LuaObject> type = Optional.of(data.get("name")).filter(elm -> elm == LuaObject.nil);
    if (type.isPresent()) {
      LuaObject typeO = type.get();
      if (typeO instanceof LuaString typeString) {
        this.type = GateType.valueOf(typeString.value);
      } else {
        throw new DataFormatException(
            "Expected a LuaString for name but got: \"%s\"".formatted(typeO.type()));
      }
    }
  }

  public String name() {
    return this.name;
  }

  public void logData(LuaTable data) {
    String type =
        Optional.ofNullable(data.get("type"))
            .map(t -> (LuaString) t)
            .map(t -> t.value)
            .orElseThrow(() -> new DataFormatException("no packet type given"));
    switch (type) {
      case "stargate" -> this.gateData.add(data);
      case "modem" -> this.modemData.add(data);
      case "other" -> this.otherData.add(data);
      default -> throw new DataFormatException("Unknown Gate packet type: \"%s\"".formatted(type));
    }
  }

  public void name(String newName) {
    this.name = newName;
  }

  public GateType type() {
    return this.type;
  }

  public Glyph[] address(GateType type) {
    return Optional.ofNullable(this.gateAddresses.get(type))
        .orElseThrow(
            () -> new IllegalArgumentException("Unknown Gate type: \"%s\"".formatted(type)));
  }

  public LuaTable addressesAsLua() {
    LuaTable addreses = new LuaTable();
    gateAddresses.entrySet().stream()
        .forEach(
            ae -> {
              LuaTable addr = new LuaTable();
              Stream.of(ae.getValue()).map(g -> LuaString.of(g.name())).forEach(addr::add);
              addreses.put(ae.getKey().toString(), addr);
            });
    return addreses;
  }

  public void updateAddresses(LuaTable addreses) {
    addreses.pairs(
        (t, addr) -> {
          GateType type = GateType.valueOf(t);
          this.gateAddresses.put(type, addressOf(type, (LuaTable) addr));
        });
  }

  public boolean hasDHD() {
    return this.hasDHD;
  }

  /*example init packet:
  {"03/02/70 04:28:08",id="1eb0a1e1-9a12-41e9-a297-76bd6485d70d",type="init",data={"init",hasDHD=false,dialed="[]",status="idle",gateType="MILKYWAY",name="Chulak"}} */
  public static Gate of(LuaTable init) {
    LuaObject id = init.get("id");
    if (!(id instanceof LuaString)) {
      throw new DataFormatException(
          "Expected LuaString for id, got: \"%s\" in:\n%s".formatted(id.type(), init));
    }
    Gate gate;
    LuaObject data = init.get("data");
    if (data != LuaObject.nil) {
      if (!(data instanceof LuaTable)) {
        throw new DataFormatException(
            "Expected LuaTable or nil for data but got \"%s\"".formatted(data.type()));
      }
      gate =
          cache.computeIfAbsent(((LuaString) id).value, newID -> new Gate(newID, (LuaTable) data));
      gate.hasDHD = ((LuaBool) (((LuaTable) init.get("data")).get("hasDHD"))).get();
    } else {
      gate =
          Optional.ofNullable(cache.get(((LuaString) id).value))
              .orElseThrow(() -> new IllegalArgumentException("Unknown gate: %s".formatted(id)));
    }
    return gate;
  }

  public static Glyph[] addressOf(GateType type, String serialAddress) {
    Glyph[] glyphs = new Glyph[9];
    final Pattern serialAddr =
        Pattern.compile(
            "^\\[(?:([^,]+), )?(?:([^,]+), )?(?:([^,]+), )?(?:([^,]+), )?(?:([^,]+), )?(?:([^,]+),"
                + " )?(?:([^,]+), )?(?:([^,]+), )?(.*?)]$");
    MatchResult mr = serialAddr.matcher(serialAddress).toMatchResult();
    if (!mr.hasMatch()) {
      throw new IllegalArgumentException("Invalid serialized address: " + serialAddress);
    }
    for (int i = 0; i < Math.min(glyphs.length, mr.groupCount()); i++) {
      glyphs[i] = Glyph.glyphGetters.get(type).apply(mr.group());
    }
    return glyphs;
  }

  public static Glyph[] addressOf(GateType type, LuaTable address) {
    Glyph[] glyphs = new Glyph[9];
    for (int i = 0; i < glyphs.length; i++) {
      glyphs[i] = Glyph.glyphGetters.get(type).apply(((LuaString) address.get(i)).value);
    }
    return glyphs;
  }

  public static Glyph[] addressOf(GateType type, LuaObject address) {
    if (address instanceof LuaString serialAddr) {
      return addressOf(type, serialAddr.value);
    } else if (address instanceof LuaTable tableAddr) {
      return addressOf(type, tableAddr);
    } else {
      throw new DataFormatException(
          "Expected LuaString or LuaTable for address data but got \"%s\""
              .formatted(address.type()));
    }
  }
}
