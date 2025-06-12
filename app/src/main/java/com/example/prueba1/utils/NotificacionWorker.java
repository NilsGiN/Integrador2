package com.example.prueba1.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.prueba1.R;
import com.example.prueba1.views.MainActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NotificacionWorker extends Worker {

    private FirebaseFirestore firestore;

    public NotificacionWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public Result doWork() {
        comprobarMantenimientos();

        // El trabajo se ejecuta de forma asíncrona, pero WorkManager espera una respuesta sincrónica,
        // así que retornamos success inmediatamente (esto se puede mejorar con CoroutineWorker si usas Kotlin).
        return Result.success();
    }

    private void comprobarMantenimientos() {
        firestore.collection("Mantenimiento").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String fechaProx = doc.getString("fecha_prox");
                        String carId = doc.getString("carId");
                        String idTipo = doc.getString("id_tipo");

                        if (fechaProx != null && carId != null && idTipo != null) {
                            if (esFechaDentroDeTresDias(fechaProx) && Boolean.FALSE.equals(doc.getBoolean("notificado"))) {
                                // Obtener tipo de mantenimiento
                                firestore.collection("Tipo_Mantenimiento").whereEqualTo("id", idTipo).get()
                                        .addOnSuccessListener(tipoQuery -> {
                                            if (!tipoQuery.isEmpty()) {
                                                String tipoMantenimiento = tipoQuery.getDocuments().get(0).getString("nombre");

                                                // Obtener placa del auto
                                                firestore.collection("Vehiculo").document(carId).get()
                                                        .addOnSuccessListener(vehiculoDoc -> {
                                                            if (vehiculoDoc.exists()) {
                                                                String placa = vehiculoDoc.getString("placa");

                                                                String mensaje = "Tu auto con placa " + (placa != null ? placa : "desconocida") +
                                                                        " tiene un mantenimiento próximo de tipo '" + tipoMantenimiento +
                                                                        "' el día " + fechaProx;

                                                                mostrarNotificacion("Mantenimiento próximo", mensaje);

                                                                // 🔔 Marcar como notificado
                                                                firestore.collection("Mantenimiento").document(doc.getId())
                                                                        .update("notificado", true);
                                                            }
                                                        });
                                            }
                                        });
                            }
                        }
                    }
                });
    }


    private boolean esFechaDentroDeTresDias(String fechaStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
        try {
            Date fechaProx = sdf.parse(fechaStr);

            Calendar hoy = Calendar.getInstance();
            Calendar tresDiasAntes = Calendar.getInstance();
            tresDiasAntes.add(Calendar.DAY_OF_YEAR, 3);

            return fechaProx != null &&
                    !fechaProx.before(hoy.getTime()) &&
                    !fechaProx.after(tresDiasAntes.getTime());

        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }

    }

    private void mostrarNotificacion(String titulo, String mensaje) {
        Context context = getApplicationContext();
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Canal (solo se crea una vez)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "canal_mantenimiento",
                    "Recordatorios de Mantenimiento",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.enableVibration(true);
            channel.setDescription("Notificaciones para mantenimientos próximos de tu vehículo");
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            Uri sonido = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            channel.setSound(sonido, audioAttributes);

            notificationManager.createNotificationChannel(channel);
        }

        // Intent al tocar la notificación (puedes redirigir a tu actividad principal o una específica)
        Intent intent = new Intent(context, MainActivity.class); // o tu pantalla de mantenimientos
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        // Sonido y vibración
        Uri sonido = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        long[] vibracion = {0, 500, 250, 500}; // patrón vibratorio

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "canal_mantenimiento")
                .setSmallIcon(R.drawable.icon_plus) // usa tu propio ícono
                .setContentTitle(titulo)
                .setContentText(mensaje)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
//                .setSound(sonido)
                .setVibrate(vibracion)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mensaje));

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

}
