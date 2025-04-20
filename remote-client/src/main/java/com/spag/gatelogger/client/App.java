package com.spag.gatelogger.client;

public class App {
  public static void main(String[] args) {
    if (args.length < 2) {
      System.out.println("Usage: remote-client <serverAddress> <serverPort>");
      System.exit(1);
    } else if (args.length > 2) {
      System.out.println(
          "$d arguments given, only 2 expected\nextra arguments given will be ignored"
              .formatted(args.length));
    }
    GUI.GUI.setVisible(true);
    System.out.println(Server.connect(args[0], Integer.parseInt(args[1])));
  }
}
