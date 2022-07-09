package com.example.pulsat.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pulsat.R;
import com.example.pulsat.adapter.RdvByAdressAdapter;
import com.example.pulsat.event.RdvClickEvent;
import com.example.pulsat.model.Rdv;
import com.example.pulsat.viewmodel.RdvViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class RdvByAdressActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rdvbyadress);

        Intent intent = getIntent();
        Rdv rdv = (Rdv) intent.getSerializableExtra("rdv");

        TextView adresse = (TextView) findViewById(R.id.adresse);
        TextView ville = (TextView) findViewById(R.id.ville);
        adresse.setText(rdv.getStreet());
        ville.setText(rdv.getTown());

        RecyclerView recyclerView = findViewById(R.id.recyclerRdvByAdress);
        RdvByAdressAdapter rdvByAdressAdapter= new RdvByAdressAdapter();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this,
                RecyclerView.VERTICAL, // direction
                false);
        recyclerView.setLayoutManager(layoutManager); // sens)
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(rdvByAdressAdapter);

        RdvViewModel rdvViewModel = new ViewModelProvider(this).get(RdvViewModel.class);

        rdvViewModel.getLiveAdressRdvs(rdv.getAddress()).observe(this, listAdressObserver ->{
            rdvByAdressAdapter.updateAdressList(listAdressObserver);
            rdvByAdressAdapter.notifyDataSetChanged();
        });
    }

    @Override
    protected void onStart(){
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop(){
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    @SuppressWarnings("unused")
    public void onRdvClickEvent(RdvClickEvent event){
        deleteRdv(event.getRdv());
    }

    private void deleteRdv(Rdv rdv){
        RdvViewModel rdvViewModel = new ViewModelProvider(this).get(RdvViewModel.class);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Effacer le rendez-vous?")
                .setTitle("Effacer");
        builder.setPositiveButton("Effacer rdv", (dialog, id) -> {
            Log.d("MAINACTIVITY", "delete rdv n°"+rdv.getArrival());
            rdvViewModel.deleteRdv(rdv);
        });
        builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        builder.setNeutralButton("Effacer tout", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                rdvViewModel.deleteAdressRdv(rdv.getAddress());
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
