package com.spag.gatelogger.client;

public class App {
  public static void main(String[] args) {
    new GUI();
    Server.connect(args[0], Integer.parseInt(args[1]));
  }
}
