package fr.upem.chathack.context;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import fr.upem.chathack.ClientChatHack;
import fr.upem.chathack.common.reader.IReader;
import fr.upem.chathack.common.reader.trame.ClientFrameReader;
import fr.upem.chathack.frame.AcceptPrivateConnection;
import fr.upem.chathack.frame.AnonymousConnection;
import fr.upem.chathack.frame.AuthentificatedConnection;
import fr.upem.chathack.frame.BroadcastMessage;
import fr.upem.chathack.frame.DirectMessage;
import fr.upem.chathack.frame.DiscoverMessage;
import fr.upem.chathack.frame.IFrame;
import fr.upem.chathack.frame.IFrameVisitor;
import fr.upem.chathack.frame.RejectPrivateConnection;
import fr.upem.chathack.frame.RequestPrivateConnection;
import fr.upem.chathack.frame.ServerResponseMessage;

public class ClientContext extends BaseContext implements IFrameVisitor {
  private final ClientFrameReader reader = new ClientFrameReader();
  private final ClientChatHack client;

  public ClientContext(SelectionKey key, ClientChatHack client) {
    super(key);
    this.client = client;
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

  public void doConnect() throws IOException {
    if (!sc.finishConnect()) {
      return;
    }

    System.out.println("clinent login " + client.getLogin());
    updateInterestOps();
  }


  @Override
  public void visit(BroadcastMessage message) {
    System.out.println(message);
  }

  @Override
  public void visit(DirectMessage directMessage) {}

  @Override
  public void visit(ServerResponseMessage serverMessage) {
    System.out.println(serverMessage);
    if (serverMessage.isErrorMessage()) {
      this.client.interruptConsole();
      System.exit(-1);
    }
  }

  /*
   * Put in a queue without run processOut(), use to send authentication message at client launch
   */
  public void putInQueue(ByteBuffer bb) {
    queue.add(bb);
  }

  @Override
  public void visit(RequestPrivateConnection requestMessage) {
    client.addPrivateConnectionRequest(requestMessage);
  }

  @Override
  public void visit(AcceptPrivateConnection responsePrivateConnection) {
    var targetAddr = responsePrivateConnection.getTargetAddress();
    client.doConnectionWithClient(targetAddr, responsePrivateConnection.getTargetLogin());
    System.out.println("private connection is accepted !");
  }

  @Override
  public void visit(RejectPrivateConnection rejectPrivateConnection) {
    System.out.println("private connection was rejected.");
  }

  @Override
  public void visit(DiscoverMessage message) {
    System.out.println("discover in Client context key hash: " + message.getKeyHashCode());
    // client.registerPrivateClient(message.getLogin(), message.getKeyHashCode());
  }

  @Override
  public void visit(AnonymousConnection message) {}

  @Override
  public void visit(AuthentificatedConnection message) {}

}
