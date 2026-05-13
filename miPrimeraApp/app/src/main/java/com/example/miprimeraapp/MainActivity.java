package com.example.miprimeraapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DatabaseReference;

public class MainActivity extends Activity {
    FloatingActionButton fab;
    Button btn;
    TextView tempVal;
    String accion="", idAmigo="", id="", rev="";
    ImageView img;
    String urlCompletaFoto="", getUrlCompletaFotoFireStore="";
    Intent tomarFotoIntent;
    detectarinternet di;
    DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

}
