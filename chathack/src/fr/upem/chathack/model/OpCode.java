package fr.upem.chathack.model;

public class OpCode {
  private OpCode() {}

  public static final byte DISCOVER_MESSAGE = (byte) 20;
  public static final byte ANONYMOUS_CLIENT_CONNECTION = (byte) 0;
  public static final byte AUTHENTICATED_CLIENT_CONNECTION = (byte) 1;
  public static final byte BROADCAST_MESSAGE = (byte) 2;
  public static final byte REQUEST_PRIVATE_CLIENT_CONNECTION = (byte) 3;
  public static final byte SUCCEDED_PRIVATE_CLIENT_CONNECTION = (byte) 4;
  public static final byte DECONNECTION_WITH_SERVER = (byte) 5;

  public static final byte DIRECT_MESSAGE = (byte) 8;
  public static final byte FILE_SEND = (byte) 9;

  public static final byte REJECTED_PRIVATE_CLIENT_CONNECTION = (byte) -4;

  /********************************
   * ServerRequest Opcode
   *******************************/



  /********************************
   * ServerResponse Opcode
   *******************************/
  public static final byte SERVER_ERROR_RESPONSE_TYPE = (byte) -1;
  public static final byte SERVER_NOT_ERROR_RESPONSE_TYPE = (byte) 1;

  public static final byte SERVER_RESPONSE_MESSAGE = (byte) 15;


  /********************************
   * DBRequest Opcode
   *******************************/
  public static final byte ASK_CREDENTIAL = ((byte) 1);
  public static final byte ASK_LOGIN_PRESENT = ((byte) 2);


  /********************************
   * DBResponse Opcode
   *******************************/
  public static final byte DB_VALID_RESPONSE = (byte) 1;
  public static final byte DB_INVALID_RESPONSE = (byte) 0;
}
