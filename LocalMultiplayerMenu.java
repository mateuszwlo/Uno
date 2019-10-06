package com.mateusz.uno;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import static java.lang.System.in;

public class LocalMultiplayerMenu extends AppCompatActivity {

    private int REQUEST_ENABLE_BT;
    private Button hostGameBtn;
    private ListView hostedGamesListView;
    private ArrayList<LocalGame> hostedGamesList = new ArrayList<>(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_multiplayer_menu);

        hostGameBtn = findViewById(R.id.hostGameBtn);
        hostedGamesListView = findViewById(R.id.hostedGamesListView);

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null) Toast.makeText(this, "No Bluetooth :(", Toast.LENGTH_LONG).show();

        if(!bluetoothAdapter.isEnabled()) startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver, filter);
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                Parcelable[] parcelables = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_DEVICE);
                BluetoothDevice[] device = new BluetoothDevice[parcelables.length];
                System.arraycopy(parcelables, 0, device, 0, parcelables.length);

                String deviceName = device[0].getName();
                String deviceHardwareAddress = device[0].getAddress();

                Toast.makeText(LocalMultiplayerMenu.this, deviceName + "\n" + deviceHardwareAddress, Toast.LENGTH_SHORT).show();
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(receiver);
    }
}
