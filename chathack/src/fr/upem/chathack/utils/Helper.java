package fr.upem.chathack.utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

/**
 * Class use to store constants, shared values, default values
 */
public class Helper {
  private Helper() {}

  /**
   * Max IP address size
   */
  public static final int IP_ADDR_SIZE = Byte.BYTES * 16;

  /**
   * Default buffer size
   */
  public static final int BUFFER_SIZE = 50_000_000; // 50 mb

  /**
   * Default file size
   */
  public static final int LIMIT_FILE_CONTENT_SIZE = 30_000_000; // 30 mb

  /**
   * Default buffer size convert to be more readable for an human
   */
  public static final String LIMIT_SIZE_MSG = humanReadableByteCountSI(LIMIT_FILE_CONTENT_SIZE);

  /**
   * Default use charset of ChatHack protocol
   */
  public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

  private static final String FOX =
      "/\\   /\\   Todd Vargo\n" + "  //\\\\_//\\\\     ____\n" + "  \\_     _/    /   /\n"
          + "   / * * \\    /^^^]\n" + "   \\_\\O/_/    [   ]\n" + "    /   \\_    [   /\n"
          + "    \\     \\_  /  /\n" + "     [ [ /  \\/ _/\n" + "    _[ [ \\  /_/";

  private static final String TITLE =
      "_________ .__            __     ___ ___                __    \n"
          + "\\_   ___ \\|  |__ _____ _/  |_  /   |   \\_____    ____ |  | __\n"
          + "/    \\  \\/|  |  \\\\__  \\\\   __\\/    ~    \\__  \\ _/ ___\\|  |/ /\n"
          + "\\     \\___|   Y  \\/ __ \\|  |  \\    Y    // __ \\\\  \\___|    < \n"
          + " \\______  /___|  (____  /__|   \\___|_  /(____  /\\___  >__|_ \\\n"
          + "        \\/     \\/     \\/             \\/      \\/     \\/     \\/";


  /**
   * Message of available command of a client
   */
  public static final String COMMAND_LIST = "\nCommands list: \n" + "public message => no prefix\n"
      + "@login msg => private message\n" + "/requests => list private connection\n"
      + "/accept login => accept private connection\n"
      + "/reject login => reject private connection\n" + "/file login filename (max file size "
      + LIMIT_SIZE_MSG + ") => send file\n" + "/logout => disconnection from server\n"
      + "/close login => close private connection between client\n"
      + "/help => display command list\n";



  /**
   * Message to be display to a new conencted client
   */
  public static final String WELCOME_MESSAGE = FOX + "\n" + TITLE + "\n" + COMMAND_LIST;


  /**
   * Max value of port
   */
  public static final int MAX_PORT_VALUE = 65535;

  /**
   * Clone a given buffer
   * 
   * @param bb: buffer to be clone
   * @return: cloned buffer
   */
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

  /**
   * Convert a byte value to be more readable for an human
   * 
   * @param byte: byte value
   * @return: a {@link String} of given value
   */
  public static String humanReadableByteCountSI(long bytes) {
    if (-1000 < bytes && bytes < 1000) {
      return bytes + " B";
    }
    CharacterIterator ci = new StringCharacterIterator("kMGTPE");
    while (bytes <= -999_950 || bytes >= 999_950) {
      bytes /= 1000;
      ci.next();
    }
    return String.format("%.1f %cB", bytes / 1000.0, ci.current());
  }
}
