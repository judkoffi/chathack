package fr.upem.chathack.common.reader;

import static fr.upem.chathack.utils.Helper.BUFFER_SIZE;
import java.nio.ByteBuffer;

public class StringLongReader implements IReader<String> {
  private enum State {
    WAITING_SIZE, WAITING_OCTET_CHAINE, DONE, ERROR
  }

  private State state = State.WAITING_SIZE;
  private final LongReader intReader = new LongReader();
  private String value;
  private long size;
  private final ByteBuffer internalbb = ByteBuffer.allocate(BUFFER_SIZE);


  @Override
  public ProcessStatus process(ByteBuffer bb) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public String get() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void reset() {
    // TODO Auto-generated method stub

  }

}
