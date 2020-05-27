package fr.upem.chathack.reader.trame;

import java.nio.ByteBuffer;
import fr.upem.chathack.model.LongSizedString;
import fr.upem.chathack.publicframe.AuthentificatedConnection;
import fr.upem.chathack.reader.IReader;
import fr.upem.chathack.reader.LongSizedStringReader;
/**
 * Class use to read AthenticatedConnection type
 *
 */
public class AuthentificatedConnectionReader implements IReader<AuthentificatedConnection> {
  private enum State {
    WAITING_LOGIN, WAITING_PASSWORD, DONE, ERROR
  }

  private final LongSizedStringReader reader;
  private State state;
  private AuthentificatedConnection value;
  private LongSizedString login;

  public AuthentificatedConnectionReader() {
    this.reader = new LongSizedStringReader();
    this.state = State.WAITING_LOGIN;
  }

  @Override
  public ProcessStatus process(ByteBuffer bb) {
    switch (state) {
      case WAITING_LOGIN: {
        var status = reader.process(bb);
        if (status != ProcessStatus.DONE) {
          return status;
        }
        login = reader.get();
        reader.reset();
        state = State.WAITING_PASSWORD;
      }
      case WAITING_PASSWORD: {
        var status = reader.process(bb);
        if (status != ProcessStatus.DONE) {
          return status;
        }
        var password = reader.get();
        value = new AuthentificatedConnection(login, password);
        state = State.DONE;
        return ProcessStatus.DONE;
      }
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  public AuthentificatedConnection get() {
    if (state != State.DONE) {
      throw new IllegalStateException();
    }
    return value;
  }

  @Override
  public void reset() {
    state = State.WAITING_LOGIN;
    reader.reset();
    value = null;
  }

}
