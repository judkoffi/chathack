package fr.upem.chathack.reader.builder;

import java.util.List;

/**
 * Interface used to build frame with what is given in argument
 *
 * @param <T>
 */
@FunctionalInterface
public interface IConstructor<T> {
  /**
   * 
   * @param parameters: a list of value to use to construct a value
   * @return: an instance of T
   */
  T get(List<Box<?>> parameters);
}
