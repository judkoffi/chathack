package chathack.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import chathack.common.model.Request.OpCode;

/**
 * Class use to store constants, shared values
 */
public class Helper {
  private Helper() {}

  public static final int BUFFER_SIZE = 10_000;
  public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  public static OpCode byteToOpCode(byte b) {
    switch (b) {
      case 0:
        return OpCode.ANONYMOUS_CLIENT_CONNECTION;
      case 1:
        return OpCode.AUTHENTICATED_CLIENT_CONNECTION;
      case 2:
        return OpCode.BROADCAST_MESSAGE;
      default:
        throw new IllegalArgumentException("Unexpected value: " + b);
    }
  }


  public static byte opcodeToByte(OpCode opCode) {
    switch (opCode) {
      case ANONYMOUS_CLIENT_CONNECTION:
        return 0;
      case AUTHENTICATED_CLIENT_CONNECTION:
        return 1;
      case BROADCAST_MESSAGE:
        return 2;
      default:
        throw new IllegalArgumentException("Unexpected value: " + opCode);
    }
  }
}
