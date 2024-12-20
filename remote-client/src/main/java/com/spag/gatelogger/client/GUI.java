package com.spag.gatelogger.client;

import com.spag.gatelogger.client.data.Gate;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.WindowConstants;

public class GUI extends JFrame {

  static final int compHeadingFontSize = 20;
  static final int compItemHeadingFontSize = 16;
  public static final Dimension compPaneMin = new Dimension(550, 800);
  public static final Dimension compPaneMax = new Dimension(1000, Integer.MAX_VALUE);
  private Gate selectedGate = null;
  private Timer refreshTimer = new Timer(500, e -> this.refresh());

  DefaultListModel<Gate> knownGates = new DefaultListModel<>();

  public static final GUI GUI = new GUI();

  private GUI() {
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    setMinimumSize(
        new Dimension(
            2 * compPaneMin.width + GateControlPane.compPaneMin.width, compPaneMin.height));
    setMaximumSize(
        new Dimension(
            2 * compPaneMax.width + GateControlPane.compPaneMax.width, compPaneMax.height));
    setContentPane(new Box(BoxLayout.X_AXIS));
    setupKnownGatesList();
    add(GateControlPane.getControlPane());
    setupBasicGateInfo();

    knownGates.addElement(new Gate("a", "Chulak"));
    knownGates.addElement(new Gate("b", "Gallifrey"));
    knownGates.addElement(new Gate("c", "Kronos"));
    knownGates.addElement(new Gate("d", "Raxacoricofallapatorius"));
  }

  private void setupKnownGatesList() {
    JPanel componentPane = new JPanel(new BorderLayout());
    componentPane.setMinimumSize(compPaneMin);
    componentPane.setMaximumSize(compPaneMax);
    componentPane.setPreferredSize(componentPane.getMaximumSize());
    JLabel componentLabel = new JLabel("Gates", JLabel.CENTER);
    componentLabel.setFont(
        new Font(
            componentLabel.getFont().getName(),
            componentLabel.getFont().getStyle(),
            compHeadingFontSize));
    componentPane.add(componentLabel, BorderLayout.NORTH);
    JList<Gate> knownGatesList = new JList<>(this.knownGates);
    knownGatesList.setPreferredSize(componentPane.getMaximumSize());
    knownGatesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    knownGatesList.setAlignmentY(TOP_ALIGNMENT);
    knownGatesList.addListSelectionListener(
        e -> {
          selectedGate = knownGatesList.getSelectedValue();
          if (selectedGate != null) {
            refreshTimer.start();
          } else {
            refreshTimer.stop();
          }
          GateControlPane.getControlPane().setTo(selectedGate);
          refresh();
        });
    componentPane.add(knownGatesList, BorderLayout.CENTER);
    add(componentPane);
  }

  private void setupBasicGateInfo() {
    JPanel componentPane = new JPanel(new BorderLayout());
    componentPane.setMinimumSize(compPaneMin);
    componentPane.setMaximumSize(compPaneMax);
    componentPane.setPreferredSize(componentPane.getMaximumSize());
    JLabel componentLabel = new JLabel("Gate Info", JLabel.CENTER);
    componentLabel.setFont(
        new Font(
            componentLabel.getFont().getName(),
            componentLabel.getFont().getStyle(),
            compHeadingFontSize));
    componentPane.add(componentLabel, BorderLayout.NORTH);
    // componentPane.add(Box.createVerticalStrut(20), BorderLayout.CENTER);
    componentPane.add(GateInfoPane.getInfoPane(), BorderLayout.CENTER);
    add(componentPane);
  }

  private void refresh() {
    GateInfoPane.getInfoPane().setTo(selectedGate);
    GateControlPane.getControlPane().refresh();
  }
}
