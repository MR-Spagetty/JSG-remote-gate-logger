package com.spag.gatelogger.client.util;

import com.spag.gatelogger.client.data.Glyph;
import java.util.stream.IntStream;
import javax.swing.Box;
import javax.swing.BoxLayout;

public class AddressDisplay extends Box {
  private DisplayButton[] buttons = new DisplayButton[9];

  public AddressDisplay() {
    super(BoxLayout.X_AXIS);
    IntStream.range(0, 9)
        .forEach(
            i -> {
              buttons[i] = new DisplayButton();
              setGlyph(i, Glyph.of());
              add(buttons[i]);
            });
  }

  public void setGlyph(int i, Glyph g) {
    if (i < 0 || i >= 9) {
      throw new IndexOutOfBoundsException(
          "index must be within the range 0 (inclusive) and 9 (exclusive)");
    }
    buttons[i].setIcon(g.icon);
    buttons[i].setText(g == Glyph.of() ? "" : g.name);
  }
}
