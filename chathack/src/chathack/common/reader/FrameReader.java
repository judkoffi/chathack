package chathack.common.reader;

import java.nio.ByteBuffer;
import java.util.function.Function;
import chathack.common.model.OpCode;
import chathack.frame.AnonymousConnection;
import chathack.frame.BroadcastMessage;
import chathack.frame.IFrame;
import chathack.utils.Helper;

public class FrameReader implements IReader<IFrame> {

  private enum State {
    WAITING_OPCODE, WAITING_CONTENT, DONE, ERROR
  }

  private final ByteReader byteReader = new ByteReader();
  private final MessageReader messageReader = new MessageReader();
  private final StringReader stringReader = new StringReader();

  private State state = State.WAITING_OPCODE;
  private OpCode opCode;
  private IFrame value;


  private ProcessStatus contentProcess(ByteBuffer bb, Function<ByteBuffer, ProcessStatus> f) {
    return f.apply(bb);
  }

  @Override
  public ProcessStatus process(ByteBuffer bb) {
    switch (state) {
      case WAITING_OPCODE: {
        var opcodeStatus = byteReader.process(bb);
        if (opcodeStatus != ProcessStatus.DONE)
          return opcodeStatus;

        // TODO: add opcode check
        opCode = Helper.byteToOpCode(byteReader.get());
        state = State.WAITING_CONTENT;
      }

      case WAITING_CONTENT: {
        switch (opCode) {
          case ANONYMOUS_CLIENT_CONNECTION:
            var contentProcess = contentProcess(bb, stringReader::process);
            if (contentProcess != ProcessStatus.DONE)
              return contentProcess;

            value = new AnonymousConnection(stringReader.get());
            state = State.DONE;
            return ProcessStatus.DONE;
          case AUTHENTICATED_CLIENT_CONNECTION:
            break;



          case BROADCAST_MESSAGE:
            contentProcess = contentProcess(bb, messageReader::process);
            if (contentProcess != ProcessStatus.DONE)
              return contentProcess;

            value = new BroadcastMessage(messageReader.get());
            state = State.DONE;
            return ProcessStatus.DONE;


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

      default:
        throw new IllegalStateException();
    }
  }


  @Override
  public IFrame get() {
    if (state != State.DONE) {
      throw new IllegalStateException();
    }
    return value;
  }

  @Override
  public void reset() {
    state = State.WAITING_OPCODE;
    value = null;
    opCode = null;

    byteReader.reset();
    messageReader.reset();
    stringReader.reset();
  }

}
