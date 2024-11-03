package com.spag.gatelogger.client;

import com.spag.gatelogger.client.data.Gate;
import java.awt.BorderLayout;
import java.awt.Font;
import javax.swing.*;

public class GUI extends JFrame {
  DefaultListModel<Gate> knownGates = new DefaultListModel<>();

  public GUI() {
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setSize(400, 500);
    setContentPane(new Box(BoxLayout.X_AXIS));
    JList<Gate> knownGatesList = new JList<>(this.knownGates);
    knownGatesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    knownGatesList.setAlignmentY(TOP_ALIGNMENT);
    {
      JPanel gateList = new JPanel(new BorderLayout());
      JLabel gatesLabel = new JLabel("Gates", JLabel.CENTER);
      gatesLabel.setFont(
          new Font(gatesLabel.getFont().getName(), gatesLabel.getFont().getStyle(), 20));
      gateList.add(gatesLabel, BorderLayout.NORTH);
      gateList.getComponent(0);
      gateList.add(knownGatesList, BorderLayout.CENTER);
      add(gateList);
    }
    knownGates.addElement(new Gate("a", "Chulak"));
    knownGates.addElement(new Gate("b", "Gallifrey"));
    knownGates.addElement(new Gate("c", "Kronos"));

    add(new JButton("2"));
    add(new JButton("three"));
    setVisible(true);
  }
}
