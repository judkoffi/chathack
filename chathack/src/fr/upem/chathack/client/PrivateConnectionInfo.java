package fr.upem.chathack.client;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;

/**
 * 
 * Class use to contains some informations about a private information between two clients
 */
public class PrivateConnectionInfo {

  enum PrivateConnectionState {
    PENDING, WAITING_COMFIRM_TOKEN, SUCCEED
  }

  String receiver;
  PrivateConnectionState state;
  long token;
  PrivateConnectionContext destinatorContext;
  ArrayDeque<ByteBuffer> pendingDirectMessages;

  public PrivateConnectionInfo(String receiver) {
    this.receiver = receiver;
    this.token = -1;
    this.state = PrivateConnectionState.PENDING;
    this.pendingDirectMessages = new ArrayDeque<>();
  }

  public PrivateConnectionInfo(String receiver, PrivateConnectionState state, long token) {
    this.receiver = receiver;
    this.token = token;
    this.state = state;
    this.pendingDirectMessages = new ArrayDeque<>();
  }

  @Override
  public String toString() {
    return "private connect with: " + receiver + " token: " + token + "; state: " + state
        + "dest ctx " + destinatorContext;
  }

}
