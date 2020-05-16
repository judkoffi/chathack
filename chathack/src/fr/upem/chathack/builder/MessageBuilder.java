package fr.upem.chathack.builder;

import java.nio.ByteBuffer;
import fr.upem.chathack.common.model.Message;

public class MessageBuilder implements IBuilder<Message> {
  private final StringBuilder writer = new StringBuilder();

  @Override
  public ByteBuffer build(Message msg) {
    var login = writer.build(msg.getLogin());
    var value = writer.build(msg.getValue());
    var bb = ByteBuffer.allocate(login.limit() + value.limit());
    bb.put(login);
    bb.put(value);
    return bb;
  }

}
