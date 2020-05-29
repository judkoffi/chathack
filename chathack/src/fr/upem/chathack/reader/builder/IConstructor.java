package fr.upem.chathack.reader.builder;

import java.util.List;

@FunctionalInterface
public interface IConstructor<T> {
  T get(List<Box<?>> parameters);
}
