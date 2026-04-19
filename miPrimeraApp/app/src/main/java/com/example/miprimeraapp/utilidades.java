package com.example.miprimeraapp;

import android.util.Base64;

public class utilidades {
    // Cambia 192.168.1.15 por la IP que te dio el comando ipconfig
    static String url_consulta = "http://192.168.101.18:5984/productos/_design/productos/_view/todos";
    static String url_mto = "http://192.168.101.18:5984/productos";
    static String user = "steven";
    static String passwd = "200612";
    static String credencialesCodificadas = Base64.encodeToString((user + ":" + passwd).getBytes(), Base64.NO_WRAP);

    public String generarUnicoId() {
        return java.util.UUID.randomUUID().toString();
    }
}
