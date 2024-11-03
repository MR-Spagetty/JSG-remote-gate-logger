package com.spag.gatelogger.client;

import javax.swing.*;

public class GateControlPane extends JPanel {
  private GateControlPane() {}

  private static final GateControlPane INSTANCE = new GateControlPane();

  public static GateControlPane getControlPane() {
    return INSTANCE;
  }

  public void update(String gateID){}
}
