package com.spag.gatelogger.client;

import com.spag.gatelogger.client.data.Gate;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GateControlPane extends JPanel {
  public static final Dimension compPaneMin =
      new Dimension((int) (GUI.compPaneMin.width * 1.5), GUI.compPaneMin.height);
  public static final Dimension compPaneMax =
      new Dimension((int) (GUI.compPaneMax.width * 1.5), GUI.compPaneMax.height);

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

  public void setTo(Gate gate) {
    removeAll();
    JLabel paneLabel = new JLabel("Gate Control", JLabel.CENTER);
    paneLabel.setFont(
        new Font(
            paneLabel.getFont().getName(),
            paneLabel.getFont().getStyle(),
            GUI.compHeadingFontSize));
    add(paneLabel, BorderLayout.NORTH);
    JPanel controlPanel = new JPanel();
    add(controlPanel, BorderLayout.CENTER);
  }
}
