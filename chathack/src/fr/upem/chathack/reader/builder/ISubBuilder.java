package fr.upem.chathack.reader.builder;

/**
 * Interface use to add method to create retrieve value of builded reader
 */
public interface ISubBuilder<T> extends IEmptyBuilder<T> {
  IBuilder<T> addConstructor(IConstructor<T> constructor);
}
