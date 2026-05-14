# API PHP para GoogieHost

Sube esta carpeta como `public_html/api` en DirectAdmin.

Prueba la conexión abriendo:

```text
https://estacioanmiento.whf.bz/api/ping.php
```

Si responde con `"ok": true`, PHP ya puede conectarse a MySQL desde el hosting.

Nota: en GoogieHost normalmente MySQL se usa como `localhost` desde PHP. Si falla, revisa en DirectAdmin o phpMyAdmin si el host interno de MySQL es distinto.
