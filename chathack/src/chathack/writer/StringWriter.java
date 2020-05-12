package chathack.writer;

import java.nio.ByteBuffer;
import chathack.common.Helper;

public class StringWriter implements IWriter<String> {

  @Override
  public ByteBuffer build(String value) {
    var msg = Helper.DEFAULT_CHARSET.encode(value);
    var bb = ByteBuffer.allocate(Integer.BYTES + msg.limit());
    bb.putInt(msg.limit());
    bb.put(msg);
    return bb;
  }

}
