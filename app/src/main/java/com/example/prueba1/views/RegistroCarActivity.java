package com.example.prueba1.views;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prueba1.R;
import com.example.prueba1.model.Car;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RegistroCarActivity extends AppCompatActivity {
    // Declaración de variables para los elementos de la interfaz
    private ImageButton atras_main;
    private Button btnRegistrar;

    private Spinner spinnermarca;
    private Spinner spinnermodelo;
    private EditText inputplaca;
    private EditText inputanio;
    private RadioGroup radioGroupSistema;

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    String userId = firebaseAuth.getCurrentUser().getUid();
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Activamos el modo Edge-to-Edge (para manejar los márgenes)
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registro_car);

        // Inicializamos el radio group de sistema (Automático o Mecánico)
        radioGroupSistema = findViewById(R.id.radioGroupSistema);

        // Inicialización de botón para regresar a la pantalla anterior
        atras_main = findViewById(R.id.Atras_main);
        atras_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegistroCarActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // Inicializamos la instancia de Firestore
        firestore = FirebaseFirestore.getInstance();

        // Inicialización de los Spinners y campos de texto
        spinnermarca = findViewById(R.id.Marca);
        spinnermodelo = findViewById(R.id.Modelo);
        inputplaca = findViewById(R.id.Placa);
        inputanio = findViewById(R.id.Anio);

        // Cargamos las marcas y modelos desde un archivo JSON
        Map<String, List<String>> carMap = loadCarData();
        List<String> marcas = new ArrayList<>(carMap.keySet());
        Collections.sort(marcas); // Ordenamos las marcas alfabéticamente

        // Adaptador para el Spinner de marcas
        ArrayAdapter<String> marcaAdapter = new ArrayAdapter<String>(this, R.layout.spinner_selected, marcas) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // Vista para el ítem seleccionado en el Spinner
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_selected, parent, false);
                }

                TextView text = convertView.findViewById(R.id.text);
                String marca = getItem(position);
                if (marca != null) {
                    text.setText(marca);
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
                String marca = getItem(position);
                if (marca != null) {
                    text.setText(marca);
                }

                return convertView;
            }
        };
        marcaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnermarca.setAdapter(marcaAdapter);

        // Adaptador inicial para el Spinner de modelos (vacío por defecto)
        ArrayAdapter<String> adapterModelo = new ArrayAdapter<String>(this, R.layout.spinner_selected, new ArrayList<>()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // Vista para el ítem seleccionado en el Spinner
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.spinner_selected, parent, false);
                }

                TextView text = convertView.findViewById(R.id.text);
                String modelo = getItem(position);
                if (modelo != null) {
                    text.setText(modelo);
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
                String modelo = getItem(position);
                if (modelo != null) {
                    text.setText(modelo);
                }

                return convertView;
            }
        };
        adapterModelo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnermodelo.setAdapter(adapterModelo);

        // Cuando selecciona una marca, se actualizan los modelos correspondientes en el segundo Spinner
        spinnermarca.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String marcaSeleccionada = marcas.get(position);
                List<String> modelos = carMap.get(marcaSeleccionada);
                if (modelos != null) {
                    adapterModelo.clear();
                    adapterModelo.addAll(modelos);
                    adapterModelo.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No hacer nada
            }
        });

        // Convierte la placa a mayúsculas automáticamente
        inputplaca.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Convertimos el texto a mayúsculas
                String upperCaseText = s.toString().toUpperCase();
                if (!upperCaseText.equals(s.toString())) {
                    inputplaca.setText(upperCaseText);
                    inputplaca.setSelection(upperCaseText.length()); // Colocamos el cursor al final del texto
                }
            }
        });

        // Configuración del botón de registro
        btnRegistrar = findViewById(R.id.BtnRegistrar);
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String marca = spinnermarca.getSelectedItem().toString();
                String modelo = spinnermodelo.getSelectedItem().toString();
                String placa = inputplaca.getText().toString();
                String anio = inputanio.getText().toString();
                int selectedSistemaId = radioGroupSistema.getCheckedRadioButtonId();

                // Verificar si se seleccionó un sistema
                if (selectedSistemaId == -1) {
                    Toast.makeText(RegistroCarActivity.this, "Debes seleccionar un sistema (Automático o Mecánico)", Toast.LENGTH_SHORT).show();
                    return;
                }
                RadioButton selectedRadioButton = findViewById(selectedSistemaId);
                String sistema = selectedRadioButton.getText().toString();

                // Verificar si todos los campos están llenos
                if (marca.isEmpty() || modelo.isEmpty() || placa.isEmpty() || anio.isEmpty() || sistema.isEmpty()) {
                    Toast.makeText(RegistroCarActivity.this, "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show();
                } else {
                    // Guardar los datos en la base de datos de Firebase
                    Map<String, Object> CarMap = new HashMap<>();
                    CarMap.put("marca", marca);
                    CarMap.put("modelo", modelo);
                    CarMap.put("placa", placa);
                    CarMap.put("anio", anio);
                    CarMap.put("sistema", sistema);
                    CarMap.put("userId", userId);

                    firestore.collection("Vehiculo").add(CarMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(RegistroCarActivity.this, "Registro de auto guardado", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegistroCarActivity.this, MainActivity.class));
                            } else {
                                String errorMessage = task.getException() != null ? task.getException().getMessage() : "Error desconocido";
                                Toast.makeText(RegistroCarActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RegistroCarActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        // Ajuste para el sistema de ventanas (márgenes)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Cargar los datos de marcas y modelos desde un archivo JSON
    private Map<String, List<String>> loadCarData() {
        Map<String, List<String>> carMap = new HashMap<>();
        try {
            InputStream is = getAssets().open("car_list_map.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            String json = new String(buffer, "UTF-8");
            JSONObject jsonObject = new JSONObject(json);

            Iterator<String> keys = jsonObject.keys();
            while (keys.hasNext()) {
                String brand = keys.next();
                JSONArray modelsArray = jsonObject.getJSONArray(brand);
                List<String> models = new ArrayList<>();
                for (int i = 0; i < modelsArray.length(); i++) {
                    models.add(modelsArray.getString(i));
                }
                carMap.put(brand, models);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return carMap;
    }
}
