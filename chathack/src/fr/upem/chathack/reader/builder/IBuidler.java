package fr.upem.chathack.reader.builder;

import fr.upem.chathack.reader.IReader;

public interface IBuidler<T> {
  IReader<T> build();
}
