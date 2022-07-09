package com.example.pulsat.adapter;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pulsat.R;
import com.example.pulsat.event.RdvClickEvent;
import com.example.pulsat.model.Rdv;

import org.greenrobot.eventbus.EventBus;

public class ViewHolderRdvByAdress extends RecyclerView.ViewHolder implements View.OnClickListener{
    private final TextView duree,arrivee,depart,date;
    private Rdv rdv;
    private RdvByAdressAdapter.OnItemClickListener listener;

    public void setOnItemClickListener(RdvByAdressAdapter.OnItemClickListener l,Rdv rdv) {
        Log.d("ClickListener", "C");
        this.listener = l;
        this.rdv = rdv;
    }
    @Override public void onClick(View v) {
        Log.d("ClickListener", "4");
        EventBus.getDefault().post(new RdvClickEvent(rdv));
    }

    public ViewHolderRdvByAdress(@NonNull View itemView) {
        super(itemView);
        duree = itemView.findViewById(R.id.duree);
        arrivee = itemView.findViewById(R.id.arrivee);
        depart = itemView.findViewById(R.id.depart);
        date = itemView.findViewById(R.id.date);
        itemView.setOnClickListener(this);
    }
    public TextView getDuree(){return duree;}
    public TextView getArrivee(){return arrivee;}
    public TextView getDepart(){return depart;}
    public TextView getDate(){return date;}
}
