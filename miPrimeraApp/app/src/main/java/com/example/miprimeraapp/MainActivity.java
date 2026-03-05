package com.example.miprimeraapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    TabHost tbh;
    TextView lblaguaRespuesta, tempVal;
    EditText txtaguaCantidad;
    Button btnaguaCalcular, btn;
    Spinner spn;

    Double[] valores = {0.0929, 0.698896, 0.836, 1.0, 0.001588, 0.000143, 0.0001};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tbh = findViewById(R.id.tbhConversores);
        if (tbh != null) {
            tbh.setup();

            TabHost.TabSpec specAgua = tbh.newTabSpec("Agua");
            specAgua.setContent(R.id.tabvaloragua);
            specAgua.setIndicator("Agua");
            tbh.addTab(specAgua);

            TabHost.TabSpec specAreas = tbh.newTabSpec("Areas");
            specAreas.setContent(R.id.tabAreas);
            specAreas.setIndicator("ÁREAS");
            tbh.addTab(specAreas);
        }

        txtaguaCantidad = findViewById(R.id.txtaguaCantidad);
        lblaguaRespuesta = findViewById(R.id.lblaguaRespuesta);
        btnaguaCalcular = findViewById(R.id.btnaguaCalcular);

        if (btnaguaCalcular != null) {
            btnaguaCalcular.setOnClickListener(v -> calcularPagoAgua());
        }

        btn = findViewById(R.id.btnAreasConvertir);
        if (btn != null) {
            btn.setOnClickListener(v -> convertirAreas());
        }
    }

    private void calcularPagoAgua() {
        try {
            String strMetros = txtaguaCantidad.getText().toString();
            if (strMetros.isEmpty()) {
                lblaguaRespuesta.setText("Por favor, ingrese los metros.");
                return;
            }

            double metros = Double.parseDouble(strMetros);
            double totalAPagar = 0;

            if (metros <= 18) {
                totalAPagar = 6.0;
            } else if (metros <= 28) {
                double exceso = metros - 18;
                totalAPagar = 6.0 + (exceso * 0.45);
            } else {
                double excesoSobre28 = metros - 28;
                totalAPagar = 6.0 + 4.50 + (excesoSobre28 * 0.65);
            }

            lblaguaRespuesta.setText("Cantidad a pagar: $" + String.format("%.2f", totalAPagar));

        } catch (Exception e) {
            lblaguaRespuesta.setText("Error: Ingrese un valor válido.");
        }
    }

    private void convertirAreas() {
        try {
            spn = findViewById(R.id.spnAreasDe);
            int de = spn.getSelectedItemPosition();

            spn = findViewById(R.id.spnAreasA);
            int a = spn.getSelectedItemPosition();

            EditText txtCantidad = findViewById(R.id.txtAreasCantidad);
            String cantidadStr = txtCantidad.getText().toString();

            if (!cantidadStr.isEmpty()) {
                double cantidad = Double.parseDouble(cantidadStr);
                double respuesta = conversor(de, a, cantidad);

                tempVal = findViewById(R.id.lblAreasRespuesta);
                tempVal.setText("Respuesta: " + String.format("%.6f", respuesta));
            }
        } catch (Exception e) {
        }
    }

    double conversor(int de, int a, double cantidad) {
        return (valores[a] / valores[de]) * cantidad;
    }
}
