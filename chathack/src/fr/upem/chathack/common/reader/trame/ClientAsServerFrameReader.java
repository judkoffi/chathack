package fr.upem.chathack.common.reader.trame;

import java.nio.ByteBuffer;
import fr.upem.chathack.common.model.OpCode;
import fr.upem.chathack.common.reader.IReader;
import fr.upem.chathack.frame.visitor.IPrivateFrame;

public class ClientAsServerFrameReader implements IReader<IPrivateFrame> {

  private enum State {
    WAITING_OPCODE, WAITING_CONTENT, DONE, ERROR
  }

  /**
   * Readers
   */

  private final DirectMessageReader directMessageReader;
  private final AnonymousConnectionReader anonymousConnectionReader;
  private final DiscoverMessageReader discoverMessageReader;

  private State state;
  private IReader<? extends IPrivateFrame> currentFrameReader;
  private IPrivateFrame value;

  public ClientAsServerFrameReader() {
    this.state = State.WAITING_OPCODE;
    this.directMessageReader = new DirectMessageReader();
    this.anonymousConnectionReader = new AnonymousConnectionReader();
    this.discoverMessageReader = new DiscoverMessageReader();
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
            System.out.println("éjdnjqsnd");
            currentFrameReader = directMessageReader;
            break;
          case OpCode.DISCOVER_MESSAGE:
            currentFrameReader = discoverMessageReader;
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
    anonymousConnectionReader.reset();
    discoverMessageReader.reset();
  }
}
