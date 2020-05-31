package fr.upem.chathack.reader.builder;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import fr.upem.chathack.reader.IReader;
import fr.upem.chathack.reader.IReader.ProcessStatus;

/**
 * Class use to building a complex reader by add some basic readers
 * 
 * @param <T>: Type of target reader value
 */
public class ReaderBuilder<T> implements IEmptyBuilder<T>, ISubBuilder<T>, IBuilder<T> {

  private enum State {
    PROCESSING, DONE, ERROR
  }

  private static class SubReader<V> {
    private final IReader<V> reader;
    private V value;

    public SubReader(IReader<V> reader) {
      this.reader = reader;
    }

    IReader.ProcessStatus process(ByteBuffer bb) {
      var status = reader.process(bb);
      if (status != ProcessStatus.DONE)
        return status;
      value = reader.get();
      reader.reset();
      return ProcessStatus.DONE;
    }
  }

  private IConstructor<T> constructor;
  private final ArrayList<SubReader<?>> subReaders = new ArrayList<>();

  public static <T> ReaderBuilder<T> create() {
    return new ReaderBuilder<>();
  }

  @Override
  public IBuilder<T> addConstructor(IConstructor<T> constructor) {
    this.constructor = constructor;
    return this;
  }

  @Override
  public <V> ISubBuilder<T> addSubReader(IReader<V> reader) {
    subReaders.add(new SubReader<V>(reader));
    return this;
  }


  @Override
  public IReader<T> build() {
    return new IReader<T>() {
      private State state = State.PROCESSING;
      private int size = subReaders.size();
      private int index = 0;

      @Override
      public ProcessStatus process(ByteBuffer bb) {
        if (state == State.ERROR || index >= size) {
          throw new IllegalStateException();
        }

        while (index < size) {
          var status = subReaders.get(index).process(bb);
          switch (status) {
            case DONE:
              break;
            case ERROR:
              state = State.ERROR;
              return status;
            case REFILL:
              return status;
            default:
              throw new AssertionError();
          }
          index++;
        }
        state = State.DONE;
        return ProcessStatus.DONE;
      }

      @Override
      public T get() {
        if (state != State.DONE) {
          throw new IllegalStateException();
        }
        var array = new ArrayList<Box<?>>();
        subReaders.forEach(p -> array.add(Box.of(p.value)));
        return constructor.get(array);
      }

      @Override
      public void reset() {
        index = 0;
        state = State.PROCESSING;
      }
    };
  }
}
