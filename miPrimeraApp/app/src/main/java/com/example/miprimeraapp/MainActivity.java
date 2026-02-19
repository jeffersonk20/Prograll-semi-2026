package com.example.miprimeraapp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class MainActivity extends AppCompatActivity {

    TextView tempVal;
    Spinner spn;
    Button btn;
    Button btnInvertir;
    Button btnTema;
    TextView lblIconoTema;

    boolean modoOscuro = false;

    // Íconos temáticos para cada tipo de conversión
    String[] iconosTema = {
            "💱", // monedas
            "📏", // longitud
            "🧪", // volumen
            "⚖️", // masa
            "⏱️", // tiempo
            "💾", // almacenamiento
            "📡"  // transferencia
    };

    Double valores[][] = {
            {1.0, 0.85, 7.67, 26.42, 36.80, 495.77}, // monedas
            {1.0, 1000.0, 100.0, 39.3701, 3.280841666667, 1.1963081929167, 1.09361}, // longitud
            {1.0, 1000.0, 1000000.0, 0.001, 0.264172, 0.0353147}, // volumen
            {1.0, 1000.0, 1000000.0, 0.001, 2.20462, 35.274}, // masa
            {1.0, 1.0/60.0, 1.0/3600.0, 1.0/86400.0, 1.0/604800.0}, // tiempo
            {1099511627776.0, 1073741824.0, 1048576.0, 1024.0, 1.0}, // almacenamiento
            {1000000000.0, 125000000.0, 1000000.0, 1000.0, 1.0} // transferencia
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

        lblIconoTema = findViewById(R.id.lblIconoTema);

        // ── Botón Convertir
        btn = findViewById(R.id.btnConvertir);
        btn.setOnClickListener(v -> convertir());

        // ── Botón Invertir
        btnInvertir = findViewById(R.id.btnInvertir);
        btnInvertir.setOnClickListener(v -> invertirConversion());

        // ── Botón Tema Oscuro/Claro
        btnTema = findViewById(R.id.btnTema);
        btnTema.setOnClickListener(v -> toggleTema());

        // ── Spinner Tipo ─────────────────────────────────────────────────
        cambiarEtiqueta(0); // valores predeterminados

        spn = findViewById(R.id.spnTipo);
        spn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                cambiarEtiqueta(i);
                // Actualizar ícono temático al cambiar el tipo
                lblIconoTema.setText(iconosTema[i]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) { }
        });

        // ── Conversión en tiempo real ────────────────────────────────────
        EditText txtCantidad = findViewById(R.id.txtCantidad);
        txtCantidad.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                convertir(); // Convierte mientras el usuario escribe
            }

            @Override
            public void afterTextChanged(Editable s) { }
        });
    }

    // ── Toggle Tema Oscuro / Claro ───────────────────────────────────────
    private void toggleTema() {
        modoOscuro = !modoOscuro;
        if (modoOscuro) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            btnTema.setText("☀️");
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            btnTema.setText("🌙");
        }
    }

    private void cambiarEtiqueta(int posicion) {
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

    private void convertir() {
        Spinner spnTipo = findViewById(R.id.spnTipo);
        int tipo = spnTipo.getSelectedItemPosition();

        Spinner spnDe = findViewById(R.id.spnDe);
        int de = spnDe.getSelectedItemPosition();

        Spinner spnA = findViewById(R.id.spnA);
        int a = spnA.getSelectedItemPosition();

        EditText txtCantidad = findViewById(R.id.txtCantidad);
        try {
            double cantidad = Double.parseDouble(txtCantidad.getText().toString());
            double respuesta = conversor(tipo, de, a, cantidad);

            TextView lblRespuesta = findViewById(R.id.lblRespuesta);
            lblRespuesta.setText("Respuesta: " + respuesta);
        } catch (Exception e) {
            // Campo vacío o valor inválido: limpiar respuesta
            TextView lblRespuesta = findViewById(R.id.lblRespuesta);
            lblRespuesta.setText("Respuesta:    ");
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

    double conversor(int tipo, int de, int a, double cantidad) {
        return (valores[tipo][a] / valores[tipo][de]) * cantidad;
    }
}