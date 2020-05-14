package chathack.common.reader;

import static chathack.utils.Helper.BUFFER_SIZE;
import java.nio.ByteBuffer;
import java.util.function.Function;
import chathack.common.model.Request.OpCode;
import chathack.frame.BroadcastMessage;
import chathack.frame.IFrame;
import chathack.utils.Helper;

public class FrameReader implements IReader<IFrame> {

  private enum State {
    WAITING_OPCODE, WAITING_CONTENT, DONE, ERROR
  }

  private final ByteReader byteReader = new ByteReader();
  private final MessageReader messageReader = new MessageReader();

  private State state = State.WAITING_OPCODE;
  private OpCode opCode;
  private IFrame value;
  private final ByteBuffer internalbb = ByteBuffer.allocate(BUFFER_SIZE);


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
            break;
          case AUTHENTICATED_CLIENT_CONNECTION:
            break;
          case BROADCAST_MESSAGE:

            var status = contentProcess(bb, messageReader::process);
            if (status != ProcessStatus.DONE) {
              return status;
            }

            var message = messageReader.get();
            System.out.println("mes: " + message);
            value = new BroadcastMessage(message.getLogin(), message.getValue());
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
    }
    return ProcessStatus.REFILL;
  }


  @Override
  public IFrame get() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void reset() {
    // TODO Auto-generated method stub

  }

}
