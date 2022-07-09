package com.example.pulsat.adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pulsat.R;
import com.example.pulsat.model.Rdv;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class RdvByAdressAdapter extends RecyclerView.Adapter<ViewHolderRdvByAdress>{
    List<Rdv> rdvByAdress = new ArrayList<>();
    private RdvByAdressAdapter.OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Rdv rdv);
    }
    @NonNull
    @Override
    public ViewHolderRdvByAdress onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rdvbyadress, parent, false);
        return new ViewHolderRdvByAdress(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderRdvByAdress holder, int position) {
        Rdv rdv = rdvByAdress.get(position);

        long duree = rdv.getElapsedTime();
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat formatter = new SimpleDateFormat("H:mm");
        holder.getArrivee().setText(formatter.format(new Date(rdv.getArrival())));
        holder.getDate().setText(rdv.getDate());

        if (duree > 0) {
            holder.getDepart().setText(formatter.format(new Date(rdv.getArrival()+duree*1000)));
            long dureeH = duree / 3600;
            long dureeM = (duree - (dureeH*3600))/60;
            @SuppressLint("DefaultLocale")
            String chrono = String.format("%d:%02d", dureeH, dureeM);
            holder.getDuree().setText(chrono);
        } else{
            holder.getDuree().setText("-:--");
            holder.getDepart().setText("-:--");
        }
        holder.setOnItemClickListener(this.listener, rdv);
    }

    @Override
    public int getItemCount() {
        if (rdvByAdress != null) {
            return rdvByAdress.size();
        }else{ return 0;}
    }

    public void updateAdressList(List<Rdv> allAdressList) {
        this.rdvByAdress = allAdressList;
        this.rdvByAdress.removeAll(Collections.singleton(null));
        Log.d("RDVBYADRESSACTIVITY", "list adresses..."+rdvByAdress.size());
    }
}
