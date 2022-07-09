package com.example.pulsat.activity;

import static androidx.databinding.DataBindingUtil.setContentView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.pulsat.R;
import com.example.pulsat.adapter.CustomAdapter;
import com.example.pulsat.event.RequestAccesFineLocationPermissionEvent;
import com.example.pulsat.event.RequestBtScanPermissionEvent;
import com.example.pulsat.permission.PermissionManager;
import com.example.pulsat.permission.PermissionStatus;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Set;

public class SelectBtDevice extends AppCompatActivity {
    private final String TAG = "SELECTBTDEVICE";
    private final String SELECTED = "   *";
    private String btDevice;
    private ListView listView;
    private ArrayList<String> mDeviceList = new ArrayList<>();

    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();
            if (action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {

                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.ERROR);

                if (state == BluetoothAdapter.STATE_CONNECTED){
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    int pos = 0;
                    for (String btDevice : mDeviceList) {
                        if (btDevice.contains(deviceHardwareAddress)){
                            btDevice += " *";
                        }
                        mDeviceList.set(pos,btDevice);
                        pos++;
                    }

                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadSettings();

        setContentView(R.layout.activity_selectbtdevice);

        listView = findViewById(R.id.listView);
        mDeviceList = PairedBtDevicesList();
        listView.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1,mDeviceList));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                String selectedDevice = mDeviceList.get(index);
                selectedDevice.replace(SELECTED,"");
                Intent intent = new Intent();
                intent.putExtra("SelectedDevice", selectedDevice);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    private ArrayList<String> PairedBtDevicesList(){
        ArrayList<String> mDeviceList = new ArrayList<>();
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
        }

        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String pairedDevice = device.getName() + "\n" + device.getAddress();
                if (pairedDevice.equals(btDevice))
                    pairedDevice += SELECTED;
                mDeviceList.add(pairedDevice);
            }
        }
        return mDeviceList;
    }
    private void loadSettings(){
        SharedPreferences sharedPreferences= getSharedPreferences("pulsatSettings", Context.MODE_PRIVATE);

        if(sharedPreferences!= null) {
            btDevice = sharedPreferences.getString("selectedBtDevice", "");
        }
    }

}
