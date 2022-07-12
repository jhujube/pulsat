package com.example.pulsat.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pulsat.R;
import com.example.pulsat.model.Rdv;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<ViewHolder> {
    private List<Rdv> listOfRdvs;

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Rdv rdv);
    }
    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_rdv, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        Rdv rdv = listOfRdvs.get(position);
        viewHolder.getRue().setText(rdv.getStreet());
        viewHolder.getVille().setText(rdv.getTown());
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("H:mm");
        viewHolder.getArrivee().setText(formatter.format(new Date(rdv.getArrival())));

        long duree = rdv.getElapsedTime();
        if (duree > 0) {
            viewHolder.getDepart().setText(formatter.format(new Date(rdv.getArrival()+duree*1000)));
            long dureeH = duree / 3600;
            long dureeM = (duree - (dureeH*3600))/60;
            @SuppressLint("DefaultLocale")
            String chrono = String.format("%d:%02d", dureeH, dureeM);
            viewHolder.getDuree().setText(chrono);
        } else{
            viewHolder.getDuree().setText("-:--");
            viewHolder.getDepart().setText("-:--");
        }
        //Log.d("ClickListener", "B");
        viewHolder.setOnItemClickListener(this.listener, rdv);
    }

    @Override
    public int getItemCount() {
        if (listOfRdvs != null) {
            return listOfRdvs.size();
        } else { return 0;}
    }

    public void updateRdvsList(List<Rdv> rdvArrayList) {
        this.listOfRdvs = rdvArrayList;
        this.listOfRdvs.removeAll(Collections.singleton(null));
    }

    /*
    public static class RdvDiff extends DiffUtil.ItemCallback<Rdv> {

        @Override
        public boolean areItemsTheSame(@NonNull Rdv oldItem, @NonNull Rdv newItem) {
            return oldItem == newItem;
        }

        @SuppressLint("DiffUtilEquals")
        @Override
        public boolean areContentsTheSame(@NonNull Rdv oldItem, @NonNull Rdv newItem) {
            return oldItem.equals(newItem);
        }
    }
*/
}

