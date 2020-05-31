package fr.upem.chathack.reader.builder;

/**
 * Class used to stock values get from reader
 *
 * @param <E>: Type of value store in a box
 */
public class Box<E> {
  final E value;

  private Box(E value) {
    this.value = value;
  }

  /**
   * 
   * @param <E>: type of value to be box
   * @param value: value to be boc
   * @return: a instance of {@link Box}
   */
  public static <E> Box<E> of(E value) {
    return new Box<>(value);
  }

  /**
   * Getter of boxed value
   * 
   * @return: value boxed
   */
  public E getBoxedValue() {
    return value;
  }
}
