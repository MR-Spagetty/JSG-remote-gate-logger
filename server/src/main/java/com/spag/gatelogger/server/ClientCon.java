package com.spag.gatelogger.server;

import com.spag.gatelogger.server.data.DataFormatException;
import com.spag.gatelogger.server.data.Gate;
import com.spag.lua.*;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ClientCon extends Connection {

  public ClientCon(Socket connectionSocket) {
    super(connectionSocket);
  }

  @Override
  protected void startUp() {
    LuaTable connPacket = LuaTable.fromString(readPacket());
    System.out.println(connPacket.get(LuaString.of("data")));
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
    List<String> command = Optional.of(requestPacket.get(LuaString.of("data")))
        .filter(p -> p != LuaObject.nil)
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
    try {
      LuaTable out = new LuaTable();
      if (params.length < 1) {
        throw new UnsupportedOperationException("No gate specified", COMMAND_ROOT_ERROR);
      }
      GateCon selected = GateCon.gateConns.stream()
          .filter(g -> g.gate.id.substring(0, params[0].length()).equals(params[0]))
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
                        out.merge((LuaTable) d.get(LuaString.of("data")));
                      }));
              packet.insert(LuaString.of("status"));
            } else if (nParams == 1) {
              switch (params[1]) {
                case "address" -> {
                  selected.subscribe(
                      new GateResponseSubscriber(
                          "address",
                          d -> {
                            selected.gate.updateAddresses((LuaTable) d.get(LuaString.of("address")));
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
