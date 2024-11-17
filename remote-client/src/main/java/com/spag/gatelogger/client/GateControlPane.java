package com.spag.gatelogger.client;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GateControlPane extends JPanel {
  public static final Dimension compPaneMin =
      new Dimension(GUI.compPaneMin.width * 2, GUI.compPaneMin.height);
  public static final Dimension compPaneMax =
      new Dimension(GUI.compPaneMax.width * 2, GUI.compPaneMax.height);

  private GateControlPane() {
    super(new BorderLayout());
    setMinimumSize(compPaneMin);
    setMaximumSize(compPaneMax);
    setPreferredSize(getMaximumSize());
    setTo(null);
  }

  private static final GateControlPane INSTANCE = new GateControlPane();

  public static GateControlPane getControlPane() {
    return INSTANCE;
  }

  public void setTo(String gateID) {
    removeAll();
    JLabel paneLabel = new JLabel("Gate Control", JLabel.CENTER);
    paneLabel.setFont(new Font(paneLabel.getFont().getName(), paneLabel.getFont().getStyle(), GUI.compHeadingFontSize));
    add(paneLabel, BorderLayout.NORTH);
    JPanel controlPanel = new JPanel();
    add(controlPanel, BorderLayout.CENTER);
  }
}
