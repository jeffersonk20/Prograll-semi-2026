package com.example.miprimeraapp;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    TextView lblRespuesta, lblNum2;
    EditText txtNum1, txtNum2;
    Button btn;
    Spinner spn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtNum1 = findViewById(R.id.txtNum1);
        txtNum2 = findViewById(R.id.txtNum2);
        lblNum2 = findViewById(R.id.lblNum2);
        lblRespuesta = findViewById(R.id.lblRespuesta);
        spn = findViewById(R.id.cboOpciones);
        btn = findViewById(R.id.btnCalcular);

        btn.setOnClickListener(v -> calcular());

        // Listener para ocultar o mostrar el segundo número
        spn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // 6: Raiz, 7: Factorial
                if (position == 6 || position == 7) {
                    lblNum2.setVisibility(View.GONE);
                    txtNum2.setVisibility(View.GONE);
                } else {
                    lblNum2.setVisibility(View.VISIBLE);
                    txtNum2.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void calcular() {
        try {
            String n1Str = txtNum1.getText().toString();
            String n2Str = txtNum2.getText().toString();

            if (n1Str.isEmpty()) return;

            Double num1 = Double.parseDouble(n1Str);
            Double num2 = (txtNum2.getVisibility() == View.GONE || n2Str.isEmpty()) ? 0 : Double.parseDouble(n2Str);
            double respuesta = 0;

            switch (spn.getSelectedItemPosition()) {
                case 0: // Suma
                    respuesta = num1 + num2;
                    break;
                case 1: // Resta
                    respuesta = num1 - num2;
                    break;
                case 2: // Multiplicacion
                    respuesta = num1 * num2;
                    break;
                case 3: // Division
                    respuesta = num1 / num2;
                    break;
                case 4: // Exponente
                    respuesta = Math.pow(num1, num2);
                    break;
                case 5: // Porcentaje
                    respuesta = (num1 * num2) / 100;
                    break;
                case 6: // Raiz
                    respuesta = Math.sqrt(num1);
                    break;
                case 7: // Factorial
                    respuesta = factorial(num1.intValue());
                    break;
            }
            lblRespuesta.setText("Respuesta: " + respuesta);
        } catch (Exception e) {
            lblRespuesta.setText("Error en los datos");
        }
    }

    private double factorial(int n) {
        if (n < 0) return 0;
        double f = 1;
        for (int i = 1; i <= n; i++) {
            f *= i;
        }
        return f;
    }
}
