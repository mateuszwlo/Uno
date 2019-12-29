package com.mateusz.uno.localmultiplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.mateusz.uno.R;

import java.util.ArrayList;
import java.util.List;

public class LocalMultiplayerMenu extends AppCompatActivity implements View.OnClickListener{

    private Button hostGameBtn;
    private ListView availableGamesListView;

    private BluetoothAdapter bluetoothAdapter;
    private List<BluetoothDevice> bluetoothDevicesList = new ArrayList<>(0);
    private List<String> bluetoothNamesList = new ArrayList<>(0);
    private ArrayAdapter<String> listViewAdapter;
    private boolean bluetoothReady = false;

    private BluetoothConnectionService bluetoothConnectionService;
    private BluetoothDevice pairedDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_local_multiplayer_menu);

        initialiseViews();
        setUpListView();
        setUpBluetooth();
    }

    private void initialiseViews() {
        hostGameBtn = findViewById(R.id.hostGameBtn);
        hostGameBtn.setOnClickListener(this);

        availableGamesListView = findViewById(R.id.availableGamesListView);
    }

    private void setUpListView(){
        listViewAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, bluetoothNamesList);
        availableGamesListView.setAdapter(listViewAdapter);

        availableGamesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                connectToGame(bluetoothDevicesList.get(position));
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.hostGameBtn:
                hostGame();
                break;
        }
    }

    private void setUpBluetooth() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            //Bluetooth not supported
            Toast.makeText(this, "Bluetooth is not supported on your device.", Toast.LENGTH_LONG).show();
            return;
        }

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);

            IntentFilter enableBTFilter = new IntentFilter(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            registerReceiver(enableBtReceiver, enableBTFilter);
        }

        //Enable Discovery
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivity(discoverableIntent);

        IntentFilter discoverBTFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(discoverabilityReceiver, discoverBTFilter);

        //Discover Devices
        checkBTPermissions();
        bluetoothAdapter.startDiscovery();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoveryReceiver, filter);

        //Check for paired devices
        IntentFilter pairingFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(pairingReceiver, pairingFilter);

        bluetoothConnectionService = BluetoothConnectionService.getInstance(this);

        bluetoothReady = true;
    }

    private void checkBTPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int permissionCheck = 0;
            permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");

            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if (permissionCheck != 0) {

                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            }
        } else {
            Log.d("BT", "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
        }
    }

    private void hostGame(){
        if(!bluetoothReady) return;

        bluetoothConnectionService.startServer();
    }

    private void connectToGame(BluetoothDevice device){
        bluetoothAdapter.cancelDiscovery();

        Log.d("BluetoothMenu", "Trying to pair with device: " + device.getName());
        device.createBond();

        pairedDevice = device;

        bluetoothConnectionService = BluetoothConnectionService.getInstance(this);
        bluetoothConnectionService.connectToServer(pairedDevice);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(enableBtReceiver);
        unregisterReceiver(discoveryReceiver);
        unregisterReceiver(discoverabilityReceiver);
    }

    private final BroadcastReceiver enableBtReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action == null) return;

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d("BT", "EnableBTReceiver: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d("BT", "EnableBTReceiver: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d("BT", "EnableBTReceiver: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d("BT", "EnableBTReceiver: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    private final BroadcastReceiver discoverabilityReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action == null) return;

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {

                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch (mode) {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d("BT", "discoverabilityReceiver: Discoverability Enabled.");
                        break;
                    //Device not in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d("BT", "discoverabilityReceiver: Discoverability Disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d("BT", "discoverabilityReceiver: Discoverability Disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d("BT", "discoverabilityReceiver: Connecting....");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d("BT", "discoverabilityReceiver: Connected.");
                        break;
                }

            }
        }
    };

    private BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action == null) return;

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();

                Log.d("BLUETOOTH DEVICE:", deviceName + "\n" + deviceHardwareAddress);

                bluetoothDevicesList.add(device);
                bluetoothNamesList.add(device.getName());
                listViewAdapter.notifyDataSetChanged();
            }
        }
    };

    private final BroadcastReceiver pairingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                switch(device.getBondState()){
                    case BluetoothDevice.BOND_BONDED:
                        Log.d("BT", "onReceive: BOND BONDED");
                        pairedDevice = device;
                        break;
                    case BluetoothDevice.BOND_BONDING:
                        Log.d("BT", "onReceive: BOND BONDING");
                        break;
                    case BluetoothDevice.BOND_NONE:
                        Log.d("BT", "onReceive: BOND NONE");
                        break;
                }
            }
        }
    };
}
