package com.spag.gatelogger.server;

import com.spag.gatelogger.lua.*;
import com.spag.gatelogger.server.data.DataFormatException;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Optional;

public class ClientCon extends Connection {

  public ClientCon(Socket connectionSocket) {
    super(connectionSocket);
  }

  @Override
  protected void startUp() {
    LuaTable connPacket = LuaTable.fromString(readPacket());
    LuaTable welcome = new LuaTable();
    welcome.put("type", LuaString.of("welcome"));

  }

  @Override
  protected void shutDown() {
    try {
      System.out.println(
          "Closing socket for User at: %s".formatted(this.socket.getRemoteSocketAddress()));
      this.socket.close();
    } catch (IOException e) {
      System.out.println("unable to close socket");
    }
  }

  @Override
  protected void doPacket(LuaTable packet) {
    String type =
        Optional.ofNullable(packet.get("type"))
            .map(t -> (LuaString) t)
            .map(t -> t.value)
            .orElse(null);
    switch (type) {
      case "request" -> handleRequest(packet);
      case "response" -> {}
      default -> {}
    }
  }

  private void handleRequest(LuaTable requestPacket) {
    LuaTable responsePacket = new LuaTable();
    responsePacket.put("to", requestPacket);
    List<String> command =
        Optional.ofNullable(requestPacket.get("data"))
            .map(c -> (LuaTable) c)
            .orElseThrow(() -> new DataFormatException("request data not found"))
            .stream()
            .map(p -> (LuaString) p)
            .map(p -> p.value)
            .toList();

    responsePacket.put(
        "data",
        switch (command.get(0)) {
          case "update" -> {
            LuaTable t = new LuaTable();
            yield t;
          }
          case "close" -> {
            LuaTable t = new LuaTable();
            yield t;
          }
          case "list" ->
              GateCon.gateConns.stream()
                  .map(c -> c.gate)
                  .reduce(
                      new LuaTable(),
                      (t, g) -> {
                        t.put(g.id, LuaString.of(g.name()));
                        return t;
                      },
                      LuaTable::merge);
          default -> null;
        });
    responsePacket.add(LuaString.of("" + System.currentTimeMillis() / 1000));
    sendPacket(responsePacket);
  }
}
