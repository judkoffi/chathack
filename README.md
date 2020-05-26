# chathack

## documentations
### Dans koffi-nguyen/
``` 
- RFC revision 1 ---> christelle_nguyen_judicael_koffi.pdf
- RFC revision 2 ---> rfc-v2.pdf
``` 

## code source
```
Tout ce qu'il y a sous chathack/src/
```

## commandes
### Dans chathack/data/
```    
java -jar ServerMDP.jar 8888 password.txt ---> pour lancer la base de données
```       

### Dans chathack/bin/
```   
java fr.upem.chathack.server.ServerChatHack 7777 localhost 8888 --->le server    
java fr.upem.chathack.client.ClientChatHack localhost 7777 joey-dir/ joey ---> client 1 
java fr.upem.chathack.client.ClientChatHack localhost 7777 joeyette-dir/ joeyette ---> client 2  
```    
- TODO : checker les entiers passes dans les buffers dans les Frames 
