package chathack.context;

import java.nio.channels.SelectionKey;
import chathack.ServerChatHack;
import chathack.common.reader.ByteReader;
import chathack.common.reader.IReader;
import chathack.common.reader.IReader.ProcessStatus;
import chathack.common.reader.StringReader;
import chathack.common.trame.Request.OpCode;
import chathack.utils.Helper;

public class ServerContext extends BaseContext {
  private final ServerChatHack server;
  private final ByteReader opcodeReader = new ByteReader();
  private final StringReader stringReader = new StringReader();


  public ServerContext(ServerChatHack server, SelectionKey key) {
    super(key);
    this.server = server;
  }

  private void handler(OpCode opcode) {
    switch (opcode) {
      case ANONYMOUS_CLIENT_CONNECTION:
        while (stringReader.process(bbin) != ProcessStatus.DONE) {

        }

        if (server.checkAnonymousLogin()) {

        }
        break;
      case AUTHENTICATED_CLIENT_CONNECTION:

        break;
      case BROADCAST_MESSAGE:
        break;
      case CLIENT_FAILED_PRIVATE_CLIENT_CONNECTION:
        break;
      case PRIVATE_MESSAGE:
        break;
      case REQUEST_PRIVATE_CLIENT_CONNECTION:
        break;
      case SERVER_NOTIFY_PRIVATE_CLIENT_CONNECTION:
        break;
      case SUCCEDED_PRIVATE_CLIENT_CONNECTION:
        break;
      default:
        break;
    }
  }

  @Override
  public void processIn() {
    for (;;) {
      IReader.ProcessStatus status = opcodeReader.process(bbin);
      switch (status) {
        case DONE:
          OpCode op = Helper.byteToOpCode(opcodeReader.get());
          handler(op);
          opcodeReader.reset();
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
    // TODO Auto-generated method stub

  }

}
