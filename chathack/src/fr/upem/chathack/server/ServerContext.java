package fr.upem.chathack.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import fr.upem.chathack.context.BaseContext;
import fr.upem.chathack.frame.IPublicFrame;
import fr.upem.chathack.publicframe.AcceptPrivateConnection;
import fr.upem.chathack.publicframe.AnonymousConnection;
import fr.upem.chathack.publicframe.AuthentificatedConnection;
import fr.upem.chathack.publicframe.BroadcastMessage;
import fr.upem.chathack.publicframe.LogOutMessage;
import fr.upem.chathack.publicframe.RejectPrivateConnection;
import fr.upem.chathack.publicframe.RequestPrivateConnection;
import fr.upem.chathack.publicframe.ServerResponseMessage;
import fr.upem.chathack.reader.IReader;
import fr.upem.chathack.reader.trame.ServerFrameReader;
import fr.upem.chathack.visitor.IPublicFrameVisitor;

/**
 * Class use to represent context between a server and client
 */
public class ServerContext extends BaseContext implements IPublicFrameVisitor {
  private final ServerFrameReader reader = new ServerFrameReader();
  private final ServerChatHack server;

  public ServerContext(SelectionKey key, ServerChatHack server) {
    super(key);
    this.server = server;
  }

  private void handler(IPublicFrame frame) {
    frame.accept(this);
  }

  @Override
  public void processIn() {
    for (;;) {
      IReader.ProcessStatus status = reader.process(bbin);
      switch (status) {
        case DONE:
          IPublicFrame frame = reader.get();
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
      bb.flip();
    }
  }

  @Override
  public void visit(BroadcastMessage message) {
    if (server.isConnected(message.getFromLogin())) {
      var bb = message.toBuffer().asReadOnlyBuffer();
      server.broadcast(bb);
    } else {
      var msg = new ServerResponseMessage("Not connected", true).toBuffer();
      queueMessage(msg);
      silentlyfflushClose();
    }
  }

  @Override
  public void visit(AnonymousConnection message) {
    if (server.isExistLogin(message.getLogin())) {
      var msg = new ServerResponseMessage("Login not available", true).toBuffer();
      queueMessage(msg);
      silentlyfflushClose();
      return;
    }
    server.registerAnonymousClient(message.getLogin(), key);
  }

  @Override
  public void visit(AuthentificatedConnection message) {
    if (server.isExistLogin(message.getLogin().getValue())) {
      var msg = new ServerResponseMessage("Login not available", true).toBuffer();
      queueMessage(msg);
      silentlyfflushClose();
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

  @Override
  public void silentlyClose() {
    try {
      sc.close();
    } catch (IOException e) {
      // ignore exception
    }
  }

  /**
   * Perform write of content output buffer and close connection
   */
  public void silentlyfflushClose() {
    try {
      doWrite();// force writing to flush bbout
      sc.close();
    } catch (IOException e) {
      // ignore exception
    }
  }


  @Override
  public void visit(RequestPrivateConnection requestMessage) {
    if (!server.isConnected(requestMessage.getAppliant().getValue())) {
      var msg = new ServerResponseMessage("You must be authentificated", true).toBuffer();
      queueMessage(msg);
      silentlyfflushClose();
      return;
    }

    var target = requestMessage.getReceiver().getValue();
    if (!server.isExistLogin(target)) {
      var msg = new ServerResponseMessage("Unknown login: " + target, true);
      System.out.println(msg);
      queueMessage(msg.toBuffer());
      return;
    }

    System.out.println(requestMessage);
    server.sendPrivateConnectionRequest(requestMessage);
  }

  @Override
  public void visit(AcceptPrivateConnection responsePrivateConnection) {
    System.out.println(responsePrivateConnection);
    var login = responsePrivateConnection.getAppliant();
    var targetKey = server.map.get(login).key;
    server.sendMessageToClient(responsePrivateConnection.toBuffer(), targetKey);
  }

  @Override
  public void visit(RejectPrivateConnection rejectPrivateConnection) {
    var login = rejectPrivateConnection.getAppliant();
    var targetKey = server.map.get(login).key;
    server.sendMessageToClient(rejectPrivateConnection.toBuffer(), targetKey);
  }

  @Override
  public void visit(LogOutMessage disconnectionMessage) {
    var from = disconnectionMessage.getLogin();
    if (!server.map.containsKey(from)) {
      silentlyClose();
      return;
    }
    silentlyClose();
    server.map.remove(from);
  }
}
