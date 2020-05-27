package fr.upem.chathack.reader.trame;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import fr.upem.chathack.model.LongSizedString;
import fr.upem.chathack.publicframe.AcceptPrivateConnection;
import fr.upem.chathack.reader.IReader;
import fr.upem.chathack.reader.InetSocketAddressReader;
import fr.upem.chathack.reader.LongReader;
import fr.upem.chathack.reader.LongSizedStringReader;
/**
 * Class use to read AcceptPrivateConnection type
 *
 */
public class AcceptPrivateConnectionReader implements IReader<AcceptPrivateConnection> {
  private enum State {
    WAITING_TARGET_LOGIN, WAITING_TARGET_ADDR, WAITING_TOKEN, WAITING_FROM_LOGIN, DONE, ERROR
  }

  private State state;
  private AcceptPrivateConnection value;
  private LongSizedString targetLogin;
  private LongSizedString fromLogin;
  private InetSocketAddress targetSocketAddress;
  private long token;

  private final LongSizedStringReader sizedStringReader;
  private final InetSocketAddressReader socketAddressReader;
  private final LongReader longReader;

  public AcceptPrivateConnectionReader() {
    this.state = State.WAITING_TARGET_LOGIN;
    this.socketAddressReader = new InetSocketAddressReader();
    this.sizedStringReader = new LongSizedStringReader();
    this.longReader = new LongReader();
  }

  @Override
  public ProcessStatus process(ByteBuffer bb) {
    switch (state) {
      case WAITING_TARGET_LOGIN: {
        var status = sizedStringReader.process(bb);
        if (status != ProcessStatus.DONE) {
          return status;
        }
        targetLogin = sizedStringReader.get();
        sizedStringReader.reset();
        state = State.WAITING_TARGET_ADDR;
      }
      case WAITING_TARGET_ADDR: {
        var status = socketAddressReader.process(bb);
        if (status != ProcessStatus.DONE) {
          return status;
        }
        targetSocketAddress = socketAddressReader.get();
        socketAddressReader.reset();
        state = State.WAITING_TOKEN;
      }
      case WAITING_TOKEN: {
        var status = longReader.process(bb);
        if (status != ProcessStatus.DONE) {
          return status;
        }
        token = longReader.get();
        longReader.reset();
        state = State.WAITING_FROM_LOGIN;
      }
      case WAITING_FROM_LOGIN: {
        var status = sizedStringReader.process(bb);
        if (status != ProcessStatus.DONE) {
          return status;
        }
        fromLogin = sizedStringReader.get();
        value = new AcceptPrivateConnection(fromLogin, targetLogin, targetSocketAddress, token);
        state = State.DONE;
        return ProcessStatus.DONE;
      }
      default:
        throw new IllegalStateException();
    }
  }

  @Override
  public AcceptPrivateConnection get() {
    if (state != State.DONE) {
      throw new IllegalStateException();
    }
    return value;
  }

  @Override
  public void reset() {
    state = State.WAITING_TARGET_LOGIN;
    sizedStringReader.reset();
    socketAddressReader.reset();
    longReader.reset();
    value = null;
    targetLogin = null;
    fromLogin = null;
    targetSocketAddress = null;
  }
}
