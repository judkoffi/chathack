package fr.upem.chathack.builder;

import java.nio.ByteBuffer;
import fr.upem.chathack.common.model.OpCode;

public class DatabaseRequestBuilder {
  private DatabaseRequestBuilder() {}

  public static ByteBuffer buildCheckRequest(long id, ByteBuffer bb) {
    var requestBuffer = ByteBuffer.allocate(Byte.BYTES + Long.BYTES + bb.limit());
    requestBuffer.put(OpCode.ASK_CREDENTIAL);
    requestBuffer.putLong(id);
    requestBuffer.put(bb);
    return requestBuffer.flip();
  }
}
