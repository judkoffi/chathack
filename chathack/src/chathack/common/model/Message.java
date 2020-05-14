package chathack.common.model;

import static chathack.utils.Helper.DEFAULT_CHARSET;
import java.nio.ByteBuffer;
import chathack.utils.Helper;

public class Message {
  final String from;
  final String content;
  final ByteBuffer bb;

  public Message(String login, String value) {
    this.from = login;
    this.content = value;
    this.bb = ByteBuffer.allocate(Helper.BUFFER_SIZE);
    fillBuffer();
  }

  private void fillBuffer() {
    ByteBuffer loginBuffer = DEFAULT_CHARSET.encode(from);
    ByteBuffer messageBuffer = DEFAULT_CHARSET.encode(content);
    bb.putInt(loginBuffer.limit());
    bb.put(loginBuffer);
    bb.putInt(messageBuffer.limit());
    bb.put(messageBuffer);
    bb.flip();
  }

  public ByteBuffer toBuffer() {
    return bb.duplicate();
  }

  public String getLogin() {
    return from;
  }

  public String getValue() {
    return content;
  }

  public int getTotalSize() {
    return bb.limit();
  }

  @Override
  public String toString() {
    return "[" + from + "] >> " + content + " ----> size: " + getTotalSize();
  }

}
