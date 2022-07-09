package com.example.pulsat.adapter;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.pulsat.R;
import com.example.pulsat.event.RdvClickEvent;
import com.example.pulsat.model.Rdv;

import org.greenrobot.eventbus.EventBus;

public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
    private final TextView rue,ville,duree,arrivee,depart;
    private CustomAdapter.OnItemClickListener listener;
    private Rdv rdv;

    public void setOnItemClickListener(CustomAdapter.OnItemClickListener l,Rdv rdv) {
        Log.d("ClickListener", "C");
        this.rdv = rdv;
        this.listener = l;
    }
    @Override public void onClick(View v) {
        Log.d("ClickListener", "4");
        EventBus.getDefault().post(new RdvClickEvent(rdv));

    }

    public ViewHolder(View view) {
        super(view);
        rue = itemView.findViewById(R.id.street);
        ville = itemView.findViewById(R.id.town);
        duree = itemView.findViewById(R.id.duree);
        arrivee = itemView.findViewById(R.id.arrivee);
        depart = itemView.findViewById(R.id.depart);
        Log.d("ClickListener", "A");
        view.setOnClickListener(this);
    }
    public TextView getRue(){return rue;}
    public TextView getVille(){return ville;}
    public TextView getDuree(){return duree;}
    public TextView getArrivee(){return arrivee;}
    public TextView getDepart(){return depart;}

}