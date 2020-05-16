package fr.upem.chathack.common.model;

import java.nio.ByteBuffer;

public class Message {
  final LongSizedString from;
  final LongSizedString content;

  public Message(LongSizedString login, LongSizedString value) {
    this.from = login;
    this.content = value;
  }

  public ByteBuffer toBuffer() {
    var size = Byte.BYTES + 2 * Long.BYTES + (int) from.getSize() + (int) content.getSize();
    var bb = ByteBuffer.allocate(size);
    bb.put(OpCode.BROADCAST_MESSAGE);
    bb.put(from.toBuffer());
    bb.put(content.toBuffer());
    return bb.flip();
  }

  @Override
  public String toString() {
    return "[" + from + "] >> " + content;
  }

}
