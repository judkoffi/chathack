package fr.upem.chathack.context;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import fr.upem.chathack.ServerChatHack;
import fr.upem.chathack.common.reader.IReader;
import fr.upem.chathack.common.reader.trame.ServerFrameReader;
import fr.upem.chathack.frame.AnonymousConnection;
import fr.upem.chathack.frame.AuthentificatedConnection;
import fr.upem.chathack.frame.BroadcastMessage;
import fr.upem.chathack.frame.DirectMessage;
import fr.upem.chathack.frame.IFrame;
import fr.upem.chathack.frame.IFrameVisitor;
import fr.upem.chathack.frame.RequestPrivateConnection;
import fr.upem.chathack.frame.ServerResponseMessage;

public class ServerContext extends BaseContext implements IFrameVisitor {
  private final ServerFrameReader reader = new ServerFrameReader();
  private final ServerChatHack server;

  public ServerContext(SelectionKey key, ServerChatHack server) {
    super(key);
    this.server = server;
  }

  private void handler(IFrame frame) {
    frame.accept(this);
  }

  @Override
  public void processIn() {
    for (;;) {
      IReader.ProcessStatus status = reader.process(bbin);
      switch (status) {
        case DONE:
          IFrame frame = reader.get();
          handler(frame);
          reader.reset();
          break;
        case REFILL:
          return;
        case ERROR:
          silentlyClose();
          return;
      }
    }
  }

  @Override
  public void processOut() {
    while (!queue.isEmpty()) {
      var bb = queue.peek();
      if (bbout.remaining() < bb.remaining())
        return;

      queue.remove();
      bbout.put(bb);
    }
  }

  @Override
  public void visit(BroadcastMessage message) {
    if (server.isConnected(message.getFromLogin())) {
      server.broadcast(message.toBuffer());
    } else {
      var msg = new ServerResponseMessage("Not connected", true).toBuffer();
      queueMessage(msg);
      silenceInuputClose();
    }
  }

  @Override
  public void visit(DirectMessage directMessage) {
    // TODO Auto-generated method stub
  }


  @Override
  public void visit(AnonymousConnection message) {
    if (!server.isAvailableLogin(message.getLogin())) {
      var msg = new ServerResponseMessage("Login not available", true).toBuffer();
      queueMessage(msg);
      silenceInuputClose();
      return;
    }
    server.registerAnonymousClient(message.getLogin(), key);
  }


  @Override
  public void visit(AuthentificatedConnection message) {
    if (!server.isAvailableLogin(message.getLogin().getValue())) {
      var msg = new ServerResponseMessage("Login not available", true).toBuffer();
      queueMessage(msg);
      silenceInuputClose();
      return;
    }
    server.registerAuthenticatedClient(message, key);
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  private void silenceInuputClose() {
    try {
      sc.shutdownInput();
    } catch (IOException e) {
      //
    }
  }

  @Override
  public void visit(ServerResponseMessage serverMessage) {

  }

  @Override
  public void visit(RequestPrivateConnection requestMessage) {
    // TODO

  }

}
