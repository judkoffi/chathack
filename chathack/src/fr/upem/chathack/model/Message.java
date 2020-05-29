package fr.upem.chathack.model;

import java.nio.ByteBuffer;
import java.util.List;
import fr.upem.chathack.reader.builder.Box;

/**
 * 
 * Model class use to represent a frame represent a message (sender login and message)
 */
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

  public static Message of(List<Box<?>> params) {
    if (params.size() != 2) {
      throw new IllegalArgumentException(params + " size is invalid");
    }
    var from = (LongSizedString) params.get(0).getBoxedValue();
    var value = (LongSizedString) params.get(1).getBoxedValue();
    return new Message(from, value);
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
