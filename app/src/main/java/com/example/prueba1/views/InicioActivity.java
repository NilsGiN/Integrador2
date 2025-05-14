package com.example.prueba1.views;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.prueba1.R;

public class InicioActivity extends AppCompatActivity {

    private LinearLayout pantallaBlanca;
    private Button btnEmpezar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        pantallaBlanca = findViewById(R.id.pantallaBlanca);
        btnEmpezar = findViewById(R.id.btnEmpezar);

        // Inicialmente ocultamos la pantalla blanca
        pantallaBlanca.setVisibility(View.INVISIBLE);

        // Esperar 3 segundos para iniciar la animación
        new Handler().postDelayed(() -> {
            pantallaBlanca.setVisibility(View.VISIBLE);

            // Asegurar que el layout tenga medida antes de animar
            pantallaBlanca.post(() -> {
                pantallaBlanca.setTranslationY(pantallaBlanca.getHeight() + 200); // posición inicial fuera de pantalla
                pantallaBlanca.animate()
                        .translationY(0) // subir a su posición original
                        .setDuration(600)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .start();
            });
        }, 1000); // 1 segundo

        btnEmpezar.setOnClickListener(v -> {
            Intent intent = new Intent(InicioActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
