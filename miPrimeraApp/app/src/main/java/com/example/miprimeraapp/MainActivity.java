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
    Button btnInvertir;
    Double valores[][] = {
            {1.0, 0.85, 7.67, 26.42, 36.80, 495.77}, // monedas
            {1.0, 1000.0, 100.0, 39.3701, 3.280841666667, 1.1963081929167, 1.09361}, // longitud
            {1.0, 1000.0, 1000000.0, 0.001, 0.264172, 0.0353147}, // volumen
            {1.0, 1000.0, 1000000.0, 0.001, 2.20462, 35.274}, // masa
            {1.0, 1.0/60.0, 1.0/3600.0, 1.0/86400.0, 1.0/604800.0}, // tiempo
            {1099511627776.0, 1073741824.0, 1048576.0, 1024.0, 1.0}, // almacenamiento (Byte, KB, MB, GB, TB)
            {1000000000.0, 125000000.0, 1000000.0, 1000.0, 1.0} // transferencia (bps, Bps, Kbps, Mbps, Gbps)
    };

    String[][] etiquetas = {
            {"Dolar", "Euro", "Quetzal", "Lempira", "Cordoba", "Colon CR"}, // monedas
            {"Mts", "Ml", "Cm", "Pulgada", "Pies", "Vara", "Yarda"}, // longitud
            {"Litro", "Mililitro", "Microlitro", "Metro cúbico", "Galón", "Pie cúbico"}, // volumen
            {"Kg", "Gramo", "Miligramo", "Tonelada", "Libra", "Onza"}, // masa
            {"Segundo", "Minuto", "Hora", "Día", "Semana"}, // tiempo
            {"Byte", "KB", "MB", "GB", "TB"}, // almacenamiento
            {"bps", "Bps", "Kbps", "Mbps", "Gbps"} // transferencia de datos
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = findViewById(R.id.btnConvertir);
        btn.setOnClickListener(v->convertir());

        btnInvertir = findViewById(R.id.btnInvertir);
        btnInvertir.setOnClickListener(v -> invertirConversion());

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
        Spinner spnDe = findViewById(R.id.spnDe);
        spnDe.setAdapter(aaEtiquetas);

        Spinner spnA = findViewById(R.id.spnA);
        spnA.setAdapter(aaEtiquetas);
    }
    private void convertir(){
        Spinner spnTipo = findViewById(R.id.spnTipo);
        int tipo = spnTipo.getSelectedItemPosition();

        Spinner spnDe = findViewById(R.id.spnDe);
        int de = spnDe.getSelectedItemPosition();

        Spinner spnA = findViewById(R.id.spnA);
        int a = spnA.getSelectedItemPosition();

        tempVal = findViewById(R.id.txtCantidad);
        try {
            double cantidad = Double.parseDouble(tempVal.getText().toString());
            double respuesta = conversor(tipo, de, a, cantidad);

            tempVal = findViewById(R.id.lblRespuesta);
            tempVal.setText("Respuesta: " + respuesta);
        } catch (Exception e) {
            // Error al parsear el numero
        }
    }
    private void invertirConversion() {
        Spinner spnDe = findViewById(R.id.spnDe);
        Spinner spnA = findViewById(R.id.spnA);

        int posDe = spnDe.getSelectedItemPosition();
        int posA = spnA.getSelectedItemPosition();

        // Intercambiar posiciones
        spnDe.setSelection(posA);
        spnA.setSelection(posDe);
    }
    double conversor(int tipo, int de, int a, double cantidad){
        return (valores[tipo][a] / valores[tipo][de]) * cantidad;
    }
}
