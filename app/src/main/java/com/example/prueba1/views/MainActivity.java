package com.example.prueba1.views;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prueba1.R;
import com.example.prueba1.model.Car;
import com.example.prueba1.presenters.CarAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class MainActivity extends AppCompatActivity {
    private Button ir_registro_car;
    private ImageButton buttonLogout; // Botón de cierre de sesión
    private ImageButton buttonAi; // Boton AI
    RecyclerView cRecycler;
    CarAdapter cAdapter;
    FirebaseFirestore mFirestore;
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    FirebaseUser  currentUser ;
    private TextView textViewRecordatorio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Verificar si el usuario está autenticado
        currentUser  = firebaseAuth.getCurrentUser ();
        if (currentUser  == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        // Inicializar componentes
        textViewRecordatorio = findViewById(R.id.text_view_recordatorio); // Inicializar el TextView

        // Inicializar Firestore y RecyclerView
        mFirestore = FirebaseFirestore.getInstance();
        cRecycler = findViewById(R.id.Recyclerview2);
        cRecycler.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        // Configurar consulta de Firestore
        String userId = currentUser .getUid(); // Obtener el ID del usuario actual
        Query query = mFirestore.collection("Vehiculo").whereEqualTo("userId", userId);
        FirestoreRecyclerOptions<Car> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder<Car>().setQuery(query, Car.class).build();

        // Inicializar adaptador
        cAdapter = new CarAdapter(firestoreRecyclerOptions, MainActivity.this);
        cAdapter.notifyDataSetChanged();
        cRecycler.setAdapter(cAdapter);

        // Configurar el botón de cierre de sesión
        buttonLogout = findViewById(R.id.button_logout);
        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Lógica para cerrar sesión
                firebaseAuth.signOut();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                Toast.makeText(MainActivity.this, "Sesión cerrada", Toast.LENGTH_SHORT).show();
            }
        });

        buttonAi = findViewById(R.id.button_ai);
        Animation sparkleAnim = AnimationUtils.loadAnimation(this, R.anim.sparkle_anim);
        buttonAi.startAnimation(sparkleAnim);
        buttonAi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AiRecommendations.class);
                startActivity(intent);
            }
        });

        ir_registro_car = findViewById(R.id.Plus);
        ir_registro_car.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RegistroCarActivity.class);
                startActivity(intent);
                // Agrega la animación de transición
//                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        // Obtener los vehículos y luego los mantenimientos
        obtenerVehiculosYMantenimientos(userId);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        cAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        cAdapter.stopListening();
    }

    private void obtenerVehiculosYMantenimientos(String userId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Vehiculo")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String carId = document.getId();
                            obtenerMantenimientosMasRecientesPorCarId(carId);
                        }
                    } else {
                        Log.e("Firestore", "Error getting vehicles: ", task.getException());
                    }
                });
    }

    private void obtenerMantenimientosMasRecientesPorCarId(String carId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Mantenimiento")
                .whereEqualTo("carId", carId)
                .orderBy("fecha", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            String fecha = document.getString("fecha");
                            String idTipo = document.getString("id_tipo");
                            obtenerTipoMantenimiento(idTipo, fecha);
                        }
                    } else {
                        Log.e("Firestore", "Error getting maintenance documents: ", task.getException());
                    }
                });
    }

    private void obtenerTipoMantenimiento(String idTipo, String fecha) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Tipo_Mantenimiento")
                .whereEqualTo("id", idTipo)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            String tipo = document.getString("nombre");
                            String recordatorio = "Tu último mantenimiento fue el " + fecha + " en " + tipo + ", no olvides hacer uno nuevo.";
                            textViewRecordatorio.setText(recordatorio); // Establecer el texto en el TextView
                        }
                    } else {
                        Log.e("Firestore", "Error getting Tipo_Mantenimiento: ", task.getException());
                    }
                });
    }
}