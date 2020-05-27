package fr.upem.chathack.model;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import fr.upem.chathack.client.PrivateConnectionContext;

/**
 * 
 * Class use to contains some informations about a private information between two clients
 */
public class PrivateConnectionInfo {

  public enum PrivateConnectionState {
    PENDING, WAITING_COMFIRM_TOKEN, SUCCEED
  }

  private String receiver;
  private PrivateConnectionState state;
  private long token;
  private PrivateConnectionContext destinatorContext;
  private ArrayDeque<ByteBuffer> pendingDirectMessages;

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

  public void setToken(long token) {
    this.token = token;
  }

  public void setState(PrivateConnectionState state) {
    this.state = state;
  }

  public String getReceiver() {
    return receiver;
  }

  public long getToken() {
    return token;
  }

  public PrivateConnectionState getState() {
    return state;
  }

  public void setDestinatorContext(PrivateConnectionContext destinatorContext) {
    this.destinatorContext = destinatorContext;
  }

  public PrivateConnectionContext getDestinatorContext() {
    return destinatorContext;
  }

  public Deque<ByteBuffer> getMessageQueue() {
    return pendingDirectMessages;
  }

  @Override
  public String toString() {
    return "private connect with: " + receiver + " token: " + token + "; state: " + state
        + "dest ctx " + destinatorContext;
  }

}
