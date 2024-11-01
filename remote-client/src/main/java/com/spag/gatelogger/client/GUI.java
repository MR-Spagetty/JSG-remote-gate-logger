package com.spag.gatelogger.client;

import com.spag.gatelogger.client.data.Gate;

import javax.swing.*;

public class GUI extends JFrame {
  DefaultListModel<Gate> knownGates = new DefaultListModel<>();

  public GUI() {
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setSize(400, 500);
    setContentPane(new Box(BoxLayout.X_AXIS));
    JList<Gate> knownGatesList = new JList<>(this.knownGates);
    knownGatesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    add(knownGatesList);

    add(new JButton("2"));
    add(new JButton("three"));
    setVisible(true);
  }
}
