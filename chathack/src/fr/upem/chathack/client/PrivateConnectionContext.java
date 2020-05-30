package fr.upem.chathack.client;

import static fr.upem.chathack.client.PrivateConnectionInfo.PrivateConnectionState.SUCCEED;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.SelectionKey;
import java.nio.file.Path;
import fr.upem.chathack.context.BaseContext;
import fr.upem.chathack.frame.IPrivateFrame;
import fr.upem.chathack.privateframe.ClosePrivateConnectionMessage;
import fr.upem.chathack.privateframe.ConfirmDiscoverMessage;
import fr.upem.chathack.privateframe.DirectMessage;
import fr.upem.chathack.privateframe.DiscoverMessage;
import fr.upem.chathack.privateframe.FileMessage;
import fr.upem.chathack.reader.IReader;
import fr.upem.chathack.reader.trame.PrivateConnectionFrameReader;
import fr.upem.chathack.visitor.IPrivateFrameVisitor;

/**
 * Class use to represent context between a client and another client
 */
public class PrivateConnectionContext extends BaseContext implements IPrivateFrameVisitor {
  private final ClientChatHack client;
  private final PrivateConnectionFrameReader reader = new PrivateConnectionFrameReader();
  private long token; // token use to certify private connection established between two clients

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
    var currentToken = client.privateConnectionMap.get(receiver).token;
    var discoverMsg = new DiscoverMessage(client.login, currentToken);
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
    System.out.println("DM received: " + directMessage);
  }

  @Override
  public void visit(DiscoverMessage message) {
    var login = message.getLogin();
    if (client.privateConnectionMap.get(login).token == message.getToken()) {
      var connectionInfo = client.privateConnectionMap.get(login);
      connectionInfo.state = SUCCEED;
      connectionInfo.destinatorContext = this;
      var confirmMsg = new ConfirmDiscoverMessage(client.login);
      queueMessage(confirmMsg.toBuffer());
    } else {
      System.err.println("bad token");
      silentlyClose();
    }
  }

  @Override
  public void visit(ConfirmDiscoverMessage confirmDiscoverMessage) {
    var connectionInfo = client.privateConnectionMap.get(confirmDiscoverMessage.getSender());
    connectionInfo.state = SUCCEED;
    connectionInfo.destinatorContext = this;

    var pendingMessageQueue = connectionInfo.pendingDirectMessages;
    while (!pendingMessageQueue.isEmpty()) {
      var dm = pendingMessageQueue.remove();
      connectionInfo.destinatorContext.queueMessage(dm);
    }
  }

  @Override
  public void visit(FileMessage fileMessage) {
    var filename = fileMessage.getFilename();
    var buffer = fileMessage.getContent();

    var filePath = Path.of(client.directoryPath.toAbsolutePath().toString(), filename);
    try (var stream = new RandomAccessFile(filePath.toFile(), "rw");
        var channel = stream.getChannel();) {
      channel.write(buffer);
    } catch (IOException e) {
      System.err.println(e.getMessage());
    }
    System.out.println("file reveiced and store at " + filePath);
  }

  @Override
  public void visit(ClosePrivateConnectionMessage closePrivateConnectionMessage) {
    System.out.println("received private connection close");
    var from = closePrivateConnectionMessage.getFrom();
    var value = client.privateConnectionMap.get(from);
    if (value != null) {
      value.destinatorContext.silentlyClose();
    }
  }
}
