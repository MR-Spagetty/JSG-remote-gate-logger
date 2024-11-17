package com.spag.gatelogger.client.data;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

public class Glyph {
  private static final Map<String, SoftReference<Glyph>> cache = new HashMap<>();

  public final Icon icon;
  public final String name;

  private Glyph() {
    this(Glyph.class.getClassLoader().getResource("glyphs/none.png"), "None");
  }

  private Glyph(String type, String name) {
    this(Glyph.class.getClassLoader().getResource("glyphs/" + type + '/' + name.toLowerCase() + ".png"), name);
  }

  private Glyph(URL imagePath, String name) {
    try {
      this.icon = new ImageIcon(ImageIO.read(imagePath));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    this.name = name;
  }

  private static void clean() {
    cache.entrySet().parallelStream()
        .filter(e -> e.getValue().get() == null)
        .map(Entry::getKey)
        .forEach(cache::remove);
  }

  public static Glyph of(String type, String name) {
    clean();
    return cache
        .computeIfAbsent(type + '.' + name, _ -> new SoftReference<>(new Glyph(type, name)))
        .get();
  }

  public static Glyph of() {
    clean();
    return cache.computeIfAbsent("None", _ -> new SoftReference<>(new Glyph())).get();
  }
}
