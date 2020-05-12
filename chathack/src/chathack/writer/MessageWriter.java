package chathack.writer;

import java.nio.ByteBuffer;
import chathack.common.trame.Message;

public class MessageWriter implements IWriter<Message> {
  private final StringWriter writer = new StringWriter();

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
