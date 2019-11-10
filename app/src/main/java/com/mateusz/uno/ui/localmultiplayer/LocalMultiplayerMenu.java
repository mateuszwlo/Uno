package com.mateusz.uno.ui.localmultiplayer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mateusz.uno.R;

public class LocalMultiplayerMenu extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private Button hostGameBtn;
    private ListView gamesListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_multiplayer_menu);

        initialiseViews();
        getAvailableGames();
    }

    private void initialiseViews() {
        hostGameBtn = findViewById(R.id.hostGameBtn);
        gamesListView = findViewById(R.id.gamesListView);
    }

    private void getAvailableGames() {

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null){
            //Device doesn't support Bluetooth
            Toast.makeText(this, "Bluetooth is not supported on your device.", Toast.LENGTH_LONG).show();
            return;
        }

        if(!bluetoothAdapter.isEnabled()){
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        boolean started = bluetoothAdapter.startDiscovery();
        Log.d("STARTED: ", String.valueOf(started));
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d("!", "onReceive Called");

            String action = intent.getAction();

            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                //Bluetooth Devices Found
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if(device == null){
                    Toast.makeText(LocalMultiplayerMenu.this, "No devices were found", Toast.LENGTH_LONG).show();
                    return;
                }

                Toast.makeText(LocalMultiplayerMenu.this, device.getName(), Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }
}
