package chathack.common;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Class use to store constants, shared values
 */
public class Helper {
  private Helper() {}

  public static final int BUFFER_SIZE = 10_000;
  public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
}
