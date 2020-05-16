package fr.upem.chathack.builder;

import java.nio.ByteBuffer;
import fr.upem.chathack.common.model.LongSizedString;
import fr.upem.chathack.common.model.OpCode;

public class ServerResponseBuilder {

  private ServerResponseBuilder() {}

  public static ByteBuffer errorResponse(String msg) {
    var sizedStr = new LongSizedString(msg);
    var bb = ByteBuffer.allocate(Byte.BYTES + Long.BYTES + (int) sizedStr.getSize());
    bb.put(OpCode.SERVER_ERROR_MESSAGE);
    bb.put(sizedStr.toBuffer());
    bb.flip();
    return bb;
  }

}
