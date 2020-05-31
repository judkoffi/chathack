package fr.upem.chathack.reader.builder;

import fr.upem.chathack.reader.IReader;
/**
 * Interface used to build the final reader by compacting many readers
 *
 * @param <T>
 */
public interface IBuilder<T> {
  IReader<T> build();
}
