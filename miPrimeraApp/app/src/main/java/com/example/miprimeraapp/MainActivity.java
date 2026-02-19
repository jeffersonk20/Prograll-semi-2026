package com.example.miprimeraapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    TabHost tbh;
    Double valores[] = new Double[] {1.0, 0.85, 7.67, 26.42, 36.80, 495.77};
    Double longitudes[] = new Double[] {1.0, 1000.0, 100.0, 39.3701, 3.280841666667, 1.1963081929167, 1.09361};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            tbh = findViewById(R.id.tbhConversores);
            if (tbh != null) {
                tbh.setup();

                // Usamos ContextCompat para evitar crashes al cargar recursos
                TabHost.TabSpec spec;

                spec = tbh.newTabSpec("Monedas");
                spec.setContent(R.id.tabMonedas);
                spec.setIndicator("Monedas", ContextCompat.getDrawable(this, R.drawable.moneda));
                tbh.addTab(spec);

                spec = tbh.newTabSpec("Longitud");
                spec.setContent(R.id.tabLongitud);
                spec.setIndicator("Longitud", ContextCompat.getDrawable(this, R.drawable.longitud));
                tbh.addTab(spec);

                spec = tbh.newTabSpec("Volumen");
                spec.setContent(R.id.tabVolumen);
                spec.setIndicator("Volumen", ContextCompat.getDrawable(this, R.drawable.peso));
                tbh.addTab(spec);

                spec = tbh.newTabSpec("Masa");
                spec.setContent(R.id.tabMasa);
                spec.setIndicator("Masa", ContextCompat.getDrawable(this, R.drawable.masa));
                tbh.addTab(spec);
            }

            // Inicialización segura de botones
            Button btnMonedas = findViewById(R.id.btnMonedasConvertir);
            if (btnMonedas != null) {
                btnMonedas.setOnClickListener(v -> convertirMonedas());
            }

            Button btnLongitud = findViewById(R.id.btnLongitudConvertir);
            if (btnLongitud != null) {
                btnLongitud.setOnClickListener(v -> convertirLongitud());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void convertirLongitud() {
        try {
            Spinner spnDe = findViewById(R.id.spnLongitudDe);
            Spinner spnA = findViewById(R.id.spnLongitudA);
            EditText txtCantidad = findViewById(R.id.txtLongitudCantidad);
            TextView lblRespuesta = findViewById(R.id.lblLongitudRespuesta);

            if (spnDe != null && spnA != null && txtCantidad != null && lblRespuesta != null) {
                int de = spnDe.getSelectedItemPosition();
                int a = spnA.getSelectedItemPosition();
                double cantidad = Double.parseDouble(txtCantidad.getText().toString());
                double respuesta = (longitudes[a] / longitudes[de]) * cantidad;
                lblRespuesta.setText("Respuesta: " + respuesta);
            }
        } catch (Exception e) {
            // Manejar error si el campo está vacío
        }
    }

    private void convertirMonedas() {
        try {
            Spinner spnDe = findViewById(R.id.spnMonedasDe);
            Spinner spnA = findViewById(R.id.spnMonedasA);
            EditText txtCantidad = findViewById(R.id.txtMonedasCantidad);
            TextView lblRespuesta = findViewById(R.id.lblMonedasRespuesta);

            if (spnDe != null && spnA != null && txtCantidad != null && lblRespuesta != null) {
                int de = spnDe.getSelectedItemPosition();
                int a = spnA.getSelectedItemPosition();
                double cantidad = Double.parseDouble(txtCantidad.getText().toString());
                double respuesta = (valores[a] / valores[de]) * cantidad;
                lblRespuesta.setText("Respuesta: " + respuesta);
            }
        } catch (Exception e) {
            // Manejar error
        }
    }
}
