package fr.upem.chathack.frame;

public interface IDatabaseFrame extends IFrame {
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

}
