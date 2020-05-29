package fr.upem.chathack.server;

import java.nio.channels.SelectionKey;
import java.util.Objects;

/**
 * Internal class use to keep some information about connected client
 */
public class ClientInfo {
  boolean isAuthenticated;
  boolean anonymous; // type of connection (anonymous or with credentials)
  SelectionKey key;
  long id;
  ServerContext context;

  ClientInfo(boolean anonymous, boolean isAuthenticated, SelectionKey key, long id) {
    this.anonymous = Objects.requireNonNull(anonymous);
    this.isAuthenticated = Objects.requireNonNull(isAuthenticated);
    this.key = Objects.requireNonNull(key);
    this.id = Objects.requireNonNull(id);
  }

  ClientInfo(boolean anonymous, SelectionKey key, long id) {
    this(anonymous, false, key, id);
  }

  @Override
  public String toString() {
    return "id: " + id;
  }
}
