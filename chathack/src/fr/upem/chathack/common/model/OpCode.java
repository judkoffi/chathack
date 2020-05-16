package fr.upem.chathack.common.model;



public class OpCode {
  private OpCode() {}

  public static final byte ANONYMOUS_CLIENT_CONNECTION = (byte) 0;
  public static final byte AUTHENTICATED_CLIENT_CONNECTION = (byte) 1;
  public static final byte BROADCAST_MESSAGE = (byte) 2;
  public static final byte REQUEST_PRIVATE_CLIENT_CONNECTION = (byte) 4;
  public static final byte SERVER_NOTIFY_PRIVATE_CLIENT_CONNECTION = (byte) 5;
  public static final byte CLIENT_FAILED_PRIVATE_CLIENT_CONNECTION = (byte) 6;
  public static final byte SUCCEDED_PRIVATE_CLIENT_CONNECTION = (byte) 7;
  public static final byte PRIVATE_MESSAGE = (byte) 8;



  /********************************
   * ServerRequest Opcode
   *******************************/



  /********************************
   * ServerResponse Opcode
   *******************************/
  public static final byte ERROR_MESSAGE = (byte) -1;


  /********************************
   * DBRequest Opcode
   *******************************/
  public static final byte ASK_CREDENTIAL = ((byte) 1);
  public static final byte ASK_LOGIN_PRESENT = ((byte) 2);


  /********************************
   * DBResponse Opcode
   *******************************/
  public static final byte GOOD_CREDENTIAL = ((byte) 1);
  public static final byte BAD_CREDENTIAL = ((byte) 0);
}
