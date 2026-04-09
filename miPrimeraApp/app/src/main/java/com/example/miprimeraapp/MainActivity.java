package com.example.miprimeraapp;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {
    DB db;
    Button btn;
    TextView tempVal;
    String accion="nuevo", idAmigo="", urlFoto;
    Intent tomarFotoIntent;
    FloatingActionButton fab;
    ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        img = findViewById(R.id.imgFotoAmigo);
        img.setOnClickListener(v->tomarFoto());

        db = new DB(this);

        btn = findViewById(R.id.btnGuardarAmigo);
        btn.setOnClickListener(v->guardarAmigo());

        fab = findViewById(R.id.fabListaAmigo);
        fab.setOnClickListener(v->regresarListaAmigos());

        mostrarDatosAmigos();
    }
    private void mostrarDatosAmigos(){
        try{
            Bundle parametros = getIntent().getExtras();
            accion = parametros.getString("accion");
            if(accion.equals("modificar")){
                JSONObject datos = new JSONObject(parametros.getString("amigos"));
                idAmigo = datos.getString("idAmigo");

                tempVal = findViewById(R.id.txtNombreAmigos);
                tempVal.setText(datos.getString("nombre"));

                tempVal = findViewById(R.id.txtDireccionAmigos);
                tempVal.setText(datos.getString("direccion"));

                tempVal = findViewById(R.id.txtTelefonoAmigos);
                tempVal.setText(datos.getString("telefono"));

                tempVal = findViewById(R.id.txtEmailAmigos);
                tempVal.setText(datos.getString("email"));

                tempVal = findViewById(R.id.txtDuiAmigos);
                tempVal.setText(datos.getString("dui"));

                urlFoto = datos.getString("foto");
                img.setImageURI(Uri.parse(urlFoto));
            }
        }catch (Exception e){
            mostrarMsg("Error al mostrar los datos: "+ e.getMessage());
        }
    }
    private void tomarFoto(){
        tomarFotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File fotoAmigo = null;

        try{
            fotoAmigo = crearImgAmigo();
            if(fotoAmigo!=null){
                Uri uriFoto = FileProvider.getUriForFile(MainActivity.this, "com.ugb.miprimeraapp.fileprovider", fotoAmigo);
                tomarFotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriFoto);
                startActivityForResult(tomarFotoIntent, 1);
            }else{
                mostrarMsg("Nose pudo crear la foto");
            }
        } catch (Exception e) {
            mostrarMsg("Error al tomar la foto: "+ e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try{
            if(requestCode==1 && resultCode==RESULT_OK){
                img.setImageURI(Uri.parse(urlFoto));
            }else{
                mostrarMsg("No fue posible mostrar la foto");
            }
        } catch (Exception e) {
            mostrarMsg("Error en abrir la camara: "+ e.getMessage());
        }
    }

    private File crearImgAmigo() throws Exception{
        String fechaHoraMs = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()),
                fileMane = "foto_"+ fechaHoraMs;
        File dirAlmacenamiento = getExternalFilesDir(Environment.DIRECTORY_DCIM);
        if(dirAlmacenamiento.exists()==false){
            dirAlmacenamiento.mkdir();
        }
        File image = File.createTempFile(fileMane, ".jpg", dirAlmacenamiento);
        urlFoto = image.getAbsolutePath();
        return image;
    }
    private void guardarAmigo(){
        tempVal = findViewById(R.id.txtNombreAmigos);
        String nombre = tempVal.getText().toString();

        tempVal = findViewById(R.id.txtDireccionAmigos);
        String direccion = tempVal.getText().toString();

        tempVal = findViewById(R.id.txtTelefonoAmigos);
        String tel = tempVal.getText().toString();

        tempVal = findViewById(R.id.txtEmailAmigos);
        String email = tempVal.getText().toString();

        tempVal = findViewById(R.id.txtDuiAmigos);
        String dui = tempVal.getText().toString();

        String[] datos = {idAmigo, nombre, direccion, tel, email, dui, urlFoto};
        db.administrar_amigos(accion, datos);
        mostrarMsg("Registro de amigo guardado con exito.");

        regresarListaAmigos();
    }
    private void mostrarMsg(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
    private void regresarListaAmigos(){
        Intent intent = new Intent(this, lista_amigos.class);
        startActivity(intent);
    }
}
