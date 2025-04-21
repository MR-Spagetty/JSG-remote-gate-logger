package com.spag.gatelogger.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;

/** Hello world! */
public class App {
  static int clientPort = 3002;
  static int gatePort = 3001;
  public static final Path dataPath = Path.of("");

  public static void main(String[] args) {
    Thread clientCons = new Thread(App::listenForClient);
    Thread gateCons = new Thread(App::listenForGate);
    clientCons.start();
    gateCons.start();
  }

  public static void listenForClient() {
    System.out.println("Listening for clients... ");
    try (ServerSocket possClient = new ServerSocket(clientPort); ) {
      while (true) {
        Socket client = possClient.accept();
        System.out.println("Got client at " + client.getInetAddress().getCanonicalHostName());
        new ClientCon(client).start();
      }
    } catch (IOException e) {
    }
  }

  public static void listenForGate() {
    System.out.println("Listening for gates... ");
    try (ServerSocket possGate = new ServerSocket(gatePort); ) {
      while (true) {
        Socket gate = possGate.accept();
        System.out.println("Got gate at " + gate.getInetAddress().getCanonicalHostName());
        new GateCon(gate).start();
      }
    } catch (IOException e) {
    }
  }
}
