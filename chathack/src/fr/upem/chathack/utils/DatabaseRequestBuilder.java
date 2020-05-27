package fr.upem.chathack.utils;

import java.nio.ByteBuffer;
import fr.upem.chathack.model.LongSizedString;
import fr.upem.chathack.model.OpCode;
import fr.upem.chathack.publicframe.AuthentificatedConnection;

/**
 * Helper class use to build frame use to send request to database server
 */
public class DatabaseRequestBuilder {
  private DatabaseRequestBuilder() {}

  /***
   * Pour demander, si une paire login/mot de passe est correcte, le client envoie:<br>
   * 
   * ----------------------------------------------------------------------------------------- <br>
   * 1 (BYTE) | id (LONG) | login size (INT) login (STRING) | pass size (INT) | pass (STRING) |<br>
   * -----------------------------------------------------------------------------------------<br>
   *
   * Good credential <br>
   * --------------------<br>
   * 1 (BYTE) | id (LONG)<br>
   * --------------------<br>
   * 
   * Wrong credential <br>
   * ---------------------<br>
   * 0 (BYTE) id (LONG)<br>
   * ---------------------<br>
   * 
   * Pour demander, si un login est dans la base de donnée, le client envoie:<br>
   * ---------------------------------------------------------<br>
   * 2 (BYTE) | id (LONG) | login size (INT) | login (STRING) |<br>
   * ---------------------------------------------------------<br>
   * avec
   * 
   * id est un LONG en BigEndian qui identifie la requête.
   * 
   * Si le login est dans la base de donnée, le serveur renvoie:
   * 
   * 1 (BYTE) id (LONG)
   * 
   * et sinon
   * 
   * 0 (BYTE) id (LONG)
   * 
   */

  public static ByteBuffer checkCredentialsRequest(long id, AuthentificatedConnection message) {
    var s = message.getLogin().getContentSize() + message.getPassword().getContentSize()
        + (Integer.BYTES * 2);
    var requestBuffer = ByteBuffer.allocate((int) (Byte.BYTES + Long.BYTES + s));
    requestBuffer.put(OpCode.ASK_CREDENTIAL);
    requestBuffer.putLong(id);
    requestBuffer.put(message.getLogin().toIntBuffer());
    requestBuffer.put(message.getPassword().toIntBuffer());
    return requestBuffer.flip();
  }

  public static ByteBuffer checkLoginRequest(long id, String login) {
    var s = new LongSizedString(login);
    var bb = ByteBuffer.allocate(Byte.BYTES + Long.BYTES + Long.BYTES + (int) s.getContentSize());
    bb.put(OpCode.ASK_LOGIN_PRESENT);
    bb.putLong(id);
    bb.put(s.toIntBuffer());
    return bb.flip();
  }
}
