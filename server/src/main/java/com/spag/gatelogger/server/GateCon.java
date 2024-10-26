package com.spag.gatelogger.server;

import com.spag.gatelogger.lua.LuaTable;
import com.spag.gatelogger.server.data.Gate;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.stream.Stream;

public class GateCon extends Connection {

  public static List<GateCon> gateConns = List.of();

  public GateCon(Socket connectionSocket) {
    super(connectionSocket);
    gateConns = Stream.concat(gateConns.stream(), Stream.of(this)).toList();
  }

  private Gate gate;

  @Override
  protected void startUp() {
    this.gate = Gate.of(LuaTable.fromString(readPacket()));
    System.out.println(
        "Gate: %s Connected with name: %s".formatted(this.gate.id, this.gate.name()));
  }

  @Override
  protected void shutDown() {
    try {
      System.out.println(
          "Closing socket for gate: %s with name: %s".formatted(this.gate.id, this.gate.name()));
      this.socket.close();
    } catch (IOException e) {
      System.out.println("unable to close socket");
    }
    gateConns = gateConns.stream().filter(c -> c != this).toList();
  }

  @Override
  protected void doPacket(LuaTable packet) {
    
  }
}
