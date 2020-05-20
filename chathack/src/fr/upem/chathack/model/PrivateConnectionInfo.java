package fr.upem.chathack.model;

import fr.upem.chathack.client.PrivateConnectionContext;

public class PrivateConnectionInfo {

  public enum PrivateConnectionState {
    PENDING, WAITING_COMFIRM_TOKEN, SUCCEED
  }

  private String receiver;
  private PrivateConnectionState state;
  private long token;
  private PrivateConnectionContext destinatorContext;

  public PrivateConnectionInfo(String receiver) {
    this.receiver = receiver;
    this.token = -1;
    this.state = PrivateConnectionState.PENDING;
  }

  public PrivateConnectionInfo(String receiver, PrivateConnectionState state, long token) {
    this.receiver = receiver;
    this.token = token;
    this.state = state;
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

  @Override
  public String toString() {
    return "private connect with: " + receiver + " token: " + token + "; state: " + state;
  }

}
