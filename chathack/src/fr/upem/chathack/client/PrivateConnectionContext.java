package fr.upem.chathack.client;

import static fr.upem.chathack.model.PrivateConnectionInfo.PrivateConnectionState.SUCCEED;
import java.io.IOException;
import java.nio.channels.SelectionKey;
import fr.upem.chathack.context.BaseContext;
import fr.upem.chathack.frame.IPrivateFrame;
import fr.upem.chathack.privateframe.ConfirmDiscoverMessage;
import fr.upem.chathack.privateframe.DirectMessage;
import fr.upem.chathack.privateframe.DiscoverMessage;
import fr.upem.chathack.reader.IReader;
import fr.upem.chathack.reader.trame.PrivateConnectionFrameReader;
import fr.upem.chathack.visitor.IPrivateFrameVisitor;

public class PrivateConnectionContext extends BaseContext implements IPrivateFrameVisitor {
  private final ClientChatHack client;
  private final PrivateConnectionFrameReader reader = new PrivateConnectionFrameReader();
  private long token;

  // Destinator in private connection
  private String receiver;

  public PrivateConnectionContext(SelectionKey key, ClientChatHack client) {
    super(key);
    this.client = client;
  }

  public PrivateConnectionContext(SelectionKey key, ClientChatHack client, long token) {
    super(key);
    this.client = client;
    this.token = token;
  }

  public PrivateConnectionContext(SelectionKey key, ClientChatHack client, long token,
      String receiver) {
    super(key);
    this.client = client;
    this.token = token;
    this.receiver = receiver;
  }

  private void handler(IPrivateFrame frame) {
    frame.accept(this);
  }

  public long getToken() {
    return token;
  }

  public void doConnect() throws IOException {
    if (!sc.finishConnect()) {
      return;
    }
    var currentToken = client.privateConnectionMap.get(receiver).getToken();
    var discoverMsg = new DiscoverMessage(client.getLogin(), currentToken);
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

  }

  @Override
  public void visit(DiscoverMessage message) {
    var login = message.getLogin();
    if (client.privateConnectionMap.get(login).getToken() == message.getToken()) {
      var connectionInfo = client.privateConnectionMap.get(login);
      connectionInfo.setState(SUCCEED);
      connectionInfo.setDestinatorContext(this);
      var confirmMsg = new ConfirmDiscoverMessage(login, client.getLogin());
      queueMessage(confirmMsg.toBuffer());
    } else {
      System.err.println("bad token");
      silentlyClose();
    }
  }

  @Override
  public void visit(ConfirmDiscoverMessage confirmDiscoverMessage) {
    var connectionInfo = client.privateConnectionMap.get(confirmDiscoverMessage.getSender());
    connectionInfo.setState(SUCCEED);
    connectionInfo.setDestinatorContext(this);
    // System.out.println("connection succeded with " + confirmDiscoverMessage.getSender());
  }
}
