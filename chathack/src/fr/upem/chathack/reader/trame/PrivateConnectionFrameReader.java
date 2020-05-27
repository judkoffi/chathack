package fr.upem.chathack.reader.trame;

import java.nio.ByteBuffer;
import fr.upem.chathack.frame.IPrivateFrame;
import fr.upem.chathack.model.OpCode;
import fr.upem.chathack.reader.IReader;
/**
 * Class use to read PrivateConnectionFrame type
 *
 */
public class PrivateConnectionFrameReader implements IReader<IPrivateFrame> {

  private enum State {
    WAITING_OPCODE, WAITING_CONTENT, DONE, ERROR
  }

  /**
   * Readers
   */
  private final DirectMessageReader directMessageReader;
  private final DiscoverMessageReader discoverMessageReader;
  private final ConfirmDiscoverMessageReader confirmDiscoverMessageReader;
  private final FileMessageReader fileMessageReader;

  private State state;
  private IReader<? extends IPrivateFrame> currentFrameReader;
  private IPrivateFrame value;

  public PrivateConnectionFrameReader() {
    this.state = State.WAITING_OPCODE;
    this.directMessageReader = new DirectMessageReader();
    this.discoverMessageReader = new DiscoverMessageReader();
    this.confirmDiscoverMessageReader = new ConfirmDiscoverMessageReader();
    this.fileMessageReader = new FileMessageReader();
  }

  @Override
  public ProcessStatus process(ByteBuffer bb) {
    switch (state) {
      case WAITING_OPCODE: {
        bb.flip();
        if (!bb.hasRemaining()) {
          bb.compact();
          return ProcessStatus.REFILL;
        }
        var opcode = bb.get();
        bb.compact();
        switch (opcode) {
          case OpCode.DIRECT_MESSAGE:
            currentFrameReader = directMessageReader;
            break;
          case OpCode.DISCOVER_MESSAGE:
            currentFrameReader = discoverMessageReader;
            break;
          case OpCode.DISCOVER_CONFIRMATION:
            currentFrameReader = confirmDiscoverMessageReader;
            break;
          case OpCode.FILE_SEND:
        	  currentFrameReader = fileMessageReader;
        	  break;
          default:
            throw new IllegalArgumentException("unknown opcode " + opcode);
        }
        state = State.WAITING_CONTENT;
      }
      case WAITING_CONTENT: {
        var status = currentFrameReader.process(bb);
        if (status != ProcessStatus.DONE)
          return status;
        value = currentFrameReader.get();
        state = State.DONE;
        return ProcessStatus.DONE;
      }
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  public IPrivateFrame get() {
    if (state != State.DONE) {
      throw new IllegalStateException();
    }
    return value;
  }

  @Override
  public void reset() {
    state = State.WAITING_OPCODE;
    value = null;
    directMessageReader.reset();
    discoverMessageReader.reset();
    confirmDiscoverMessageReader.reset();
    fileMessageReader.reset();
  }
}
