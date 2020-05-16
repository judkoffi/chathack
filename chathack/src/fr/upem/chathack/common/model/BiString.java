package fr.upem.chathack.common.model;

import static fr.upem.chathack.utils.Helper.DEFAULT_CHARSET;
import java.nio.ByteBuffer;
import fr.upem.chathack.utils.Helper;

public class BiString {
  final String from;
  final String content;
  final ByteBuffer bb;

  public BiString(String login, String value) {
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
  }

  public ByteBuffer toBuffer() {
    return bb.duplicate().flip();
  }

  public String getLogin() {
    return from;
  }

  public String getValue() {
    return content;
  }


  @Override
  public String toString() {
    return "[" + from + "] >> " + content;
  }

}
