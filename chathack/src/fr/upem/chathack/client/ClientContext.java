package fr.upem.chathack.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import fr.upem.chathack.context.BaseContext;
import fr.upem.chathack.frame.IPublicFrame;
import fr.upem.chathack.publicframe.AcceptPrivateConnection;
import fr.upem.chathack.publicframe.BroadcastMessage;
import fr.upem.chathack.publicframe.LogOutMessage;
import fr.upem.chathack.publicframe.RejectPrivateConnection;
import fr.upem.chathack.publicframe.RequestPrivateConnection;
import fr.upem.chathack.publicframe.ServerResponseMessage;
import fr.upem.chathack.reader.IReader;
import fr.upem.chathack.reader.trame.ClientFrameReader;
import fr.upem.chathack.visitor.IPublicFrameVisitor;

/**
 * Class use to represent context between a client and server
 */
public class ClientContext extends BaseContext implements IPublicFrameVisitor {
  private final ClientFrameReader reader = new ClientFrameReader();
  private final ClientChatHack client;

  public ClientContext(SelectionKey key, ClientChatHack client) {
    super(key);
    this.client = client;
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
          var msg = reader.get();
          handler(msg);
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

  /**
   * 
   * @return true if socket channel is closed
   */
  boolean isClosed() {
    return closed;
  }

  /**
   * Method use to do connection when selector notify with OP_CONNECT
   * 
   * @throws IOException
   */
  public void doConnect() throws IOException {
    if (!sc.finishConnect()) {
      return;
    }
    updateInterestOps();
  }


  @Override
  public void visit(BroadcastMessage message) {
    System.out.println(message);
  }

  @Override
  public void visit(ServerResponseMessage serverMessage) {
    if (serverMessage.isErrorMessage()) {
      System.err.println(serverMessage);
      var splited = serverMessage.getValue().split(":");
      if (splited.length >= 2) {
        var login = splited[1];
        client.privateConnectionMap.remove(login);
        client.pendingPrivateRequests
          .stream()
          .filter(req -> req.getReceiver().getValue().equals(login))
          .findFirst()
          .ifPresent(client.pendingPrivateRequests::remove);
      }
    } else {
      System.out.println(serverMessage);
    }
  }

  /**
   * Put in a queue without run processOut(), use to send authentication message at client launch
   * 
   * @param bb: buffer to be send
   */
  public void putInQueue(ByteBuffer bb) {
    queue.add(bb);
  }

  @Override
  public void visit(RequestPrivateConnection requestMessage) {
    if (client.pendingPrivateRequests.contains(requestMessage))
      return;
    System.out.println(requestMessage);
    client.pendingPrivateRequests.add(requestMessage);
  }

  @Override
  public void visit(AcceptPrivateConnection response) {
    client.doConnectionWithClient(response);
  }

  @Override
  public void visit(RejectPrivateConnection response) {
    System.out.println(response);
    client.privateConnectionMap.remove(response.getReceiver());
  }

  @Override
  public void visit(LogOutMessage disconnectionMessage) {
    var login = disconnectionMessage.getLogin();
    client.privateConnectionMap.remove(login);
    client.pendingPrivateRequests
      .stream()
      .filter(req -> req.getReceiver().getValue().equals(login))
      .findFirst()
      .ifPresent(client.pendingPrivateRequests::remove);
  }
}
