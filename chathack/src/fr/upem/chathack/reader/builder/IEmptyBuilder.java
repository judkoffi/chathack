package fr.upem.chathack.reader.builder;

import fr.upem.chathack.reader.IReader;

public interface IEmptyBuilder<T> {
  <V> ISubBuilder<T> addSubReader(IReader<V> reader);
}
