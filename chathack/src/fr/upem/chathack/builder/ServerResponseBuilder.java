package fr.upem.chathack.builder;

import java.nio.ByteBuffer;
import fr.upem.chathack.common.model.OpCode;
import fr.upem.chathack.utils.Helper;

public class ServerResponseBuilder {

  private ServerResponseBuilder() {}

  public static ByteBuffer errorResponse(String msg) {
    var encodedMsg = Helper.DEFAULT_CHARSET.encode(msg);
    var bb = ByteBuffer.allocate(Byte.BYTES + Long.BYTES + encodedMsg.limit());
    System.out.println("err op code " + OpCode.SERVER_ERROR_MESSAGE);
    bb.put(OpCode.SERVER_ERROR_MESSAGE);
    bb.putInt(encodedMsg.limit());
    bb.put(encodedMsg);
    bb.flip();
    return bb;
  }

}
