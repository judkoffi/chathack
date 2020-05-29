package fr.upem.chathack.reader.trame;

import java.nio.ByteBuffer;
import fr.upem.chathack.frame.IPublicFrame;
import fr.upem.chathack.model.Message;
import fr.upem.chathack.publicframe.AcceptPrivateConnection;
import fr.upem.chathack.publicframe.AnonymousConnection;
import fr.upem.chathack.publicframe.AuthentificatedConnection;
import fr.upem.chathack.publicframe.BroadcastMessage;
import fr.upem.chathack.publicframe.LogOutMessage;
import fr.upem.chathack.publicframe.RejectPrivateConnection;
import fr.upem.chathack.publicframe.RequestPrivateConnection;
import fr.upem.chathack.reader.IReader;
import fr.upem.chathack.reader.InetSocketAddressReader;
import fr.upem.chathack.reader.LongReader;
import fr.upem.chathack.reader.LongSizedStringReader;
import fr.upem.chathack.reader.builder.ReaderBuilder;
import fr.upem.chathack.utils.OpCode;

/**
 * Class use to read all public frame exchange between client and server
 *
 */
public class ServerFrameReader implements IReader<IPublicFrame> {

  private enum State {
    WAITING_OPCODE, WAITING_CONTENT, DONE, ERROR
  }

  /**
   * Base readers
   */
  private final LongSizedStringReader longSizedStringReader = new LongSizedStringReader();
  private final LongReader longReader = new LongReader();
  private final InetSocketAddressReader socketAddressReader = new InetSocketAddressReader();

  /**
   * Builded readers
   */
  private final IReader<BroadcastMessage> broadcastMessageReader = ReaderBuilder
    .<BroadcastMessage>create()
    .addSubReader(longSizedStringReader)
    .addSubReader(longSizedStringReader)
    .addConstructor(params -> new BroadcastMessage(Message.of(params)))
    .build();

  private final IReader<AnonymousConnection> anonymousReader = ReaderBuilder
    .<AnonymousConnection>create()
    .addSubReader(longSizedStringReader)
    .addConstructor(AnonymousConnection::of)
    .build();

  private final IReader<AuthentificatedConnection> authenticatedReader = ReaderBuilder
    .<AuthentificatedConnection>create()
    .addSubReader(longSizedStringReader)
    .addSubReader(longSizedStringReader)
    .addConstructor(AuthentificatedConnection::of)
    .build();

  private final IReader<RequestPrivateConnection> requestConnectionReader = ReaderBuilder
    .<RequestPrivateConnection>create()
    .addSubReader(longSizedStringReader)
    .addSubReader(longSizedStringReader)
    .addConstructor(RequestPrivateConnection::of)
    .build();

  private final IReader<AcceptPrivateConnection> acceptPrivateConnectionReader = ReaderBuilder
    .<AcceptPrivateConnection>create()
    .addSubReader(longSizedStringReader)// receiver
    .addSubReader(socketAddressReader)// ip + port
    .addSubReader(longReader)// token
    .addSubReader(longSizedStringReader)// from
    .addConstructor(AcceptPrivateConnection::of)
    .build();

  private final IReader<RejectPrivateConnection> rejectPrivateConnectionReader = ReaderBuilder
    .<RejectPrivateConnection>create()
    .addSubReader(longSizedStringReader)
    .addSubReader(longSizedStringReader)
    .addConstructor(RejectPrivateConnection::of)
    .build();

  private final IReader<LogOutMessage> logOutMessageReader = ReaderBuilder
    .<LogOutMessage>create()
    .addSubReader(longSizedStringReader)
    .addSubReader(longSizedStringReader)
    .addConstructor(params -> new LogOutMessage(Message.of(params)))
    .build();

  private State state = State.WAITING_OPCODE;
  private IReader<? extends IPublicFrame> currentFrameReader;
  private IPublicFrame value;


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
          case OpCode.CLIENT_LOG_OUT:
            currentFrameReader = logOutMessageReader;
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
    logOutMessageReader.reset();
  }
}
