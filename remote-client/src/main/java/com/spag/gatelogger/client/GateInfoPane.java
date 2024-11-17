package com.spag.gatelogger.client;

import com.spag.gatelogger.client.data.Gate;
import com.spag.gatelogger.client.util.DisplayButton;
import com.spag.lua.*;
import java.awt.Font;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GateInfoPane extends JPanel {
  private static final GateInfoPane INSTANCE = new GateInfoPane();

  static GateInfoPane getInfoPane() {
    return INSTANCE;
  }

  JButton name = new DisplayButton();
  JButton compAddr = new DisplayButton();
  JButton gateType = new DisplayButton();
  JButton status = new DisplayButton();
  JButton hasDHD = new DisplayButton();

  private GateInfoPane() {
    Box content = new Box(BoxLayout.Y_AXIS);
    JLabel nameLabel = new JLabel("Name", JLabel.CENTER);
    nameLabel.setFont(
        new Font(
            nameLabel.getFont().getName(),
            nameLabel.getFont().getStyle(),
            GUI.compItemHeadingFontSize));
    content.add(nameLabel);
    content.add(this.name);

    JLabel compAddrLabel = new JLabel("Component Address", JLabel.CENTER);
    compAddrLabel.setFont(
        new Font(
            compAddrLabel.getFont().getName(),
            compAddrLabel.getFont().getStyle(),
            GUI.compItemHeadingFontSize));
    content.add(compAddrLabel);
    content.add(this.compAddr);

    JLabel gateTypeLabel = new JLabel("Component Address", JLabel.CENTER);
    gateTypeLabel.setFont(
        new Font(
            gateTypeLabel.getFont().getName(),
            gateTypeLabel.getFont().getStyle(),
            GUI.compItemHeadingFontSize));
    content.add(gateTypeLabel);
    content.add(this.gateType);

    JLabel statusLabel = new JLabel("Status", JLabel.CENTER);
    statusLabel.setFont(
        new Font(
            statusLabel.getFont().getName(),
            statusLabel.getFont().getStyle(),
            GUI.compItemHeadingFontSize));
    content.add(statusLabel);
    content.add(this.status);

    // JLabel hasDHDLabel = new JLabel("Status", JLabel.CENTER);
    // hasDHDLabel.setFont(
    //     new Font(hasDHDLabel.getFont().getName(), hasDHDLabel.getFont().getStyle(),
    // GUI.compItemHeadingFontSize));
    // content.add(hasDHDLabel);
    content.add(this.hasDHD);
    this.hasDHD.setEnabled(false);

    add(content);
    initInfoFieldsData();
  }

  private void initInfoFieldsData() {
    this.name.setText("Unknown");
    this.compAddr.setText("________-____-____-____-____________");
    this.gateType.setText("Unknown");
    this.status.setText("Unknown");
    this.hasDHD.setText("NO DHD");
    // this.hasDHD.setIcon();
  }

  public void setTo(Gate gate) {
    if (gate == null) {
      initInfoFieldsData();
      return;
    }
    LuaTable queryData = (LuaTable) Server.query("info", gate.id()).get("data");
    System.out.println(queryData);
    if (queryData.get(1).equals( LuaString.of("invalid command"))) {
      setTo(null);
      return;
    }

    this.name.setText(gate.name());
    this.compAddr.setText(gate.id());
    this.gateType.setText(((LuaString)queryData.get("gateType")).value);
    this.status.setText(((LuaString)queryData.get("status")).value);
    this.hasDHD.setText(((LuaBool)queryData.get("hasDHD")).get()?"DHD":"NO DHD");
  }
}
