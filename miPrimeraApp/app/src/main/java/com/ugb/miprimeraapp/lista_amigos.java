package com.ugb.miprimeraapp;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
public class lista_amigos extends Activity {
    Bundle parametros = new Bundle();
    ListView ltsAmigos;
    final ArrayList<amigos> alAmigos = new ArrayList<amigos>();
    final ArrayList<amigos> alAmigosCopia = new ArrayList<amigos>();
    JSONArray jsonArray = new JSONArray();
    JSONObject jsonObject;
    amigos misAmigos;
    FloatingActionButton fab;
    int posicion = 0;
    DatabaseReference databaseReference;
    String miToken = "";
    detectarinternet di;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_amigos);
        ltsAmigos = findViewById(R.id.ltsAmigos);
        parametros.putString("accion", "nuevo");
        listarDatos();
        buscarAmigos();
    }
    private void listarDatos(){
        try{
            databaseReference  = FirebaseDatabase.getInstance().getReference("amigos");
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(tarea->{
                if(!tarea.isSuccessful()){
                    mostrarMsg("Error al obtener token: "+tarea.getException().getMessage());
                    return;
                }else{
                    miToken = tarea.getResult();
                    if( miToken!=null && miToken.length()>0 ){
                        databaseReference.orderByChild("token").equalTo(miToken).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                try{
                                    if( snapshot.getChildrenCount()<=0 ){
                                        mostrarMsg("No hay amigos registrados.");
                                        parametros.putString("accion", "nuevo");
                                        abrirVentana();
                                    }
                                }catch (Exception e){
                                    mostrarMsg("Error al llamar la ventana: " + e.getMessage());
                                }
                            }
                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                mostrarMsg("Error se cancelo: " + error.getMessage());
                            }
                        });
                    }
                }
            });
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    try{
                        jsonArray = new JSONArray();
                        for( DataSnapshot dataSnapshot : snapshot.getChildren() ){
                            amigos amigo = dataSnapshot.getValue(amigos.class);
                            jsonObject = new JSONObject();
                            jsonObject.put("idAmigo", amigo.getIdAmigo());
                            jsonObject.put("nombre", amigo.getNombre());
                            jsonObject.put("direccion", amigo.getDireccion());
                            jsonObject.put("telefono", amigo.getTelefono());
                            jsonObject.put("email", amigo.getEmail());
                            jsonObject.put("dui", amigo.getDui());
                            jsonObject.put("urlFoto", amigo.getFoto());
                            jsonObject.put("urlCompletaFotoFirestore", amigo.getUrlCompletaFotoFirestore());
                            jsonObject.put("to", amigo.getToken());
                            jsonObject.put("from", miToken);
                            jsonArray.put(jsonObject);
                        }
                        mostrarDatosAmigos();
                    }catch (Exception e){
                        mostrarMsg("Error al escuchar evento de firebase: " + e.getMessage());
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                }
            });
        }catch (Exception e){
            mostrarMsg("Error al listar datos: " + e.getMessage());
        }
    }
    private void mostrarDatosAmigos(){
        try{
            if(jsonArray.length()>0){
                alAmigos.clear();
                alAmigosCopia.clear();
                for (int i=0; i<jsonArray.length(); i++){
                    jsonObject = jsonArray.getJSONObject(i);
                    misAmigos = new amigos(
                            jsonObject.getString("idAmigo"),
                            jsonObject.getString("nombre"),
                            jsonObject.getString("direccion"),
                            jsonObject.getString("telefono"),
                            jsonObject.getString("email"),
                            jsonObject.getString("dui"),
                            jsonObject.getString("urlFoto"),
                            jsonObject.getString("urlCompletaFotoFirestore"),
                            jsonObject.getString("to")
                    );
                    alAmigos.add(misAmigos);
                }
                alAmigosCopia.addAll(alAmigos);
                ltsAmigos.setAdapter(new AdaptadorAmigos(this, alAmigos));
                registerForContextMenu(ltsAmigos);
            }else{
                mostrarMsg("No hay amigos registrados.");
                abrirVentana();
            }
        }catch (Exception e){
            mostrarMsg("Error al mostrar: " + e.getMessage());
        }
    }
    private void buscarAmigos(){
        TextView tempVal = findViewById(R.id.txtBuscarAmigos);
        tempVal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                alAmigos.clear();
                String buscar = tempVal.getText().toString().trim().toLowerCase();
                if( buscar.length()<=0){
                    alAmigos.addAll(alAmigosCopia);
                }else{
                    for (amigos item: alAmigosCopia){
                        if(item.getNombre().toLowerCase().contains(buscar) ||
                                item.getDui().toLowerCase().contains(buscar) ||
                                item.getEmail().toLowerCase().contains(buscar)){
                            alAmigos.add(item);
                        }
                    }
                    ltsAmigos.setAdapter(new AdaptadorAmigos(getApplicationContext(), alAmigos));
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
    private void mostrarMsg(String msg){
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
    }
    private void abrirVentana(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
