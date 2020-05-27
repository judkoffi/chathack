package fr.upem.chathack.reader;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * Class use to read InetSocketAddress type
 *
 */
public class InetSocketAddressReader implements IReader<InetSocketAddress> {
  private enum State {
    WAITING_IP, WAITING_PORT, DONE, ERROR
  }

  private long address;
  private long port;
  private State state;
  private final LongReader reader;
  private InetSocketAddress value;

  public InetSocketAddressReader() {
    this.reader = new LongReader();
    this.state = State.WAITING_IP;
  }

  @Override
  public ProcessStatus process(ByteBuffer bb) {
    switch (state) {
      case WAITING_IP: {
        var status = reader.process(bb);
        if (status != ProcessStatus.DONE) {
          return status;
        }
        address = reader.get();
        reader.reset();
        state = State.WAITING_PORT;
      }
      case WAITING_PORT: {
        var status = reader.process(bb);
        if (status != ProcessStatus.DONE) {
          return status;
        }
        port = reader.get();
        value = new InetSocketAddress(longToIp(address), (int) port);
        state = State.DONE;
        return ProcessStatus.DONE;
      }
      default:
        throw new IllegalStateException();
    }
  }

  private String longToIp(long ip) {

    return ((ip >> 24) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 8) & 0xFF) + "."
        + (ip & 0xFF);

  }

  @Override
  public InetSocketAddress get() {
    if (state != State.DONE) {
      throw new IllegalStateException();
    }
    return value;
  }

  @Override
  public void reset() {
    state = State.WAITING_IP;
    reader.reset();
    value = null;
    address = 0;
    port = 0;
  }
}
