package fr.upem.chathack.reader;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Class use to read InetSocketAddress type
 *
 */
public class InetSocketAddressReader implements IReader<InetSocketAddress> {
  private enum State {
    WAITING_IP_SIZE, WAITING_IP, WAITING_PORT, DONE, ERROR
  }

  private ByteBuffer address;
  private int port;
  private State state;
  private int ipSize;
  private InetSocketAddress value;
  private IntReader intReader;

  public InetSocketAddressReader() {
    this.intReader = new IntReader();
    this.state = State.WAITING_IP_SIZE;
  }

  @Override
  public ProcessStatus process(ByteBuffer bb) {
    switch (state) {
      case WAITING_IP_SIZE: {
        var status = intReader.process(bb);
        if (status != ProcessStatus.DONE) {
          return status;
        }
        ipSize = intReader.get();
        intReader.reset();
        state = State.WAITING_IP;
      }
      case WAITING_IP: {
        address = ByteBuffer.allocate(ipSize);
        bb.flip();
        try {
          if (bb.remaining() <= address.remaining()) {
            address.put(bb);
          } else {
            var oldLimit = bb.limit();
            bb.limit(address.remaining());
            address.put(bb);
            bb.limit(oldLimit);
          }
        } finally {
          bb.compact();
        }

        if (address.hasRemaining()) {
          return ProcessStatus.REFILL;
        }
        address.flip();
        state = State.WAITING_PORT;
      }

      case WAITING_PORT: {
        var status = intReader.process(bb);
        if (status != ProcessStatus.DONE) {
          return status;
        }
        port = intReader.get();
        try {
          value = parseInetAddr();
        } catch (UnknownHostException e) {
          System.err.println("failed to parsed ip address in InetSocketAddrReader");
          state = State.ERROR;
          return ProcessStatus.ERROR;
        }
        state = State.DONE;
        return ProcessStatus.DONE;
      }
      default:
        throw new IllegalStateException();
    }
  }

  private InetSocketAddress parseInetAddr() throws UnknownHostException {
    var bytes = new byte[ipSize];
    for (var i = 0; i < ipSize; i++) {
      bytes[i] = address.get();
    }
    return new InetSocketAddress(InetAddress.getByAddress(bytes), port);
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
    state = State.WAITING_IP_SIZE;
    intReader.reset();
    value = null;
    address.clear();
    port = 0;
  }
}
