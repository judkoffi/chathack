package fr.upem.chathack.context;

public class PrivateConnectionInfo {
  private PrivateConnectionContext ctx;

  public PrivateConnectionInfo(PrivateConnectionContext ctx) {
    this.ctx = ctx;
  }

  public PrivateConnectionContext getContext() {
    return ctx;
  }
}
