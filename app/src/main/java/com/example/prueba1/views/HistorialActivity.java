package com.example.prueba1.views;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
    private RecyclerView mRecycler;
    private MantenimientoAdapter mAdapter;
    private FirebaseFirestore mFirestore;

    private ImageButton btnFechaInicio, btnFechaFin, btnLimpiarFechas;
    private TextView tvFechaSeleccionada;

    private String fechaInicio = null;
    private String fechaFin = null;

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

        tipo = getIntent().getStringExtra("tipo_mantenimiento");
        carId = getIntent().getStringExtra("carId");

        // Inicialmente mostrar todo sin filtro
        aplicarQuerySinFecha();

        // Título
        TextView tituloMantenimiento = findViewById(R.id.Titulo_mantenimiento);
        String titulo = getIntent().getStringExtra("titulo_mantenimiento");
        tituloMantenimiento.setText(titulo);

        // Botón atrás
        atras_mosaico = findViewById(R.id.Atras_mosaico);
        atras_mosaico.setOnClickListener(v -> {
            Intent intent = new Intent(HistorialActivity.this, MosaicoActivity.class);
            intent.putExtra("carId", carId);
            startActivity(intent);
        });

        // Botones y texto
        btnFechaInicio = findViewById(R.id.btnFechaInicio);
        btnFechaFin = findViewById(R.id.btnFechaFin);
        btnLimpiarFechas = findViewById(R.id.btnLimpiarFechas);
        tvFechaSeleccionada = findViewById(R.id.tvFechaSeleccionada);

        btnFechaInicio.setOnClickListener(v -> showDatePicker(true));
        btnFechaFin.setOnClickListener(v -> showDatePicker(false));

        btnLimpiarFechas.setOnClickListener(v -> {
            fechaInicio = null;
            fechaFin = null;
            actualizarTextoFechas();
            aplicarQuerySinFecha();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void showDatePicker(boolean esInicio) {
        final Calendar calendar = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String fechaSeleccionada = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);

                    if (esInicio) {
                        fechaInicio = fechaSeleccionada;
                    } else {
                        fechaFin = fechaSeleccionada;
                    }

                    actualizarTextoFechas();

                    if (fechaInicio != null && fechaFin != null) {
                        aplicarQueryPorRango();
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void actualizarTextoFechas() {
        if (fechaInicio != null && fechaFin != null) {
            tvFechaSeleccionada.setText("Rango: " + fechaInicio + " a " + fechaFin);
        } else if (fechaInicio != null) {
            tvFechaSeleccionada.setText("Desde: " + fechaInicio);
        } else if (fechaFin != null) {
            tvFechaSeleccionada.setText("Hasta: " + fechaFin);
        } else {
            tvFechaSeleccionada.setText("Rango: no seleccionado");
        }
    }

    private void aplicarQueryPorRango() {
        Query query = mFirestore.collection("Mantenimiento")
                .whereEqualTo("id_tipo", tipo)
                .whereEqualTo("carId", carId)
                .whereGreaterThanOrEqualTo("fecha", fechaInicio)
                .whereLessThanOrEqualTo("fecha", fechaFin);

        FirestoreRecyclerOptions<Mantenimiento> options =
                new FirestoreRecyclerOptions.Builder<Mantenimiento>()
                        .setQuery(query, Mantenimiento.class)
                        .build();

        if (mAdapter != null) {
            mAdapter.stopListening();
        }

        mAdapter = new MantenimientoAdapter(options);
        mAdapter.startListening();
        mRecycler.setAdapter(mAdapter);
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
        mAdapter.startListening();
        mRecycler.setAdapter(mAdapter);
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
