package com.spag.gatelogger.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public abstract class Connection extends Thread {
  protected final Socket socket;

  public Connection(Socket connectionSocket) {
    this.socket = connectionSocket;
    try {
      this.incoming = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
      this.outgoing = new PrintWriter(this.socket.getOutputStream());
    } catch (IOException e) {
      throw new IllegalStateException("Socket is not connected");
    }
  }

  protected final BufferedReader incoming;
  protected final PrintWriter outgoing;

  protected abstract void startUp();

  protected abstract void shutDown();

  @Override
  public void run() {
    if (socket.isClosed()) {
      return;
    }
    startUp();
    while (socket.isConnected()) {}

    shutDown();
  }
}
