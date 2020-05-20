package fr.upem.chathack.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import fr.upem.chathack.context.BaseContext;
import fr.upem.chathack.frame.IPublicFrame;
import fr.upem.chathack.publicframe.AcceptPrivateConnection;
import fr.upem.chathack.publicframe.AnonymousConnection;
import fr.upem.chathack.publicframe.AuthentificatedConnection;
import fr.upem.chathack.publicframe.BroadcastMessage;
import fr.upem.chathack.publicframe.RejectPrivateConnection;
import fr.upem.chathack.publicframe.RequestPrivateConnection;
import fr.upem.chathack.publicframe.ServerResponseMessage;
import fr.upem.chathack.reader.IReader;
import fr.upem.chathack.reader.trame.ServerFrameReader;
import fr.upem.chathack.visitor.IPublicFrameVisitor;

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
  public void visit(BroadcastMessage message) {
    if (server.isConnected(message.getFromLogin())) {
      // var bb = message.toBuffer().asReadOnlyBuffer();
      // donner une vue (sans possibiliter de write sur le buffer)
      server.broadcast(message.toBuffer());
    } else {
      var msg = new ServerResponseMessage("Not connected", true).toBuffer();
      queueMessage(msg);
      silenceInputClose();
    }
  }

  @Override
  public void visit(AnonymousConnection message) {
    if (server.isExistLogin(message.getLogin())) {
      var msg = new ServerResponseMessage("Login not available", true).toBuffer();
      queueMessage(msg);
      silenceInputClose();
      return;
    }
    server.registerAnonymousClient(message.getLogin(), key);
  }

  @Override
  public void visit(AuthentificatedConnection message) {
    if (server.isExistLogin(message.getLogin().getValue())) {
      var msg = new ServerResponseMessage("Login not available", true).toBuffer();
      queueMessage(msg);
      silenceInputClose();
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

  private void silenceInputClose() {
    try {
      server.removeClientByKey(key);
      sc.shutdownInput();
    } catch (IOException e) {
      //
    }
  }

  @Override
  public void silentlyClose() {
    try {
      server.removeClientByKey(key);
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
      silenceInputClose();
      return;
    }

    System.out.println(requestMessage);
    var target = requestMessage.getReceiver().getValue();
    if (!server.isExistLogin(target)) {
      var msg = new ServerResponseMessage("Unknown login", true);
      System.out.println(msg);
      queueMessage(msg.toBuffer());
      silenceInputClose();
      return;
    }

    System.out.println(requestMessage);
    server.sendPrivateConnectionRequest(requestMessage);
  }

  @Override
  public void visit(AcceptPrivateConnection responsePrivateConnection) {
    System.out.println(responsePrivateConnection);
    var targetKey = server.findKeyByLogin(responsePrivateConnection.getAppliant());
    server.sendMessageToClient(responsePrivateConnection.toBuffer(), targetKey);
  }

  @Override
  public void visit(RejectPrivateConnection rejectPrivateConnection) {
    System.out.println(rejectPrivateConnection);
    var targetKey = server.findKeyByLogin(rejectPrivateConnection.getFromLogin());
    server.sendMessageToClient(rejectPrivateConnection.toBuffer(), targetKey);
  }


  /*****************************
   * Not received by server
   *****************************/
  @Override
  public void visit(ServerResponseMessage serverMessage) {}

}
