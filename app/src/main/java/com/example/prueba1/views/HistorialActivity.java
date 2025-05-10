package com.example.prueba1.views;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prueba1.R;
import com.example.prueba1.presenters.MantenimientoAdapter;
import com.example.prueba1.model.Mantenimiento;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class HistorialActivity extends AppCompatActivity {
    private ImageButton atras_mosaico;
    RecyclerView mRecycler;
    MantenimientoAdapter mAdapter;
    FirebaseFirestore mFirestore;
//    String vehiculoPlaca = getIntent().getStringExtra("VEHICULO_PLACA");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_historial);

        mFirestore = FirebaseFirestore.getInstance();
        mRecycler = findViewById(R.id.Recyclerview);
        mRecycler.setLayoutManager(new LinearLayoutManager(HistorialActivity.this));

        TextView tipoId = findViewById(R.id.Tipo_id);
        String tipo = getIntent().getStringExtra("tipo_mantenimiento");
        String carId = getIntent().getStringExtra("carId");

        Query query = mFirestore.collection("Mantenimiento").whereEqualTo("id_tipo", tipo).whereEqualTo("carId", carId);
        FirestoreRecyclerOptions<Mantenimiento> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<Mantenimiento>().setQuery(query, Mantenimiento.class).build();

        mAdapter = new MantenimientoAdapter(firestoreRecyclerOptions);
        mAdapter.notifyDataSetChanged();
        mRecycler.setAdapter(mAdapter);

        // Inicializacion de boton para volver a pantalla anterior
        atras_mosaico = findViewById(R.id.Atras_mosaico);
        atras_mosaico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HistorialActivity.this, MosaicoActivity.class);
                intent.putExtra("carId", carId);
                startActivity(intent);
            }
        });

        // Obtener el título pasado desde la actividad anterior
        TextView tituloMantenimiento = findViewById(R.id.Titulo_mantenimiento);
        String titulo = getIntent().getStringExtra("titulo_mantenimiento");
        tituloMantenimiento.setText(titulo);  // Mostrar el nombre del botón como título

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening();
    }
}