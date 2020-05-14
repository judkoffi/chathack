package chathack.common.model;

import static chathack.utils.Helper.DEFAULT_CHARSET;
import java.nio.ByteBuffer;
import chathack.utils.Helper;

public class Message {
  final String from;
  final String content;
  // final ByteBuffer bb;

  public Message(String login, String value) {
    this.from = login;
    this.content = value;
    // this.bb = ByteBuffer.allocate(Helper.BUFFER_SIZE);
    fillBuffer();
  }

  private void fillBuffer() {
    // var bb = ByteBuffer.allocate(Helper.BUFFER_SIZE);
    // ByteBuffer loginBuffer = DEFAULT_CHARSET.encode(from);
    // ByteBuffer messageBuffer = DEFAULT_CHARSET.encode(content);
    // bb.putInt(loginBuffer.limit());
    // bb.put(loginBuffer);
    // bb.putInt(messageBuffer.limit());
    // bb.put(messageBuffer);
    // bb.flip();
  }

  public ByteBuffer toBuffer() {
    var bb = ByteBuffer.allocate(Helper.BUFFER_SIZE);
    ByteBuffer loginBuffer = DEFAULT_CHARSET.encode(from);
    ByteBuffer messageBuffer = DEFAULT_CHARSET.encode(content);
    bb.putInt(loginBuffer.limit());
    bb.put(loginBuffer);
    bb.putInt(messageBuffer.limit());
    bb.put(messageBuffer);
    return bb.flip();
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
