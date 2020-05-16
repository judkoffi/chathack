package fr.upem.chathack.common.reader;

import java.nio.ByteBuffer;
import java.util.function.Function;
import fr.upem.chathack.common.model.OpCode;
import fr.upem.chathack.frame.AnonymousConnection;
import fr.upem.chathack.frame.AuthentificatedConnection;
import fr.upem.chathack.frame.BroadcastMessage;
import fr.upem.chathack.frame.IFrame;

public class FrameReader implements IReader<IFrame> {

  private enum State {
    WAITING_OPCODE, WAITING_CONTENT, DONE, ERROR
  }

  private final ByteReader byteReader = new ByteReader();
  private final IntMessageReader messageReader = new IntMessageReader();
  private final StringReader stringReader = new StringReader();

  private State state = State.WAITING_OPCODE;
  private byte opCode;
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
        opCode = byteReader.get();
        System.out.println("FrameReader opcode read value " + opCode);
        state = State.WAITING_CONTENT;
      }

      case WAITING_CONTENT: {
        switch (opCode) {
          case OpCode.ANONYMOUS_CLIENT_CONNECTION:
            var contentProcess = contentProcess(bb, stringReader::process);
            if (contentProcess != ProcessStatus.DONE)
              return contentProcess;

            value = new AnonymousConnection(stringReader.get());
            state = State.DONE;
            return ProcessStatus.DONE;

          case OpCode.AUTHENTICATED_CLIENT_CONNECTION:
            contentProcess = contentProcess(bb, messageReader::process);
            if (contentProcess != ProcessStatus.DONE) {
              return contentProcess;
            }
            value = new AuthentificatedConnection(messageReader.get());
            state = State.DONE;
            return ProcessStatus.DONE;

          case OpCode.BROADCAST_MESSAGE:
            contentProcess = contentProcess(bb, messageReader::process);
            if (contentProcess != ProcessStatus.DONE)
              return contentProcess;

            value = new BroadcastMessage(messageReader.get());
            state = State.DONE;
            return ProcessStatus.DONE;


          case OpCode.CLIENT_FAILED_PRIVATE_CLIENT_CONNECTION:
            break;
          case OpCode.PRIVATE_MESSAGE:
            break;
          case OpCode.REQUEST_PRIVATE_CLIENT_CONNECTION:
            break;
          case OpCode.SERVER_NOTIFY_PRIVATE_CLIENT_CONNECTION:
            break;
          case OpCode.SUCCEDED_PRIVATE_CLIENT_CONNECTION:
            break;



          case OpCode.SERVER_ERROR_MESSAGE:
            System.out.println("error message server");
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
    opCode = -1;
    byteReader.reset();
    messageReader.reset();
    stringReader.reset();
  }

}
