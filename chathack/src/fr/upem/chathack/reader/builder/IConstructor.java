package fr.upem.chathack.reader.builder;

import java.util.List;
/**
 * Interface used to build frame with what is given in argument
 *
 * @param <T>
 */
@FunctionalInterface
public interface IConstructor<T> {
  T get(List<Box<?>> parameters);
}
