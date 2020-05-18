package fr.upem.chathack.common.model;

import java.nio.ByteBuffer;

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
  public static final byte DB_VALID_RESPONSE = ((byte) 1);
  public static final byte DB_INVALID_RESPONSE = ((byte) 0);
}
