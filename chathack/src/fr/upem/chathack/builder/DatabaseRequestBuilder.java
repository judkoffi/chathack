package fr.upem.chathack.builder;

import static fr.upem.chathack.builder.DatabaseRequestBuilder.RequestDatabaseOpcode.ASK_CREDENTIAL;
import static fr.upem.chathack.builder.DatabaseRequestBuilder.RequestDatabaseOpcode.ASK_LOGIN_PRESENT;
import static fr.upem.chathack.builder.DatabaseRequestBuilder.ResponseDatabaseOpcode.BAD_CREDENTIAL;
import static fr.upem.chathack.builder.DatabaseRequestBuilder.ResponseDatabaseOpcode.GOOD_CREDENTIAL;
import java.nio.ByteBuffer;

public class DatabaseRequestBuilder {

  public enum RequestDatabaseOpcode {
    ASK_CREDENTIAL, ASK_LOGIN_PRESENT
  }

  public enum ResponseDatabaseOpcode {
    GOOD_CREDENTIAL, BAD_CREDENTIAL,
  }

  public static byte requestDBOpcodeToByte(RequestDatabaseOpcode op) {
    switch (op) {
      case ASK_CREDENTIAL:
        return (byte) 1;
      case ASK_LOGIN_PRESENT:
        return (byte) 2;
      default:
        throw new IllegalArgumentException("unknown db request op code " + op);
    }
  }

  public static RequestDatabaseOpcode byteToRequestDBOpcode(byte b) {
    switch (b) {
      case 1:
        return ASK_CREDENTIAL;
      case 2:
        return ASK_LOGIN_PRESENT;
      default:
        throw new IllegalArgumentException("unknown db request byte " + b);
    }
  }

  public static byte responseOpcodeToByte(ResponseDatabaseOpcode op) {
    switch (op) {
      case GOOD_CREDENTIAL:
        return (byte) 1;
      case BAD_CREDENTIAL:
        return (byte) 0;
      default:
        throw new IllegalArgumentException("unknown db request op code " + op);
    }
  }

  public static ResponseDatabaseOpcode byteToResponseDBOpcode(byte b) {
    switch (b) {
      case 0:
        return BAD_CREDENTIAL;
      case 1:
        return GOOD_CREDENTIAL;
      default:
        throw new IllegalArgumentException("unknown db response byte " + b);
    }
  }

  public static ByteBuffer buildCheckRequest(long id, ByteBuffer bb) {
    var requestBuffer = ByteBuffer.allocate(Byte.BYTES + Long.BYTES + bb.limit());
    requestBuffer.put(requestDBOpcodeToByte(ASK_CREDENTIAL));
    requestBuffer.putLong(id);
    requestBuffer.put(bb);
    return requestBuffer.flip();
  }
}
