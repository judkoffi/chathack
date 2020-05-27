package fr.upem.chathack.reader.trame;

import static fr.upem.chathack.utils.Helper.LIMIT_FILE_CONTENT_SIZE;
import java.nio.ByteBuffer;
import fr.upem.chathack.model.LongSizedString;
import fr.upem.chathack.privateframe.FileMessage;
import fr.upem.chathack.reader.IReader;
import fr.upem.chathack.reader.IntReader;
import fr.upem.chathack.reader.LongSizedStringReader;
/**
 * Class use to read FileMessage type
 *
 */
public class FileMessageReader implements IReader<FileMessage> {
  private enum State {
    WAITING_TARGET, WAITING_FILENAME, WAITING_CONTENT_SIZE, WAITING_FILE_CONTENT, DONE, ERROR
  }

  private LongSizedStringReader sizeStringReader;
  private IntReader intReader;

  private LongSizedString filename;
  private LongSizedString target;

  private int contentSize;

  private FileMessage value;
  private State state;

  private ByteBuffer internalbb = ByteBuffer.allocate(LIMIT_FILE_CONTENT_SIZE);

  public FileMessageReader() {
    this.sizeStringReader = new LongSizedStringReader();
    this.intReader = new IntReader();
    this.state = State.WAITING_TARGET;
  }

  @Override
  public ProcessStatus process(ByteBuffer bb) {
    switch (state) {
      case WAITING_TARGET: {
        var status = sizeStringReader.process(bb);
        if (status != ProcessStatus.DONE) {
          return status;
        }
        target = sizeStringReader.get();
        sizeStringReader.reset();
        state = State.WAITING_FILENAME;
      }
      case WAITING_FILENAME: {
        var status = sizeStringReader.process(bb);
        if (status != ProcessStatus.DONE) {
          return status;
        }
        filename = sizeStringReader.get();
        sizeStringReader.reset();
        state = State.WAITING_CONTENT_SIZE;
      }
      case WAITING_CONTENT_SIZE: {
        var status = intReader.process(bb);
        if (status != ProcessStatus.DONE)
          return status;

        contentSize = intReader.get();
        if (contentSize <= 0 || contentSize > LIMIT_FILE_CONTENT_SIZE) {
          state = State.ERROR;
          return ProcessStatus.ERROR;
        }

        state = State.WAITING_FILE_CONTENT;
      }
      case WAITING_FILE_CONTENT: {
        var missing = contentSize - internalbb.position();
        bb.flip();
        if (bb.remaining() <= missing) {
          internalbb.put(bb);
        } else {
          var oldLimit = bb.limit();
          bb.limit(missing);
          internalbb.put(bb);
          bb.limit(oldLimit);
        }
        bb.compact();
        if (internalbb.position() < contentSize) {
          return ProcessStatus.REFILL;
        }
        state = State.DONE;
        internalbb.flip();
        value = new FileMessage(filename, target, internalbb);
        return ProcessStatus.DONE;
      }

      default:
        throw new IllegalStateException();
    }

  }

  @Override
  public FileMessage get() {
    if (state != State.DONE) {
      throw new IllegalStateException();
    }
    return value;
  }

  @Override
  public void reset() {
    state = State.WAITING_TARGET;
    sizeStringReader.reset();
    intReader.reset();
    target = null;
    filename = null;
    contentSize = 0;
    value = null;
    internalbb.clear();
  }

}
