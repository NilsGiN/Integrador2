package com.example.prueba1.views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.prueba1.R;
//https://lottiefiles.com/blog/working-with-lottie-animations/getting-started-with-lottie-animations-in-android-app
public class InicioActivity extends AppCompatActivity {

    VideoView videoView;
    private LinearLayout pantallaBlanca;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

//        videoView = findViewById(R.id.videoView);
//        pantallaBlanca = findViewById(R.id.pantallaBlanca);
        Button btnEmpezar = findViewById(R.id.btnEmpezar);
//
//        // Ruta del video en /res/raw/
//        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video); // tu_video.mp4 en res/raw
//        videoView.setVideoURI(uri);
//        videoView.start();
//
//        // Al finalizar el video, mostrar pantalla blanca con animación
//        videoView.setOnCompletionListener(mp -> {
//            pantallaBlanca.setVisibility(View.VISIBLE);
//            pantallaBlanca.setTranslationY(pantallaBlanca.getHeight()); // por si el layout aún no está medido
//
//            pantallaBlanca.post(() -> {
//                pantallaBlanca.setTranslationY(pantallaBlanca.getHeight()+200);
//                pantallaBlanca.animate()
//                        .translationY(0)
//                        .setDuration(600)
//                        .setInterpolator(new AccelerateDecelerateInterpolator())
//                        .start();
//            });
//        });

        btnEmpezar.setOnClickListener(v -> {
            Intent intent = new Intent(InicioActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}