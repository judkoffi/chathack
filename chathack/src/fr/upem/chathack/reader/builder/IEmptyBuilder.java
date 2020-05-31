package fr.upem.chathack.reader.builder;

import fr.upem.chathack.reader.IReader;

/**
 * Interface use to add different component of a reader which will build
 * 
 * @param <T>: Type of value returned by build reader
 */
public interface IEmptyBuilder<T> {
  <V> ISubBuilder<T> addSubReader(IReader<V> reader);
}
