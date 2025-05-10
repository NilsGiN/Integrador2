package com.example.prueba1.views;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prueba1.R;
import com.example.prueba1.model.Car;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AiRecommendations extends AppCompatActivity {
    private ImageButton atras_main;
    private Button buttonGenerar;
    private Spinner spinnerPlacas;
    private TextView textoRecomendacion;

    private FirebaseFirestore mFirestore;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ai_recommendations);
        textoRecomendacion = findViewById(R.id.respuesta_ia);

        atras_main = findViewById(R.id.Atras_main);
        atras_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AiRecommendations.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Verificar usuario autenticado
        currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(AiRecommendations.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        mFirestore = FirebaseFirestore.getInstance();

        buttonGenerar = findViewById(R.id.btnGenerar);
        spinnerPlacas = findViewById(R.id.dropdown_carros);

        buttonGenerar.setOnClickListener(view -> {
            Car selectedCar = (Car) spinnerPlacas.getSelectedItem();
            if (selectedCar != null & !selectedCar.getPlaca().equals("Elige tu placa")) {
                String placa = selectedCar.getPlaca();
                obtenerDatosVehiculoYEnviar(placa); // función personalizada
            } else {
                textoRecomendacion.setText("Por favor, selecciona una placa válida.");
            }
        });

        // Cargar placas en el Spinner
        String userId = currentUser.getUid();
        obtenerPlacasPorUserID(userId);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void obtenerDatosVehiculoYEnviar(String placa) {
        mFirestore.collection("Vehiculo")
                .whereEqualTo("placa", placa)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        String idVehiculo = doc.getId();  // <-- necesario para buscar los mantenimientos
                        String modelo = doc.getString("modelo");
                        String marca = doc.getString("marca");
//                        String sistema = doc.getString("sistema");
                        Log.d("idVehiculo", idVehiculo);
                        // Buscar los mantenimientos asociados a ese vehículo
                        mFirestore.collection("Mantenimiento")
                                .whereEqualTo("carId", idVehiculo)
                                .get()
                                .addOnSuccessListener(mantenimientoSnapshots -> {
                                    List<Map<String, Object>> mantenimientos = new ArrayList<>();

                                    for (DocumentSnapshot mantenimientoDoc : mantenimientoSnapshots.getDocuments()) {
                                        Map<String, Object> mantenimientoData = new HashMap<>();
                                        mantenimientoData.put("costo", mantenimientoDoc.getString("costo"));
                                        mantenimientoData.put("fecha", mantenimientoDoc.getString("fecha"));
                                        mantenimientoData.put("fecha_prox", mantenimientoDoc.getString("fecha_prox"));
                                        mantenimientoData.put("id_tipo", mantenimientoDoc.getString("id_tipo"));
                                        mantenimientoData.put("km_actual", mantenimientoDoc.getString("km_actual"));
                                        mantenimientoData.put("km_prox", mantenimientoDoc.getString("km_prox"));
                                        mantenimientoData.put("notas", mantenimientoDoc.getString("notas"));
                                        mantenimientos.add(mantenimientoData);
                                    }

                                    // Llamar a función para enviar to do al webhook
                                    enviarDatosAlWebhook(placa, marca, modelo, mantenimientos);
                                });
                    }
                });
    }


    private void enviarDatosAlWebhook(String placa, String marca, String modelo, List<Map<String, Object>> mantenimientos) {
        OkHttpClient client = new OkHttpClient();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("placa", placa);
            jsonObject.put("marca", marca);
            jsonObject.put("modelo", modelo);
//            jsonObject.put("sistema", sistema);
            jsonObject.put("mantenimientos", new JSONArray(mantenimientos));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(
                jsonObject.toString(), MediaType.get("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url("https://hook.us2.make.com/9mac9nalkbw8r5awv7wksqqflmzp9r4w") // <- reemplaza con tu URL real
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Webhook", "Error al enviar al webhook", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String respuesta = response.body().string();
                    runOnUiThread(() -> {
                        mostrarDialogoRecomendacion(respuesta); // o tu TextView
                    });
                } else {
                    Log.e("Webhook", "Respuesta fallida: " + response.code());
                }
            }
        });
    }


    private void mostrarDialogoRecomendacion(String mensaje) {
        textoRecomendacion.setText(""); // Limpiar texto anterior

        final int[] index = {0};
        final Handler handler = new Handler();

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (index[0] < mensaje.length()) {
                    textoRecomendacion.append(String.valueOf(mensaje.charAt(index[0])));
                    index[0]++;
                    handler.postDelayed(this, 30); // Velocidad: 30 ms por letra
                }
            }
        };

        handler.post(runnable);
    }


    private void obtenerPlacasPorUserID(String userId) {
        mFirestore.collection("Vehiculo")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Car> listaVehiculos = new ArrayList<>();

                        listaVehiculos.add(new Car("Elige tu placa"));

                        for (DocumentSnapshot document : task.getResult().getDocuments()) {
                            String placa = document.getString("placa");
//                            String idCarro = document.getString("id");
                            if (placa != null ) {
                                listaVehiculos.add(new Car(placa));
                            }
                        }

                        // Adaptador para el spinner de placas
                        ArrayAdapter<Car> adapter = new ArrayAdapter<Car>(this, R.layout.spinner_selected, listaVehiculos) {
                            @Override
                            public View getView(int position, View convertView, ViewGroup parent) {
                                // Vista para el ítem seleccionado
                                if (convertView == null) {
                                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_selected, parent, false);
                                }

                                TextView text = convertView.findViewById(R.id.text);
//                                ImageView icon = convertView.findViewById(R.id.icon);

                                Car car = getItem(position);
                                if (car != null) {
                                    text.setText(car.getPlaca());
//                                    icon.setImageResource(R.drawable.ic_spinner); // o cualquier icono que quieras
                                }

                                return convertView;
                            }

                            @Override
                            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                                // Vista para el ítem en el dropdown
                                if (convertView == null) {
                                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_dropdown, parent, false);
                                }

                                TextView text = convertView.findViewById(R.id.text);
//                                ImageView icon = convertView.findViewById(R.id.icon);

                                Car car = getItem(position);
                                if (car != null) {
                                    text.setText(car.getPlaca());
//                                    icon.setImageResource(R.drawable.ic_spinner); // opcional: cambiar ícono por item
                                }

                                return convertView;
                            }
                        };
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerPlacas.setAdapter(adapter);

                    } else {
                        Log.e("Firestore", "Error getting vehicles: ", task.getException());
                    }
                });
    }
}
