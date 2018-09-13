KIE Server Router
==========================

## TSL support
To start the router with TSL support, start it with following command: 
```
java -Dorg.kie.server.router.tls.keystore=PATH_TO_YOUR_KEYSTORE 
     -Dorg.kie.server.router.tls.keystore.password=YOUR_KEYSTORE_PASSWD 
     -Dorg.kie.server.router.tls.keystore.keyalias=YOUR_KEYSTORE_ALIAS 
      -jar kie-server-router-proxy-YOUR_VERSION.jar
```
And replace PATH_TO_YOUR_KEYSTORE YOUR_KEYSTORE_PASSWD YOUR_KEYSTORE_ALIAS and VERSION according to 
what you are using.