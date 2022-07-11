package com.example.pulsat.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.pulsat.R;
import com.example.pulsat.event.ReloadSettingsEvent;
import com.example.pulsat.event.StartInterventionEvent;
import com.example.pulsat.foreground.ForegroundLocation;
import com.example.pulsat.viewmodel.LocationViewModel;

import org.greenrobot.eventbus.EventBus;

public class SettingsActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_SELECT_BT = 1;
    public static final int REQUEST_CODE_RDV_LIFETIME = 2;
    private TextView selectedBtDevice;
    private TextView selectedRdvLifetime;
    private String btDevice;
    private Integer rdvLifetime;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadSettings();

        setContentView(R.layout.activity_settings);

        ConstraintLayout selectBtDevice = findViewById(R.id.bluetooth);
        selectBtDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent selectBtDeviceIntent = new Intent(SettingsActivity.this,SelectBtDevice.class);
                startActivityForResult(selectBtDeviceIntent,REQUEST_CODE_SELECT_BT);
            }
        });
        selectedBtDevice = findViewById(R.id.bluetoothSelectedName);
        selectedBtDevice.setText(getBtDeviceName(btDevice));

        ConstraintLayout selectRdvLifetime = findViewById(R.id.rdvLifetime);
        selectRdvLifetime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent selectLifetimeRdvIntent = new Intent(SettingsActivity.this,SelectLifetimeRdvActivity.class);
                selectLifetimeRdvIntent.putExtra("lifetimeSelected",rdvLifetime);
                startActivityForResult(selectLifetimeRdvIntent,REQUEST_CODE_RDV_LIFETIME);
            }
        });


        selectedRdvLifetime = findViewById(R.id.lifetime);
        selectedRdvLifetime.setText(lifeTimeToString(rdvLifetime));

        Button bt_cancel = findViewById(R.id.bt_cancel);
        bt_cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent mainActivityIntent = new Intent(SettingsActivity.this,MainActivity.class);
                startActivity(mainActivityIntent);
                finish();
            }
        });
        Button bt_ok = findViewById(R.id.bt_ok);
        bt_ok.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                saveSettings();
                EventBus.getDefault().post(new ReloadSettingsEvent());
                finish();
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_BT) {
            if (resultCode == Activity.RESULT_OK) {
                btDevice = data.getStringExtra("SelectedDevice");
                selectedBtDevice.setText(getBtDeviceName(btDevice));
            }
        }
        if (requestCode == REQUEST_CODE_RDV_LIFETIME) {
            if (resultCode == Activity.RESULT_OK) {
                rdvLifetime = data.getIntExtra("rdvLifetime",0);
                selectedRdvLifetime.setText(lifeTimeToString(rdvLifetime));
            }
        }
    }
    private void saveSettings(){
        SharedPreferences sharedPreferences= getSharedPreferences("pulsatSettings", Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("selectedBtDevice", btDevice);
        editor.putInt("selectedLifetime", rdvLifetime);
        // Save.
        editor.apply();
        Toast.makeText(this,"Réglages sauvegardés!",Toast.LENGTH_LONG).show();
    }
    private void loadSettings(){
        SharedPreferences sharedPreferences = getSharedPreferences("pulsatSettings", Context.MODE_PRIVATE);

        if(sharedPreferences!= null) {
            btDevice = sharedPreferences.getString("selectedBtDevice", "");
            rdvLifetime = sharedPreferences.getInt("selectedLifetime", 0);
        } else {
            Toast.makeText(this,"Use the default Domo setting",Toast.LENGTH_LONG).show();
        }
    }
    private String getBtDeviceName(String btDevice){
        if (btDevice != null){
           String [] device =  btDevice.split("\n");
           return device[0];
        }
        return "";
    }
    private String lifeTimeToString(int rdvLifetime){
        switch (rdvLifetime){
            case 0:
                return "Illimitée";
            case 1:
                return "1 journée";
            case 7:
                return "1 semaine";
            case 14:
                return "2 semaines";
            case 21:
                return "3 semaines";
            case 31:
                return "1 mois";
            case 62:
                return "2 mois";
            case 183:
                return "6 mois";
            case 365:
                return "1 an";
        }
        return "?";
    }
}
