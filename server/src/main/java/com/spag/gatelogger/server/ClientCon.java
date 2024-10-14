package com.spag.gatelogger.server;

import java.net.Socket;

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

}
