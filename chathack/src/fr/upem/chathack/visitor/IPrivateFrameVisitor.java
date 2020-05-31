package fr.upem.chathack.visitor;

import fr.upem.chathack.privateframe.ClosePrivateConnectionMessage;
import fr.upem.chathack.privateframe.ConfirmDiscoverMessage;
import fr.upem.chathack.privateframe.DirectMessage;
import fr.upem.chathack.privateframe.DiscoverMessage;
import fr.upem.chathack.privateframe.FileMessage;


/**
 * Interface use to contains visit methods of all private frame
 */
public interface IPrivateFrameVisitor extends IFrameVisitor {

  /**
   * method use to handle direct message received in private connection between client
   * 
   * @param directMessage: {@link DirectMessage} represent received private message
   */
  public void visit(DirectMessage directMessage);

  /**
   * method use to handle a discover message received in private connection between client
   * 
   * @param message: {@link DiscoverMessage} represent received discover message
   */
  public void visit(DiscoverMessage message);

  /**
   * method use to handle a discover confirmation message received in private connection between
   * client
   * 
   * @param confirmDiscoverMessage: a {@link ConfirmDiscoverMessage} represent received confirmation
   *        of token
   */
  public void visit(ConfirmDiscoverMessage confirmDiscoverMessage);

  /**
   * method use to handle a file message received in private connection between client
   * 
   * @param fileMessage:a {@link FileMessage} represent a received file frame
   */
  public void visit(FileMessage fileMessage);



  /**
   * method use to handle a private connection close message received in private connection between
   * client
   * 
   * @param closePrivateConnectionMessage:a {@link ClosePrivateConnectionMessage} represent a
   *        received confirmation close
   */
  public void visit(ClosePrivateConnectionMessage closePrivateConnectionMessage);

}
