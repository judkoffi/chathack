package fr.upem.chathack.reader.builder;

/**
 * Interface use to add method to create retrieve value of builded reader
 */
public interface ISubBuilder<T> extends IEmptyBuilder<T> {
  /**
   * 
   * @param constructor: method use to construct instance of message reader by current building
   *        reader
   * @return: current {@link ReaderBuilder} after add producer method which will use to produce
   *          message read by reader
   */
  IBuilder<T> addConstructor(IConstructor<T> constructor);
}
