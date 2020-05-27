package fr.upem.chathack.visitor;

import fr.upem.chathack.publicframe.AcceptPrivateConnection;
import fr.upem.chathack.publicframe.AnonymousConnection;
import fr.upem.chathack.publicframe.AuthentificatedConnection;
import fr.upem.chathack.publicframe.BroadcastMessage;
import fr.upem.chathack.publicframe.LogOutMessage;
import fr.upem.chathack.publicframe.RejectPrivateConnection;
import fr.upem.chathack.publicframe.RequestPrivateConnection;
import fr.upem.chathack.publicframe.ServerResponseMessage;

/**
 * Interface use to contains visit methods of all public frame
 */
public interface IPublicFrameVisitor extends IFrameVisitor {

  /**
   * Method use to handle a anonymous client connection message received by server
   * 
   * @param message: a {@link AnonymousConnection} object
   */
  public default void visit(AnonymousConnection message) {}

  /**
   * Method use to handle a authenticated client connection message received by server
   * 
   * @param message: a {@link AuthentificatedConnection} object
   */
  public default void visit(AuthentificatedConnection message) {}

  /**
   * Method use to handle a public message from a client received by server
   * 
   * @param message: a {@link BroadcastMessage} object
   */
  public void visit(BroadcastMessage message);

  /**
   * Method use to handle a message send by server to a client
   * 
   * @param message: a {@link ServerResponseMessage} object
   */
  public default void visit(ServerResponseMessage serverMessage) {}

  /**
   * Method use to handle a private connection between client request message by server or a client
   * 
   * @param message: a {@link RequestPrivateConnection} object
   */
  public void visit(RequestPrivateConnection requestMessage);

  /**
   * Method use to handle a positive response about a private connection request initiate by a
   * client
   * 
   * @param message: a {@link AcceptPrivateConnection} object
   */
  public void visit(AcceptPrivateConnection responsePrivateConnection);

  /**
   * Method use to handle a negative response about a private connection request initiate by a
   * client
   * 
   * @param message: a {@link RejectPrivateConnection} object
   */
  public void visit(RejectPrivateConnection rejectPrivateConnection);

  /**
   * Method use to handle a disconnection of a client from server
   * 
   * @param message: a {@link LogOutMessage} object
   */
  public void visit(LogOutMessage disconnectionMessage);

}
