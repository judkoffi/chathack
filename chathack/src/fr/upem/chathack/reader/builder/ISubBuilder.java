package fr.upem.chathack.reader.builder;

public interface ISubBuilder<T> extends IEmptyBuilder<T> {
  IBuidler<T> addConstructor(IConstructor<T> constructor);
}
