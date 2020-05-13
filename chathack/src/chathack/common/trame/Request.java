package chathack.common.trame;

import java.nio.ByteBuffer;
import chathack.utils.Helper;

public class Request {
  public enum OpCode {
    ANONYMOUS_CLIENT_CONNECTION, //
    AUTHENTICATED_CLIENT_CONNECTION, //
    BROADCOAST_MESSAGE, //
    REQUEST_PRIVATE_CLIENT_CONNECTION, //
    SERVER_NOTIFY_PRIVATE_CLIENT_CONNECTION, //
    CLIENT_FAILED_PRIVATE_CLIENT_CONNECTION, //
    SUCCEDED_PRIVATE_CLIENT_CONNECTION, //
    PRIVATE_MESSAGE,
  }

  final OpCode op;
  final Message content;
  final ByteBuffer bb;

  public Request(OpCode op, Message content) {
    this.op = op;
    this.content = content;
    this.bb = ByteBuffer.allocate(Byte.BYTES + content.getTotalSize());
    fillBuffer();
  }

  private void fillBuffer() {
    bb.put(Helper.opcodeToByte(op));
    bb.put(content.bb);
  }

  public ByteBuffer toBuffer() {
    return bb.duplicate().flip();
  }


  public int size() {
    return bb.limit();
  }

  @Override
  public String toString() {
    return "[" + op + "]";
  }

}
