package fr.upem.chathack.model;

import static fr.upem.chathack.utils.Helper.DEFAULT_CHARSET;
import static fr.upem.chathack.utils.Helper.cloneByteBuffer;
import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * 
 * Model class use to represent a frame contain a string with this string size
 */
public class LongSizedString {
  private final long size;
  private final String content;
  private final ByteBuffer contentBuffer;

  public LongSizedString(String content) {
    this.content = content;
    this.contentBuffer = DEFAULT_CHARSET.encode(content);
    this.size = contentBuffer.limit();
  }

  public ByteBuffer toBuffer() {
    var bb = ByteBuffer.allocate(Long.BYTES + (int) size);
    bb.putLong(size);
    bb.put(cloneByteBuffer(contentBuffer));//
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

  public long getContentSize() {
    return contentBuffer.limit();
  }

  /**
   * use to have sum of size of buffer and a long prefixed length
   * 
   * @return a long with represent sum of Long.BYTES and content buffer size
   */
  public long getTrameSize() {
    return (Long.BYTES + size);
  }

  public String getValue() {
    return content;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof LongSizedString))
      return false;

    LongSizedString r = (LongSizedString) obj;
    return r.size == size && r.content.equals(content);
  }

  @Override
  public int hashCode() {
    return Objects.hash(size, content);
  }

  @Override
  public String toString() {
    return content;
  }
}
