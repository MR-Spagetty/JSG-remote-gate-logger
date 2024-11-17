package com.spag.gatelogger.server.data;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public interface Glyph {
  static final Map<GateType, Function<String, Glyph>> glyphGetters = new HashMap<>();
  String name();

  GateType type();
}
