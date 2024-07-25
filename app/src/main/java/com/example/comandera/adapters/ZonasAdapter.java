package com.example.comandera.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.comandera.R;
import com.example.comandera.utils.ZonaVenta;

import java.util.List;

public class ZonasAdapter extends RecyclerView.Adapter<ZonasAdapter.ZonaViewHolder> {

    private List<ZonaVenta> zonas;

    public ZonasAdapter(List<ZonaVenta> zonas) {
        this.zonas = zonas;
    }

    @NonNull
    @Override
    public ZonaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_zona, parent, false);
        return new ZonaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ZonaViewHolder holder, int position) {
        ZonaVenta zona = zonas.get(position);
        holder.textViewZona.setText(zona.getZona());
    }

    @Override
    public int getItemCount() {
        return zonas.size();
    }

    public static class ZonaViewHolder extends RecyclerView.ViewHolder {
        TextView textViewZona;

        public ZonaViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewZona = itemView.findViewById(R.id.textViewZona);
        }
    }
}
