package com.example.prueba1.presenters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.prueba1.R;
import com.example.prueba1.model.Car;
import com.example.prueba1.utils.LogosManager;
import com.example.prueba1.views.MosaicoActivity;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

// Clase CarAdapter que extiende de FirestoreRecyclerAdapter para gestionar el RecyclerView
public class CarAdapter extends FirestoreRecyclerAdapter<Car, CarAdapter.ViewHolder> implements View.OnClickListener {

    // Variables de la clase
    private View.OnClickListener listener;
    private Context context;

    private int[] cardColors;

    // Constructor que recibe las opciones de Firestore y el contexto
    public CarAdapter(@NonNull FirestoreRecyclerOptions<Car> options, Context context) {
        super(options);
        this.context = context;
        LogosManager.initialize(context); // Inicializa logos al instanciar el adapter

        // Inicializa los colores desde colors.xml
        cardColors = new int[]{
                context.getResources().getColor(R.color.back_cardview_1),
                context.getResources().getColor(R.color.back_cardview_2),
                context.getResources().getColor(R.color.back_cardview_3)
        };
    }

    // Método para enlazar los datos del modelo (Car) con las vistas del ViewHolder
    @Override
    protected void onBindViewHolder(@NonNull CarAdapter.ViewHolder viewHolder, int i, @NonNull Car car) {
        // Establecer los valores de los campos del vehículo
        viewHolder.placa.setText(car.getPlaca());
        viewHolder.marca.setText(car.getMarca());
        viewHolder.modelo.setText(car.getModelo());
        viewHolder.sistema.setText(car.getSistema());

        // Mostrar logo de la marca
        String nombreMarca = car.getMarca().toLowerCase();
        String logoUrl = LogosManager.getLogoUrl(nombreMarca);
        if (logoUrl != null) {
            Glide.with(context)
                    .load(logoUrl)
                    .into(viewHolder.logoMarca); // Cargar el logo de la marca
        } else {
            viewHolder.logoMarca.setImageResource(R.drawable.default_logo_car); // Logo por defecto si no se encuentra el logo
        }

        // Obtener el ID del documento
        String documentId = getSnapshots().getSnapshot(i).getId();

        // Acción al hacer click en el item (redirigir a MosaicoActivity con el carId)
        viewHolder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(context, MosaicoActivity.class);
            intent.putExtra("carId", documentId);
            context.startActivity(intent);
        });

        // Acción al hacer un click largo en el item (mostrar opciones)
        viewHolder.itemView.setOnLongClickListener(view -> {
            new AlertDialog.Builder(context)
                    .setTitle("Opciones")
                    .setItems(new CharSequence[]{"Eliminar"}, (dialog, which) -> {
                        if (which == 0) eliminarCar(documentId); // Eliminar vehículo si se selecciona la opción "Eliminar"
                    })
                    .show();
            return true;
        });

        int color = cardColors[i % cardColors.length]; // rotativo
        // int color = cardColors[new Random().nextInt(cardColors.length)]; // si prefieres aleatorio
        viewHolder.relativeLayout.setBackgroundColor(color);

        // Determinar si el fondo es oscuro y ajustar color del texto
        double luminance = ColorUtils.calculateLuminance(color);
        int textColor = luminance < 0.5 ? Color.WHITE : Color.BLACK;

        // Cambiar color de texto directamente en todos los TextView
        viewHolder.placa.setTextColor(textColor);
        viewHolder.marca.setTextColor(textColor);
        viewHolder.modelo.setTextColor(textColor);
        viewHolder.sistema.setTextColor(textColor);

        int badgeRes = (luminance < 0.5) ? R.drawable.item_badge_light : R.drawable.item_badge_dark;

        viewHolder.marca.setBackgroundResource(badgeRes);
        viewHolder.modelo.setBackgroundResource(badgeRes);
        viewHolder.sistema.setBackgroundResource(badgeRes);
    }

    // Método para eliminar un vehículo de la base de datos
    private void eliminarCar(String documentId) {
        FirebaseFirestore.getInstance().collection("Vehiculo")
                .document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("CarAdapter", "Vehículo eliminado con éxito"))
                .addOnFailureListener(e -> Log.e("CarAdapter", "Error al eliminar el vehículo", e));
    }

    // Método para crear el ViewHolder y asignar el listener
    @NonNull
    @Override
    public CarAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_car_single, parent, false);
        v.setOnClickListener(this);
        return new CarAdapter.ViewHolder(v);
    }

    // Método para establecer un listener personalizado
    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    // Método para manejar el click en el ViewHolder
    @Override
    public void onClick(View view) {
        if (listener != null) {
            listener.onClick(view);
        }
    }

    // Clase ViewHolder que define las vistas de cada item del RecyclerView
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView placa, marca, modelo, sistema;
        ImageView logoMarca;
        RelativeLayout relativeLayout;

        // Constructor que asigna los elementos del layout a las variables
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            placa = itemView.findViewById(R.id.placa);
            marca = itemView.findViewById(R.id.marca);
            modelo = itemView.findViewById(R.id.modelo);
            sistema = itemView.findViewById(R.id.sistema);
            logoMarca = itemView.findViewById(R.id.logo_marca);
            relativeLayout = itemView.findViewById(R.id.relativeLayout);
        }
    }
}
