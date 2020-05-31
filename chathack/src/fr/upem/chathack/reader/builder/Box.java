package fr.upem.chathack.reader.builder;
/**
 * Class used to stock values get from reader
 *
 * @param <E>
 */
public class Box<E> {
  final E value;

  private Box(E value) {
    this.value = value;
  }

  public static <E> Box<E> of(E value) {
    return new Box<>(value);
  }

  public E getBoxedValue() {
    return value;
  }
}
