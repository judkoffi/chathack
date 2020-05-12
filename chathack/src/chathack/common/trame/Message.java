package chathack.common.trame;

import static chathack.common.Helper.DEFAULT_CHARSET;
import java.nio.ByteBuffer;
import chathack.common.Helper;

public class Message {
  final String login;
  final String value;
  final ByteBuffer bb;

  public Message(String login, String value) {
    this.login = login;
    this.value = value;
    this.bb = ByteBuffer.allocate(Helper.BUFFER_SIZE);
    fillBuffer();
  }

  private void fillBuffer() {
    ByteBuffer loginBuffer = DEFAULT_CHARSET.encode(login);
    ByteBuffer messageBuffer = DEFAULT_CHARSET.encode(value);
    bb.putInt(loginBuffer.limit());
    bb.put(loginBuffer);
    bb.putInt(messageBuffer.limit());
    bb.put(messageBuffer);
  }

  public ByteBuffer toBuffer() {
    return bb.duplicate().flip();
  }

  public int getTotalSize() {
    return bb.limit();
  }

  @Override
  public String toString() {
    return "[" + login + "] >> " + value + " ----> size: " + getTotalSize();
  }

}
