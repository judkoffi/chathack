package fr.upem.chathack.context;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import fr.upem.chathack.ClientChatHack;
import fr.upem.chathack.frame.IPrivateFrame;
import fr.upem.chathack.privateframe.DirectMessage;
import fr.upem.chathack.privateframe.DiscoverMessage;
import fr.upem.chathack.reader.IReader;
import fr.upem.chathack.reader.trame.PrivateConnectionFrameReader;
import fr.upem.chathack.visitor.IPrivateFrameVisitor;

public class PrivateConnectionContext extends BaseContext implements IPrivateFrameVisitor {
  private final ClientChatHack client;
  private final PrivateConnectionFrameReader reader = new PrivateConnectionFrameReader();

  public PrivateConnectionContext(SelectionKey key, ClientChatHack client) {
    super(key);
    this.client = client;
  }

  private void handler(IPrivateFrame frame) {
    frame.accept(this);
  }

  public void doConnect() throws IOException {
    if (!sc.finishConnect()) {
      return;
    }
    var discoverMsg = new DiscoverMessage(client.getLogin(), client.getToken());
    queueMessage(discoverMsg.toBuffer());
    updateInterestOps();
  }

  @Override
  public void processIn() {
    for (;;) {
      IReader.ProcessStatus status = reader.process(bbin);
      switch (status) {
        case DONE:
          var msg = reader.get();
          System.out.println("class: " + msg.getClass());
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

  @Override
  public void visit(DirectMessage directMessage) {
    System.out.println("direct message received");
  }

  @Override
  public void visit(DiscoverMessage message) {
    System.out.println("discover in Client as server context ");
    client.putConnected(message.getLogin(), new PrivateConnectionInfo(this, message.getToken()));
    System.out.println(client.getConnectedMap());
    System.out.println(client.getPendingConnections());
    System.out.println("received message discover " + message);
  }
}
