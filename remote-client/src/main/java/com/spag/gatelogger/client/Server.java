package com.spag.gatelogger.client;

import com.spag.lua.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.stream.Stream;

public abstract sealed class Server permits InnerServer {

  static Socket socket = new Socket();
  private static Scanner incoming;
  private static PrintWriter outgoing;

  public static boolean connect(String host, int port) {
    try {
      socket.connect(new InetSocketAddress(host, port), 10_000);
      if (socket.isConnected()) {
        incoming = new Scanner(socket.getInputStream());
        outgoing = new PrintWriter(socket.getOutputStream());
        return true;
      }
    } catch (IOException e) {
    }
    return false;
  }

  public static LuaTable query(String query, String... params) {
    if (!socket.isConnected()) {
      throw new IllegalStateException("Not connected to server");
    }
    LuaTable packet = new LuaTable();
    packet.put("type", LuaString.of("request"));
    LuaTable data = new LuaTable();
    data.add(LuaString.of(query));
    Stream.of(params).map(LuaString::of).forEach(data::add);
    packet.put("data", data);
    send(packet);
    return receive();
  }

  private static LuaTable receive() {
    if (!socket.isConnected()) {
      throw new IllegalStateException("Not connected to server");
    }
    String output = "";
    while (!LuaTable.bracePat.matcher(output.trim()).matches()) {
      output += incoming.nextLine() + "\n";
    }
    return LuaTable.fromString(output.trim());
  }

  private static void send(LuaTable packet) {
    if (!socket.isConnected()) {
      throw new IllegalStateException("Not connected to server");
    }
    outgoing.print(packet.toString());
  }
}

final class InnerServer extends Server {}