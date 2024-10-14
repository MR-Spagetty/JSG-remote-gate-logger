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
    try {
      StringBuilder message = new StringBuilder();
      while (this.incoming.ready()) {
        message.append(this.incoming.readLine() + "\n");
      }
      this.gate = Gate.of(LuaTable.fromString(message.toString().trim()));
      System.out.println("Gate: "+this.gate.id+ " Connected with name: "+this.gate.name());
    } catch (IOException e) {
    }
  }

  @Override
  protected void shutDown() {
    try {
      this.socket.close();
    } catch (IOException e) {
      System.out.println("unable to close socket");
    }
    gateConns = gateConns.stream().filter(c -> c != this).toList();
  }
}
