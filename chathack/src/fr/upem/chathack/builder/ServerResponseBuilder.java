package fr.upem.chathack.builder;

import java.nio.ByteBuffer;
import fr.upem.chathack.common.model.OpCode;
import fr.upem.chathack.utils.Helper;

public class ServerResponseBuilder {

  private ServerResponseBuilder() {}

  public static ByteBuffer errorResponse(String msg) {
    var encodedMsg = Helper.DEFAULT_CHARSET.encode(msg);
    var bb = ByteBuffer.allocate(Byte.BYTES + Long.BYTES + encodedMsg.limit());
    bb.put(OpCode.ERROR_MESSAGE);
    return bb;
  }

}
