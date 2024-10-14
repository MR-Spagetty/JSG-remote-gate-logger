package com.spag.gatelogger.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import com.spag.gatelogger.lua.LuaTable;

public abstract class Connection extends Thread {
  protected final Socket socket;

  public Connection(Socket connectionSocket) {
    this.socket = connectionSocket;
    try {
      this.incoming = new Scanner(this.socket.getInputStream());
      this.incoming.useDelimiter("\\Z");
      this.outgoing = new PrintWriter(this.socket.getOutputStream());
    } catch (IOException e) {
      throw new IllegalStateException("Socket is not connected");
    }
  }
  protected String readPacket(){
    String output = "";
    while (!LuaTable.bracePat.matcher(output.trim()).matches()){
      output += this.incoming.nextLine() + "\n";
    }
    return output.trim();
  }

  protected final Scanner incoming;
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
