package com.example.prueba1.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prueba1.R;

public class MosaicoActivity extends AppCompatActivity {
    private Button registrar_mantenimiento;
    private ImageButton atras_main;
    private String carId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        Log.e("MainActivity", "Vehiculo placa: " + vehiculoPlaca);
         carId = getIntent().getStringExtra("carId");

        if(carId != null){
            Log.d("FirebaseAuth", "carId nay" );
            Log.d("FirebaseAuth", "carId " + carId);

        }else{
            Log.d("FirebaseAuth", "carId " + carId);

        }
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mosaico);
        // Referencia a los botones
        Button buttonAceites = findViewById(R.id.button_aceites);
        Button buttonRefrigeracion = findViewById(R.id.button_refrigeracion);
        Button buttonFrenos = findViewById(R.id.button_frenos);
        Button buttonCorreas = findViewById(R.id.button_correas);
        Button buttonBujias = findViewById(R.id.button_bujias);
        Button buttonNeumaticos = findViewById(R.id.button_neumaticos);
        Button buttonTransmision = findViewById(R.id.button_transmision);
        Button buttonElectrico = findViewById(R.id.button_electrico);
        ImageButton buttonTodos = findViewById(R.id.button_todos);

        // Configurar el listener para abrir la nueva actividad
        buttonAceites.setOnClickListener(v -> abrirDetalleMantenimiento(buttonAceites.getText().toString(), "tipo_1"));
        buttonRefrigeracion.setOnClickListener(v -> abrirDetalleMantenimiento(buttonRefrigeracion.getText().toString(), "tipo_2"));
        buttonFrenos.setOnClickListener(v -> abrirDetalleMantenimiento(buttonFrenos.getText().toString(), "tipo_3"));
        buttonCorreas.setOnClickListener(v -> abrirDetalleMantenimiento(buttonCorreas.getText().toString(), "tipo_4"));
        buttonBujias.setOnClickListener(v -> abrirDetalleMantenimiento(buttonBujias.getText().toString(), "tipo_5"));
        buttonNeumaticos.setOnClickListener(v -> abrirDetalleMantenimiento(buttonNeumaticos.getText().toString(), "tipo_6"));
        buttonTransmision.setOnClickListener(v -> abrirDetalleMantenimiento(buttonTransmision.getText().toString(), "tipo_7"));
        buttonElectrico.setOnClickListener(v -> abrirDetalleMantenimiento(buttonElectrico.getText().toString(), "tipo_8"));
        buttonTodos.setOnClickListener(v -> abrirDetalleMantenimiento("Mantenimiento Global", "tipo_9"));

        registrar_mantenimiento = findViewById(R.id.Registrar_mantenimiento);
        registrar_mantenimiento.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MosaicoActivity.this, RegistroMantenimientoActivity.class);
                intent.putExtra("carId", carId);
                startActivity(intent);
            }
        });

        atras_main = findViewById(R.id.Atras_main);
        atras_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MosaicoActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Método para abrir la nueva actividad y pasar el título
    private void abrirDetalleMantenimiento(String titulo, String tipo) {
        Intent intent = new Intent(MosaicoActivity.this, HistorialActivity.class);
        intent.putExtra("titulo_mantenimiento", titulo);
        intent.putExtra("tipo_mantenimiento", tipo);
        intent.putExtra("carId", carId);
        startActivity(intent);
    }
}