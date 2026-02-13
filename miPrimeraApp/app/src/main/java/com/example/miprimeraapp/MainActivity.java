package com.example.miprimeraapp;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    TextView tempVal;
    Spinner spn;
    Button btn;
    Double valores[][] = {
            {1.0, 0.85, 7.67, 26.42, 36.80, 495.77}, // monedas
            {1.0, 1000.0, 100.0, 39.3701, 3.280841666667, 1.1963081929167, 1.09361}, // longitud
            {1.0, 1000.0, 1000000.0, 0.001, 0.264172, 0.0353147}, // volumen
            {1.0, 1000.0, 1000000.0, 0.001, 2.20462, 35.274}, // masa
            {1.0, 60.0, 3600.0, 86400.0, 604800.0}, // tiempo
            {1.0, 1024.0, 1048576.0, 1073741824.0, 1099511627776.0}, // almacenamiento
            {1.0, 8.0, 1024.0, 1048576.0, 1073741824.0} // transferencia de datos
    };

    String[][] etiquetas = {
            {"Dolar", "Euro", "Quetzal", "Lempira", "Cordoba", "Colon CR"}, // monedas
            {"Mts", "Ml", "Cm", "Pulgada", "Pies", "Vara", "Yarda"}, // longitud
            {"Litro", "Mililitro", "Microlitro", "Metro cúbico", "Galón", "Pie cúbico"}, // volumen
            {"Kg", "Gramo", "Miligramo", "Tonelada", "Libra", "Onza"}, // masa
            {"Minuto", "Segundo", "Hora", "Día", "Semana"}, // tiempo
            {"Byte", "KB", "MB", "GB", "TB"}, // almacenamiento
            {"bps", "Bps", "Kbps", "Mbps", "Gbps"} // transferencia de datos
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = findViewById(R.id.btnConvertir);
        btn.setOnClickListener(v->convertir());

        cambiarEtiqueta(0);//valores predeterminaods

        spn = findViewById(R.id.spnTipo);
        spn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                cambiarEtiqueta(i);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
    private void cambiarEtiqueta(int posicion){
        ArrayAdapter<String> aaEtiquetas = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                etiquetas[posicion]
        );
        aaEtiquetas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn = findViewById(R.id.spnDe);
        spn.setAdapter(aaEtiquetas);

        spn = findViewById(R.id.spnA);
        spn.setAdapter(aaEtiquetas);
    }
    private void convertir(){
        spn = findViewById(R.id.spnTipo);
        int tipo = spn.getSelectedItemPosition();

        spn = findViewById(R.id.spnDe);
        int de = spn.getSelectedItemPosition();

        spn = findViewById(R.id.spnA);
        int a = spn.getSelectedItemPosition();

        tempVal = findViewById(R.id.txtCantidad);
        double cantidad = Double.parseDouble(tempVal.getText().toString());
        double respuesta = conversor(tipo, de, a, cantidad);

        tempVal = findViewById(R.id.lblRespuesta);
        tempVal.setText("Respuesta: "+ respuesta);
    }
    double conversor(int tipo, int de, int a, double cantidad){
        return valores[tipo][a]/valores[tipo][de] * cantidad;
    }
}