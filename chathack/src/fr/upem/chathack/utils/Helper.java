package fr.upem.chathack.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Class use to store constants, shared values
 */
public class Helper {
  private Helper() {}

  public static final int BUFFER_SIZE = 4096;
  public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
  public static final String WELCOME_MESSAGE = "Welcome to ChatHack !\n"
  		+ "To use this chat : \n"
  		+ "public message => no prefix\n"
  		+ "/requests => list private connection\n"
  		+ "/accept => accept private connection\n"
  		+ "/reject => reject private connection\n"
  		+ "/file => send file\n";

  public static ByteBuffer cloneByteBuffer(ByteBuffer bb) {
    int capacity = bb.limit();
    int pos = bb.position();
    ByteOrder order = bb.order();
    ByteBuffer copy;
    copy = ByteBuffer.allocate(capacity);
    bb.rewind();
    copy.order(order);
    copy.put(bb);
    copy.position(pos);
    bb.position(pos);
    return copy;
  }
}
