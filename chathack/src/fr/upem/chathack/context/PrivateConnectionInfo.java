package fr.upem.chathack.context;

public class PrivateConnectionInfo {
  private PrivateConnectionContext ctx;
  private final long token;

  public PrivateConnectionInfo(PrivateConnectionContext ctx, long token) {
    this.ctx = ctx;
    this.token = token;
  }

  public PrivateConnectionContext getContext() {
    return ctx;
  }

  public long getToken() {
    return token;
  }
}
