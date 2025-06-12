package com.example.prueba1.views;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
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
import com.example.prueba1.model.Mantenimiento;
import com.example.prueba1.presenters.MantenimientoAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Calendar;

public class HistorialActivity extends AppCompatActivity {

    private ImageButton atras_mosaico;
    private TextView btnFecha;
    private RecyclerView mRecycler;
    private MantenimientoAdapter mAdapter;
    private FirebaseFirestore mFirestore;

    private String tipo;
    private String carId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_historial);

        mFirestore = FirebaseFirestore.getInstance();
        mRecycler = findViewById(R.id.Recyclerview);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));

        TextView tituloMantenimiento = findViewById(R.id.Titulo_mantenimiento);
        btnFecha = findViewById(R.id.tv_fecha_filtro);

        tipo = getIntent().getStringExtra("tipo_mantenimiento");
        carId = getIntent().getStringExtra("carId");
        String titulo = getIntent().getStringExtra("titulo_mantenimiento");
        tituloMantenimiento.setText(titulo);

        atras_mosaico = findViewById(R.id.Atras_mosaico);
        atras_mosaico.setOnClickListener(v -> {
            Intent intent = new Intent(HistorialActivity.this, MosaicoActivity.class);
            intent.putExtra("carId", carId);
            startActivity(intent);
        });

        btnFecha.setOnClickListener(v -> showDatePicker());

        aplicarQuerySinFecha();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void aplicarQuerySinFecha() {
        Query query = mFirestore.collection("Mantenimiento")
                .whereEqualTo("id_tipo", tipo)
                .whereEqualTo("carId", carId);

        FirestoreRecyclerOptions<Mantenimiento> options =
                new FirestoreRecyclerOptions.Builder<Mantenimiento>()
                        .setQuery(query, Mantenimiento.class)
                        .build();

        if (mAdapter != null) {
            mAdapter.stopListening();
        }

        mAdapter = new MantenimientoAdapter(options);
        mRecycler.setAdapter(mAdapter);
        mAdapter.startListening();
    }

    private void filtrarPorFecha(String fecha) {
        Query queryFiltrado = mFirestore.collection("Mantenimiento")
                .whereEqualTo("id_tipo", tipo)
                .whereEqualTo("carId", carId)
                .whereEqualTo("fecha", fecha);

        FirestoreRecyclerOptions<Mantenimiento> options =
                new FirestoreRecyclerOptions.Builder<Mantenimiento>()
                        .setQuery(queryFiltrado, Mantenimiento.class)
                        .build();

        if (mAdapter != null) {
            mAdapter.stopListening();
        }

        mAdapter = new MantenimientoAdapter(options);
        mRecycler.setAdapter(mAdapter);
        mAdapter.startListening();
    }

    private void showDatePicker() {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    String fechaFormateada = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    filtrarPorFecha(fechaFormateada);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAdapter != null) {
            mAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }
}
