package com.spag.gatelogger.server;

import com.spag.gatelogger.server.data.Gate;
import com.spag.lua.*;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class GateCon extends Connection {

  public static List<GateCon> gateConns = List.of();

  public final Object monitor = new Object();

  public GateCon(Socket connectionSocket) {
    super(connectionSocket);
    gateConns = Stream.concat(gateConns.stream(), Stream.of(this)).toList();
  }

  Gate gate;

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
    String type =
        Optional.ofNullable(packet.get("type"))
            .map(t -> (LuaString) t)
            .map(t -> t.value)
            .orElse(null);
    switch (type) {
      case "init" -> this.gate = Gate.of(packet);
      case "stargate", "modem", "other" -> this.gate.logData(packet);
      case "response" -> {
        synchronized (monitor) {
          if (packet.get("data") == LuaObject.nil) {
            return;
          }
          this.subs.parallelStream()
              .filter(s -> s instanceof GateResponseSubscriber)
              .filter(s -> s.classifier().test(packet))
              .toList()
              .stream()
              .forEach(
                  sr -> {
                    sr.handler().accept(packet);
                    this.subs = this.subs.parallelStream().filter(s -> s != sr).toList();
                  });
          monitor.notify();
        }
      }
      default -> {}
    }
  }

  private List<PacketSubscriber> subs = List.of();

  public void subscribe(PacketSubscriber sub) {
    this.subs = Stream.concat(this.subs.stream(), Stream.of(sub)).toList();
  }

  public static void sendPacket(String id, LuaTable packetData) {
    gateConns.parallelStream()
        .filter(g -> g.gate.id.equals(id))
        .findFirst()
        .orElseThrow(
            () -> new IllegalArgumentException("No known gate by the id: \"%s\"".formatted(id)))
        .sendPacket(packetData);
  }
}
