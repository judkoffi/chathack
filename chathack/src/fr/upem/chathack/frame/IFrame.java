package fr.upem.chathack.frame;

import java.nio.ByteBuffer;

/**
 * Interface use to represent common type for all frame using during with chaton protocol
 */
public interface IFrame {
  public ByteBuffer toBuffer();
}
