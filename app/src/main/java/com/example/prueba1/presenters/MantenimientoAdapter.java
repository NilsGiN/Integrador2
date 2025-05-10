package com.example.prueba1.presenters;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.prueba1.R;
import com.example.prueba1.model.Mantenimiento;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class MantenimientoAdapter extends FirestoreRecyclerAdapter<Mantenimiento, MantenimientoAdapter.ViewHolder> {
    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public MantenimientoAdapter(@NonNull FirestoreRecyclerOptions<Mantenimiento> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder viewHolder, int i, @NonNull Mantenimiento mantenimiento) {
        viewHolder.fecha_actual.setText(mantenimiento.getFecha());
        viewHolder.fecha_prox.setText(mantenimiento.getFecha_prox());
        viewHolder.km_actual.setText(mantenimiento.getKm_actual());
        viewHolder.km_prox.setText(mantenimiento.getKm_prox());
        viewHolder.costo.setText(mantenimiento.getCosto());
        viewHolder.notas.setText(mantenimiento.getNotas());

        // Agregar LongClickListener para eliminar
        viewHolder.itemView.setOnLongClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Eliminar mantenimiento")
                    .setMessage("¿Está seguro de que desea eliminar este mantenimiento?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        // Eliminar el documento de Firestore
                        getSnapshots().getSnapshot(i).getReference().delete()
                                .addOnSuccessListener(aVoid -> {
                                    // Opcional: Mostrar un mensaje de éxito
                                })
                                .addOnFailureListener(e -> {
                                    // Opcional: Manejar errores
                                });
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true; // Indica que el evento fue manejado
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_mantenimiento_single, parent, false);
        return new ViewHolder(v);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView fecha_actual, fecha_prox, km_actual, km_prox, costo, notas;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            fecha_actual = itemView.findViewById(R.id.fecha_actual);
            fecha_prox = itemView.findViewById(R.id.fecha_prox);
            km_actual = itemView.findViewById(R.id.km_actual);
            km_prox = itemView.findViewById(R.id.km_prox);
            costo = itemView.findViewById(R.id.costo);
            notas = itemView.findViewById(R.id.notas);
        }
    }
}
