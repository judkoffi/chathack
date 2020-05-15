package chathack.builder;

import java.nio.ByteBuffer;

public class DatabaseRequestBuilder {

  private enum DatabaseRequestEnum {
    ASK_CREDENTIAL
  }

  private static byte requestEnumToByte(DatabaseRequestEnum e) {
    switch (e) {
      case ASK_CREDENTIAL:
        return (byte) 1;
      default:
        throw new IllegalArgumentException();
    }
  }


  public static ByteBuffer buildCheckRequest(ByteBuffer bb) {
    var requestBuffer = ByteBuffer.allocate(Byte.BYTES + Long.BYTES + bb.limit());
    requestBuffer.put(requestEnumToByte(DatabaseRequestEnum.ASK_CREDENTIAL));
    requestBuffer.putLong(1);
    requestBuffer.put(bb);
    return requestBuffer.flip();
  }
}
