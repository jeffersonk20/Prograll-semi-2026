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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {
    FloatingActionButton fab;
    Button btn;
    TextView tempVal;
    String accion="", idAmigo="", id="", rev="";
    ImageView img;
    String urlCompletaFoto="", getUrlCompletaFotoFirestore ="";
    Intent tomarFotoIntent;
    detectarinternet di;
    DatabaseReference databaseReference;
    String miToken = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        obtenerToken();
        img = findViewById(R.id.imgFotoAmigo);

        btn = findViewById(R.id.btnGuardarAmigo);
        btn.setOnClickListener(view -> subirFotoFirestore());

        //mostrarDatos();
        tomarFoto();
    }
    private void obtenerToken(){
        try{
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(tarea->{
                if(!tarea.isSuccessful()){
                    mostrarMsg("Error al obtener token: "+tarea.getException().getMessage());
                }else{
                    miToken = tarea.getResult();
                }
            });
        }catch (Exception e){
            mostrarMsg("Error al obtener token: "+e.getMessage());
        }
    }
    private void tomarFoto(){
        img.setOnClickListener(view->{
            tomarFotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File fotoAmigo = null;
            try{
                fotoAmigo = crearImagenAmigo();
                if( fotoAmigo!=null ){
                    Uri uriFotoAimgo = FileProvider.getUriForFile(MainActivity.this,
                            "com.ugb.miprimeraapp.fileprovider", fotoAmigo);
                    tomarFotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriFotoAimgo);
                    startActivityForResult(tomarFotoIntent, 1);
                }else{
                    mostrarMsg("Nose pudo crear la imagen.");
                }
            }catch (Exception e){
                mostrarMsg("Error al tomar foto: "+e.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try{
            if( requestCode==1 && resultCode==RESULT_OK ){
                img.setImageURI(Uri.parse(urlCompletaFoto));
            }else{
                mostrarMsg("No se tomo la foto.");
            }
        }catch (Exception e){
            mostrarMsg("Error al tomar la foto: "+e.getMessage());
        }
    }

    private File crearImagenAmigo() throws Exception{
        String fechaHoraMs = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()),
                fileName = "imagen_"+ fechaHoraMs+"_";
        File dirAlmacenamiento = getExternalFilesDir(Environment.DIRECTORY_DCIM);
        if( dirAlmacenamiento.exists()==false ){
            dirAlmacenamiento.mkdir();
        }
        File image = File.createTempFile(fileName, ".jpg", dirAlmacenamiento);
        urlCompletaFoto = image.getAbsolutePath();
        return image;
    }
    private void mostrarMsg(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
    private void abrirVentana(){
        /*Intent intent = new Intent(this, lista_amigos.class);
        startActivity(intent);*/
    }
    private void guardarAmigo() {
        try {
            tempVal = findViewById(R.id.txtNombreAmigos);
            String nombre = tempVal.getText().toString();

            tempVal = findViewById(R.id.txtDireccionAmigos);
            String direccion = tempVal.getText().toString();

            tempVal = findViewById(R.id.txtTelefonoAmigos);
            String telefono = tempVal.getText().toString();

            tempVal = findViewById(R.id.txtEmailAmigos);
            String email = tempVal.getText().toString();

            tempVal = findViewById(R.id.txtDuiAmigos);
            String dui = tempVal.getText().toString();

            databaseReference = FirebaseDatabase.getInstance().getReference("amigos");
            String key = databaseReference.push().getKey();

            if( miToken.equals("") || miToken==null ){
                obtenerToken();
            }
            amigos amigo = new amigos(idAmigo, nombre, direccion, telefono, email, dui, urlCompletaFoto, getUrlCompletaFotoFirestore, miToken);
            if( key!= null ){
                databaseReference.child(key).setValue(amigo).addOnSuccessListener(success->{
                    mostrarMsg("Registro guardado con exito.");
                    abrirVentana();
                }).addOnFailureListener(failure->{
                    mostrarMsg("Error al registrar datos: "+failure.getMessage());
                });
            } else {
                mostrarMsg("Error al guardar en firebase.");
            }
        }catch (Exception e){
            mostrarMsg("Error guardar: "+e.getMessage());
        }
    }
    private void subirFotoFirestore(){
        mostrarMsg("Subiendo foto a firestore");
        StorageReference reference = FirebaseStorage.getInstance().getReference();
        Uri file = Uri.fromFile(new File(urlCompletaFoto));
        final StorageReference fileRef = reference.child("fotosAmigos/"+file.getLastPathSegment());

        final UploadTask uploadTask = fileRef.putFile(file);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                getUrlCompletaFotoFirestore = uri.toString();
                guardarAmigo();
            }).addOnFailureListener(e -> {
                mostrarMsg("Error al obtener la url de la foto: "+e.getMessage());
            });
        }).addOnFailureListener(e -> {
            mostrarMsg("Error al subir la foto: "+e.getMessage());
        });
    }
}
