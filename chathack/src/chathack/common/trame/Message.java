package chathack.common.trame;

import static chathack.common.Helper.DEFAULT_CHARSET;
import java.nio.ByteBuffer;
import chathack.common.Helper;
import chathack.common.OpCode;

public class Message {
  final OpCode opcode;
  final String login;
  final String value;
  final ByteBuffer bb;

  public Message(OpCode opcode, String login, String value) {
    this.opcode = opcode;
    this.login = login;
    this.value = value;
    this.bb = ByteBuffer.allocate(Helper.BUFFER_SIZE);
    fillBuffer();
  }

  private void fillBuffer() {
    ByteBuffer loginBuffer = DEFAULT_CHARSET.encode(login);
    ByteBuffer messageBuffer = DEFAULT_CHARSET.encode(value);
    bb.put(Helper.opcodeToByte(opcode));
    bb.putInt(loginBuffer.limit());
    bb.put(loginBuffer);
    bb.putInt(messageBuffer.limit());
    bb.put(messageBuffer);
  }

  public ByteBuffer toBuffer() {
    return bb.duplicate().flip();
  }

  public String getLogin() {
    return login;
  }

  public String getValue() {
    return value;
  }

  public int getTotalSize() {
    return bb.limit();
  }

  public OpCode getOpcode() {
    return opcode;
  }

  @Override
  public String toString() {
    return "[" + login + "] >> " + value + " ----> size: " + getTotalSize();
  }

}
