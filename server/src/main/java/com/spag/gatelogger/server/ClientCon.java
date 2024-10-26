package com.spag.gatelogger.server;

import java.net.Socket;

import com.spag.gatelogger.lua.LuaTable;

public class ClientCon extends Connection {

  public ClientCon(Socket connectionSocket) {
    super(connectionSocket);
  }

  @Override
  protected void startUp() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'startUp'");
  }

  @Override
  protected void shutDown() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'shutDown'");
  }

  @Override
  protected void doPacket(LuaTable packet) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'doPacket'");
  }

}
