package fr.upem.chathack.common.model;

import static fr.upem.chathack.utils.Helper.DEFAULT_CHARSET;
import java.nio.ByteBuffer;

public class LongSizedString {
  final long size;
  final String content;
  final ByteBuffer contentBuffer;

  public LongSizedString(String content) {
    this.content = content;
    this.contentBuffer = DEFAULT_CHARSET.encode(content);
    this.size = contentBuffer.limit();
  }

  public ByteBuffer toBuffer() {
    var bb = ByteBuffer.allocate(Long.BYTES + (int) size);
    bb.putLong(size);
    bb.put(contentBuffer.duplicate());
    return bb.flip();
  }

  /*
   * For DB requests
   */
  public ByteBuffer toIntBuffer() {
    var bb = ByteBuffer.allocate(Integer.BYTES + (int) size);
    bb.putInt((int) size);
    bb.put(contentBuffer);
    return bb.flip();
  }

  public long getSize() {
    return size;
  }

  public String getValue() {
    return content;
  }

  @Override
  public String toString() {
    return content + "[" + size + "]";
  }
}
