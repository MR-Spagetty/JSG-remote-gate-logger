package com.spag.gatelogger.client;

import com.spag.gatelogger.client.data.Gate;
import com.spag.lua.LuaTable;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Label;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GateControlPane extends JPanel {
  public static final Dimension compPaneMin =
      new Dimension((int) (GUI.compPaneMin.width * 1.5), GUI.compPaneMin.height);
  public static final Dimension compPaneMax =
      new Dimension((int) (GUI.compPaneMax.width * 1.5), GUI.compPaneMax.height);
  private JButton dial = new JButton("Dial");
  private JButton close = new JButton("Close");
  private JButton iris = new JButton("Iris");
  private Box controlButtons = new Box(BoxLayout.X_AXIS);

  private GateControlPane() {
    super(new BorderLayout());
    setMinimumSize(compPaneMin);
    setMaximumSize(compPaneMax);
    setPreferredSize(getMaximumSize());
    controlButtons.add(dial);
    controlButtons.add(close);
    controlButtons.add(iris);
    setTo(null);
  }

  private static final GateControlPane INSTANCE = new GateControlPane();

  public static GateControlPane getControlPane() {
    return INSTANCE;
  }

  private Gate selectedGate = null;

  public void setTo(Gate gate) {
    removeAll();
    JLabel paneLabel = new JLabel("Gate Control", JLabel.CENTER);
    paneLabel.setFont(
        new Font(
            paneLabel.getFont().getName(),
            paneLabel.getFont().getStyle(),
            GUI.compHeadingFontSize));
    add(paneLabel, BorderLayout.NORTH);
    Box controlPanel = new Box(BoxLayout.Y_AXIS);
    add(controlPanel, BorderLayout.CENTER);
    this.selectedGate = gate;
    if (selectedGate == null) {
      return;
    }
    controlPanel.add(new Label("Control", Label.CENTER));
    controlPanel.add(this.controlButtons);
  }

  public void refresh() {
    if (selectedGate == null) {
      return;
    }
    LuaTable addressData =
      (LuaTable) Server.query("info", selectedGate.id(), "address").get("data");
    LuaTable dialedData = (LuaTable) Server.query("info", selectedGate.id(), "dialed").get("data");
  }
}
