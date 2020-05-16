package fr.upem.chathack.builder;

import java.nio.ByteBuffer;
import fr.upem.chathack.utils.Helper;

public class StringBuilder implements IBuilder<String> {

  @Override
  public ByteBuffer build(String value) {
    var msg = Helper.DEFAULT_CHARSET.encode(value);
    var bb = ByteBuffer.allocate(Integer.BYTES + msg.limit());
    bb.putInt(msg.limit());
    bb.put(msg);
    return bb;
  }

}
