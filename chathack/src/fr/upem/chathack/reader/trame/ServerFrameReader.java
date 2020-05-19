package fr.upem.chathack.reader.trame;

import java.nio.ByteBuffer;
import fr.upem.chathack.frame.IPublicFrame;
import fr.upem.chathack.model.OpCode;
import fr.upem.chathack.reader.IReader;

public class ServerFrameReader implements IReader<IPublicFrame> {

  private enum State {
    WAITING_OPCODE, WAITING_CONTENT, DONE, ERROR
  }

  /**
   * Readers
   */
  private final BroadcastMessageReader broadcastMessageReader;
  private final AnonymousConnectionReader anonymousReader;
  private final AuthentificatedConnectionReader authenticatedReader;
  private final RequestPrivateConnectionReader requestConnectionReader;
  private final AcceptPrivateConnectionReader acceptPrivateConnectionReader;
  private final RejectPrivateConnectionReader rejectPrivateConnectionReader;

  private State state;
  private IReader<? extends IPublicFrame> currentFrameReader;
  private IPublicFrame value;



  public ServerFrameReader() {
    this.broadcastMessageReader = new BroadcastMessageReader();
    this.anonymousReader = new AnonymousConnectionReader();
    this.authenticatedReader = new AuthentificatedConnectionReader();
    this.requestConnectionReader = new RequestPrivateConnectionReader();
    this.acceptPrivateConnectionReader = new AcceptPrivateConnectionReader();
    this.rejectPrivateConnectionReader = new RejectPrivateConnectionReader();
    this.state = State.WAITING_OPCODE;
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
          case OpCode.ANONYMOUS_CLIENT_CONNECTION:
            currentFrameReader = anonymousReader;
            break;
          case OpCode.AUTHENTICATED_CLIENT_CONNECTION:
            currentFrameReader = authenticatedReader;
            break;
          case OpCode.BROADCAST_MESSAGE:
            currentFrameReader = broadcastMessageReader;
            break;
          case OpCode.REQUEST_PRIVATE_CLIENT_CONNECTION:
            currentFrameReader = requestConnectionReader;
            break;
          case OpCode.SUCCEDED_PRIVATE_CLIENT_CONNECTION:
            currentFrameReader = acceptPrivateConnectionReader;
            break;
          case OpCode.REJECTED_PRIVATE_CLIENT_CONNECTION:
            currentFrameReader = rejectPrivateConnectionReader;
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
  public IPublicFrame get() {
    if (state != State.DONE) {
      throw new IllegalStateException();
    }
    return value;
  }

  @Override
  public void reset() {
    state = State.WAITING_OPCODE;
    value = null;
    broadcastMessageReader.reset();
    authenticatedReader.reset();
    anonymousReader.reset();
    requestConnectionReader.reset();
    acceptPrivateConnectionReader.reset();
    rejectPrivateConnectionReader.reset();
  }
}
