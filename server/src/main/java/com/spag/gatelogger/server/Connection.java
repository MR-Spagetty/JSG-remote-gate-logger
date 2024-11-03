package com.spag.gatelogger.server;

import com.spag.lua.LuaTable;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

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

  protected final String readPacket() {
    String output = "";
    while (!LuaTable.bracePat.matcher(output.trim()).matches()) {
      output += this.incoming.nextLine() + "\n";
    }
    return output.trim();
  }

  protected final Scanner incoming;
  protected final PrintWriter outgoing;

  public final void sendPacket(LuaTable packetData) {
    this.outgoing.println(packetData.toString());
    this.outgoing.flush();
  }

  protected abstract void startUp();

  protected abstract void shutDown();

  protected abstract void doPacket(LuaTable packet);

  private final void doPacket(String packet) {
    doPacket(LuaTable.fromString(packet));
  }

  @Override
  public final void run() {
    if (socket.isClosed()) {
      return;
    }
    startUp();
    while (socket.isConnected()) {
      try {
        doPacket(readPacket());
      } catch (NoSuchElementException e) {
      }
    }

    shutDown();
  }
}
