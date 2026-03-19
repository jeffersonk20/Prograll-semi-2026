package com.example.miprimeraapp;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class lista_amigos extends Activity {
    Bundle parametros = new Bundle();
    DB db;
    FloatingActionButton fab;
    ListView ltsAmigos;
    Cursor cAmigos;
    final ArrayList<amigos> alAmigos = new ArrayList<amigos>();
    final ArrayList<amigos> alAmigosCopia = new ArrayList<amigos>();
    JSONArray jsonArray;
    JSONObject jsonObject;
    int posicion = 0;
    amigos misAmigos;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_amigos);

        parametros.putString("accion","nuevo");
        db=new DB(this);

        fab = findViewById(R.id.fabAgregarAmigos);
        fab.setOnClickListener(v->abrirActivity());

        obtenerAmigos();
    }
    private void abrirActivity(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtras(parametros);
        startActivity(intent);
    }
    private void obtenerAmigos(){
        try{
            cAmigos = db.lista_amigos();
            if( cAmigos.moveToFirst() ){
                jsonArray = new JSONArray();
                do{
                    jsonObject = new JSONObject();
                    jsonObject.put("idAmigo", cAmigos.getString(0));
                    jsonObject.put("nombre", cAmigos.getString(1));
                    jsonObject.put("direccion", cAmigos.getString(2));
                    jsonObject.put("telefono", cAmigos.getString(3));
                    jsonObject.put("email", cAmigos.getString(4));
                    jsonObject.put("dui", cAmigos.getString(5));
                    jsonObject.put("foto", cAmigos.getString(6));
                    jsonArray.put(jsonObject);
                }while (cAmigos.moveToNext());
                mostrarAmigos();
            }else{
                mostrarMsg("No hay amigos que mostrar");
                abrirActivity();
            }
        } catch (Exception e) {
            mostrarMsg(e.getMessage());
        }
    }

    private void mostrarAmigos() {
    }

    private void mostrarMsg(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}