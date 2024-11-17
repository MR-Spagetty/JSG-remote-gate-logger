package com.spag.gatelogger.client;

public class App {
  public static void main(String[] args) {
    GUI.GUI.setVisible(true);
    System.out.println(Server.connect(args[0], Integer.parseInt(args[1])));
  }
}
