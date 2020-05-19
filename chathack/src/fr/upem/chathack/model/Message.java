package fr.upem.chathack.model;

import java.nio.ByteBuffer;

public class Message {
  private final LongSizedString from;
  private final LongSizedString content;

  public Message(LongSizedString login, LongSizedString value) {
    this.from = login;
    this.content = value;
  }

  public Message(String login, String value) {
    this.from = new LongSizedString(login);
    this.content = new LongSizedString(value);
  }

  public ByteBuffer toBuffer() {
    var size = from.getTrameSize() + content.getTrameSize();
    var bb = ByteBuffer.allocate((int) size);
    bb.put(from.toBuffer());
    bb.put(content.toBuffer());
    return bb.flip();
  }

  public LongSizedString getFrom() {
    return from;
  }

  public long getTrameSize() {
    return (from.getTrameSize() + content.getTrameSize());
  }

  @Override
  public String toString() {
    return "[" + from + "] >> " + content;
  }
}
