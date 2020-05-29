package fr.upem.chathack.reader.trame;

import java.nio.ByteBuffer;
import fr.upem.chathack.frame.IPrivateFrame;
import fr.upem.chathack.privateframe.ClosePrivateConnectionMessage;
import fr.upem.chathack.privateframe.ConfirmDiscoverMessage;
import fr.upem.chathack.privateframe.DirectMessage;
import fr.upem.chathack.privateframe.DiscoverMessage;
import fr.upem.chathack.privateframe.FileMessage;
import fr.upem.chathack.reader.BufferReader;
import fr.upem.chathack.reader.IReader;
import fr.upem.chathack.reader.LongReader;
import fr.upem.chathack.reader.LongSizedStringReader;
import fr.upem.chathack.reader.builder.ReaderBuilder;
import fr.upem.chathack.utils.OpCode;

/**
 * Class use to read PrivateConnectionFrame type
 *
 */
public class PrivateConnectionFrameReader implements IReader<IPrivateFrame> {

  private enum State {
    WAITING_OPCODE, WAITING_CONTENT, DONE, ERROR
  }

  /**
   * Base readers
   */
  private final LongSizedStringReader longSizedStringReader = new LongSizedStringReader();
  private final LongReader longReader = new LongReader();
  private final BufferReader bufferReader = new BufferReader();

  /**
   * Readers
   */
  private final IReader<DirectMessage> directMessageReader = ReaderBuilder
    .<DirectMessage>create()
    .addSubReader(longSizedStringReader)// destinator
    .addSubReader(longSizedStringReader)// from
    .addSubReader(longSizedStringReader)// message
    .addConstructor(DirectMessage::of)
    .build();

  private final IReader<DiscoverMessage> discoverMessageReader = ReaderBuilder
    .<DiscoverMessage>create()
    .addSubReader(longSizedStringReader)// message
    .addSubReader(longReader)// token
    .addConstructor(DiscoverMessage::of)
    .build();

  private final IReader<ConfirmDiscoverMessage> confirmDiscoverMessageReader = ReaderBuilder
    .<ConfirmDiscoverMessage>create()
    .addSubReader(longSizedStringReader)// destinator
    .addSubReader(longSizedStringReader)// sender
    .addConstructor(ConfirmDiscoverMessage::of)
    .build();

  private final IReader<FileMessage> fileMessageReader = ReaderBuilder
    .<FileMessage>create()
    .addSubReader(longSizedStringReader)// destinator
    .addSubReader(longSizedStringReader)// filename
    .addSubReader(bufferReader)// content
    .addConstructor(FileMessage::of)
    .build();

  private final IReader<ClosePrivateConnectionMessage> privateConnectionCloseReader = ReaderBuilder
    .<ClosePrivateConnectionMessage>create()
    .addSubReader(longSizedStringReader)// from
    .addConstructor(ClosePrivateConnectionMessage::of)
    .build();

  private State state = State.WAITING_OPCODE;
  private IReader<? extends IPrivateFrame> currentFrameReader;
  private IPrivateFrame value;

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
          case OpCode.PRIVATE_CONNECTION_CLOSE:
            currentFrameReader = privateConnectionCloseReader;
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
    longSizedStringReader.reset();
    longReader.reset();
    bufferReader.reset();
  }
}
