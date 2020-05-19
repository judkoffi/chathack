package fr.upem.chathack.privateframe;

import java.nio.ByteBuffer;
import fr.upem.chathack.frame.IPrivateFrame;
import fr.upem.chathack.model.LongSizedString;
import fr.upem.chathack.model.Message;
import fr.upem.chathack.model.OpCode;
import fr.upem.chathack.visitor.IPrivateFrameVisitor;

public class DirectMessage implements IPrivateFrame {
  private final LongSizedString destinator;
  private final Message message;

  public DirectMessage(String fromLogin, String targetLogin, String message) {
    this.destinator = new LongSizedString(targetLogin);
    this.message = new Message(fromLogin, message);
  }

  public DirectMessage(LongSizedString target, Message content) {
    this.destinator = target;
    this.message = content;
  }

  @Override
  public ByteBuffer toBuffer() {
    var size = Byte.BYTES + message.getTrameSize() + destinator.getTrameSize();
    var bb = ByteBuffer.allocate((int) size);
    bb.put(OpCode.DIRECT_MESSAGE);
    bb.put(destinator.toBuffer());
    bb.put(message.toBuffer());
    return bb.flip();
  }

  public String getDestinator() {
    return destinator.getValue();
  }

  @Override
  public void accept(IPrivateFrameVisitor frameVisitor) {
    frameVisitor.visit(this);
  }

  @Override
  public String toString() {
    return "from: " + destinator + " " + message;
  }
}
