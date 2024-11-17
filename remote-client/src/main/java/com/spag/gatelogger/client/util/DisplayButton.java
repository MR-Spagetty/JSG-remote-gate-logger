package com.spag.gatelogger.client.util;

import java.awt.Color;

import javax.swing.JButton;
import javax.swing.plaf.metal.MetalButtonUI;

public class DisplayButton extends JButton{
  public DisplayButton(){
    JButton s = this;
    setUI(new MetalButtonUI(){
      @Override
      protected Color getDisabledTextColor() {
        return s.getForeground();
      }
    });
    setEnabled(false);
  }
}
