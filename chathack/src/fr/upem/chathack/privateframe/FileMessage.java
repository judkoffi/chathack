package fr.upem.chathack.privateframe;

import java.nio.ByteBuffer;
import java.util.List;
import fr.upem.chathack.frame.IPrivateFrame;
import fr.upem.chathack.model.LongSizedString;
import fr.upem.chathack.reader.builder.Box;
import fr.upem.chathack.utils.OpCode;
import fr.upem.chathack.visitor.IPrivateFrameVisitor;

/**
 * Class use to represent a frame contains file to be send between client
 */
public class FileMessage implements IPrivateFrame {
  private final LongSizedString filename;
  private final LongSizedString destinator;
  private ByteBuffer content;

  public FileMessage(LongSizedString filename, LongSizedString destinator, ByteBuffer content) {
    this.filename = filename;
    this.destinator = destinator;
    this.content = content;
  }

  public FileMessage(String filename, String destinator, ByteBuffer buffer) {
    this.filename = new LongSizedString(filename);
    this.destinator = new LongSizedString(destinator);
    this.content = buffer;
  }

  public static FileMessage of(List<Box<?>> params) {
    if (params.size() != 3) {
      throw new IllegalArgumentException(params + " size is invalid");
    }

    var dest = (LongSizedString) params.get(0).getBoxedValue();
    var filename = (LongSizedString) params.get(1).getBoxedValue();
    var buffer = (ByteBuffer) params.get(2).getBoxedValue();
    return new FileMessage(filename, dest, buffer);
  }

  @Override
  public ByteBuffer toBuffer() {
    var size = Byte.BYTES + filename.getTrameSize() + destinator.getTrameSize() + Integer.BYTES
        + content.limit();
    var bb = ByteBuffer.allocate((int) size);
    bb.put(OpCode.FILE_SEND);
    bb.put(destinator.toBuffer());
    bb.put(filename.toBuffer());
    bb.putInt(content.limit());
    bb.put(content);
    return bb.flip();
  }

  @Override
  public void accept(IPrivateFrameVisitor frameVisitor) {
    frameVisitor.visit(this);
  }

  public String getFilename() {
    return filename.getValue();
  }

  public ByteBuffer getContent() {
    return content;
  }

  @Override
  public String toString() {
    return "file: " + filename;
  }
}
