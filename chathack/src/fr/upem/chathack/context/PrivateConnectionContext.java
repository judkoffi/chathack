package fr.upem.chathack.context;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import fr.upem.chathack.ClientChatHack;
import fr.upem.chathack.common.reader.IReader;
import fr.upem.chathack.common.reader.trame.ClientAsServerFrameReader;
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

public class PrivateConnectionContext extends BaseContext implements IFrameVisitor {
  private final ClientChatHack client;
  private final ClientAsServerFrameReader reader = new ClientAsServerFrameReader();

  public PrivateConnectionContext(SelectionKey key, ClientChatHack client) {
    super(key);
    this.client = client;
  }

  private void handler(IFrame frame) {
    frame.accept(this);
  }

  public void doConnect() throws IOException {
    if (!sc.finishConnect()) {
      return;
    }
   // System.out.println("send discover with key 39");
    var discoverMsg = new DiscoverMessage(client.getLogin(), 39);
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
  public void visit(AnonymousConnection message) {}

  @Override
  public void visit(AuthentificatedConnection message) {}

  @Override
  public void visit(BroadcastMessage message) {}

  @Override
  public void visit(ServerResponseMessage serverMessage) {}

  @Override
  public void visit(RequestPrivateConnection requestMessage) {}

  @Override
  public void visit(AcceptPrivateConnection responsePrivateConnection) {}

  @Override
  public void visit(RejectPrivateConnection rejectPrivateConnection) {}

  @Override
  public void visit(DiscoverMessage message) {
    System.out.println("discover in Client as server context ");
    System.out.println(message);
    client.putAlreadyConnection(message.getLogin(), new PrivateConnectionInfo(this));
    System.out.println("received message discover");
  }

}
