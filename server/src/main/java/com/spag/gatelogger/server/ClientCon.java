package com.spag.gatelogger.server;

import com.spag.gatelogger.server.access_control.Permission;
import com.spag.gatelogger.server.access_control.User;
import com.spag.gatelogger.server.data.DataFormatException;
import com.spag.gatelogger.server.data.Gate;
import com.spag.lua.*;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClientCon extends Connection {

  public ClientCon(Socket connectionSocket) {
    super(connectionSocket);
  }

  User user;

  @Override
  protected void startUp() {
    LuaTable connPacket = LuaTable.fromString(readPacket());
    LuaTable packetData = LuaOptional.ofNilable(connPacket.get(LuaString.of("data"))).map(data -> (LuaTable) data)
        .orElseThrow();
    try {
      this.user = User.get((LuaString) packetData.get(LuaString.of("username")),
          packetData.get(LuaString.of("password")));
    } catch (IllegalAccessError IAEr) {
      sendPacket(LuaTable.fromString("{type=\"access denied\", data={\"Invalid Credentials\"}}"));
      shutDown();
      return;
    } catch (IllegalAccessException IAEx) {
      sendPacket(LuaTable.fromString("{type=\"access denied\", data={\"Invalid Credentials\"}}"));
      shutDown();
      return;
    }
    LuaTable welcome = new LuaTable();
    welcome.put(LuaString.of("type"), LuaString.of("welcome"));
    LuaTable welcomeData = new LuaTable();
    welcomeData.insert(LuaString.of("Hello %s"));
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
    String type = Optional.of(packet.get(LuaString.of("type")))
        .filter(t -> t != LuaObject.nil)
        .map(t -> (LuaString) t)
        .map(t -> t.value)
        .orElse("none");
    switch (type) {
      case "request" -> handleRequest(packet);
      case "response" -> {
      }
      default -> {
      }
    }
  }

  private void handleRequest(LuaTable requestPacket) {
    LuaTable responsePacket = new LuaTable();
    responsePacket.put(LuaString.of("to"), requestPacket);
    List<String> command = LuaOptional.ofNilable(requestPacket.get(LuaString.of("data")))
        .map(c -> (LuaTable) c)
        .orElseThrow(() -> new DataFormatException("request data not found"))
        .stream()
        .map(p -> p instanceof LuaString ? ((LuaString) p).value : p.toString())
        .toList();
    if (command.isEmpty()) {
      responsePacket.put(LuaString.of("data"), invalidCommand("No request given"));
    } else {
      responsePacket.put(
          LuaString.of("data"),
          switch (command.get(0)) {
            case "list" ->
              GateCon.gateConns.stream()
                  .map(c -> c.gate)
                  .filter(gate -> Permission.has(Permission.CAN_VIEW, this.user.getPerm(gate.id)))
                  .reduce(
                      new LuaTable(),
                      (t, g) -> {
                        t.put(LuaString.of(g.id), LuaString.of(g.name()));
                        return t;
                      },
                      (a, b) -> a.merge(b));
            default ->
              gateRequest(
                  command.get(0),
                  command.subList(1, command.size()).stream().toArray(String[]::new));
          });
    }
    responsePacket.insert(LuaString.of("" + System.currentTimeMillis() / 1000));
    sendPacket(responsePacket);
  }

  private static final UnsupportedOperationException COMMAND_ROOT_ERROR = new UnsupportedOperationException(
      "invalid command");

  private LuaTable invalidCommand(String reason, String... extra) {
    LuaTable out = new LuaTable();
    out.insert(LuaString.of("invalid command"));
    out.put(LuaString.of("reason"), LuaString.of(reason));
    if (extra.length > 0) {
      LuaTable extraData = new LuaTable();
      Stream.of(extra).forEach(d -> extraData.insert(LuaString.of(d)));
      out.put(LuaString.of("info"), extraData);
    }
    return out;
  }

  private LuaTable gateRequest(String command, String... params) {
    final Map<String, Integer> permsCache = new HashMap<>();
    try {
      LuaTable out = new LuaTable();
      if (params.length < 1) {
        throw new UnsupportedOperationException("No gate specified", COMMAND_ROOT_ERROR);
      }
      GateCon selected = GateCon.gateConns.stream()
          .filter(g -> g.gate.id.substring(0, params[0].length()).equals(params[0]))
          .filter(g -> Permission.has(Permission.CAN_VIEW,
              permsCache.computeIfAbsent(g.gate.id, id -> this.user.getPerm(id))))
          .reduce(
              (a, b) -> {
                throw new UnsupportedOperationException(
                    """
                        Shortened id \"%s\" is ambiguous between at least:
                          \"%s\" (%s)
                          \"%s\" (%s)"""
                        .formatted(
                            params[0], a.gate.id, a.gate.name(), b.gate.id, b.gate.name()),
                    COMMAND_ROOT_ERROR);
              })
          .orElseThrow(
              () -> new UnsupportedOperationException(
                  "No gate found with id matching \"%s\"".formatted(params[0]),
                  COMMAND_ROOT_ERROR));

      int nParams = params.length - 1;
      assert nParams >= 0;
      switch (command) {
        case "update" -> {
          LuaTable packet = new LuaTable();
          packet.insert(LuaString.of("update"));
          packet.put(LuaString.of("reboot"), LuaBool.True);
          selected.sendPacket(packet);
        }
        case "info" -> {
          LuaTable packet = new LuaTable();
          synchronized (selected.monitor) {
            if (nParams == 0) {
              selected.subscribe(
                  new GateResponseSubscriber(
                      "status",
                      d -> {
                        if (!Permission.has(Permission.IRIS_VIEW, permsCache.get(selected.gate.id))) {
                          d.put(LuaString.of("irisType"), LuaObject.nil);
                        }
                        out.merge((LuaTable) d.get(LuaString.of("data")));
                      }));
              packet.insert(LuaString.of("status"));
            } else if (nParams == 1) {
              switch (params[1]) {
                case "address" -> {
                  if (!Permission.has(Permission.ADDRESS_VIEW, permsCache.get(selected.gate.id))) {
                    return invalidCommand(
                        "Disallowed request \"info %s\"".formatted(Stream.of(params).collect(Collectors.joining(" "))),
                        "You do not have permission to see the address of this gate");
                  }
                  selected.subscribe(
                      new GateResponseSubscriber(
                          "address",
                          d -> {
                            LuaTable addresses = (LuaTable) d.get(LuaString.of("address"));
                            selected.gate.updateAddresses(addresses);
                            out.merge(addresses);
                          }));
                  packet.insert(LuaString.of("address"));
                }
                case "dialed" -> {
                  selected.subscribe(
                      new GateResponseSubscriber(
                          "dialed",
                          d -> {
                            selected.gate.dialedAddress = Gate.addressOf(selected.gate.type(),
                                d.get(LuaString.of("address")));
                            // TODO decide if this should have its own permission
                          }));
                  packet.insert(LuaString.of("dialed"));
                }
                default -> {
                  return invalidCommand(
                      "Unknown request \"info %s\""
                          .formatted(Stream.of(params).collect(Collectors.joining(" "))));
                }
              }
            } else {
              return invalidCommand(
                  "Unknown request \"info %s\""
                      .formatted(Stream.of(params).collect(Collectors.joining(" "))));
            }
            selected.sendPacket(packet);
            selected.monitor.wait();
          }
          System.out.println("Resposne recieved");
          out.put(LuaNum.of(1), LuaString.of(""));
        }
        case "close" -> {
          sendPacket(LuaTable.fromString("{\"close\"}"));
        }
        default -> invalidCommand("Unknown request \"%s\"".formatted(command));
      }

      return out;
    } catch (UnsupportedOperationException e) {
      if (e.getCause() == COMMAND_ROOT_ERROR) {
        return invalidCommand(e.getMessage());
      }
      throw e;
    } catch (InterruptedException e) {
      LuaTable out = new LuaTable();
      out.insert(LuaString.of("response await interrupted"));
      return out;
    }
  }
}
