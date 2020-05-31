package fr.upem.chathack.reader.builder;

public interface ISubBuilder<T> extends IEmptyBuilder<T> {
  IBuilder<T> addConstructor(IConstructor<T> constructor);
}
