package com.mateusz.uno.localmultiplayer;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.UUID;

public class BluetoothConnectionService {
    private static final String TAG = "BluetoothConnectionServ";

    static final String appName = "UNO";
    static final UUID MY_UUID = UUID.fromString("e31bb97d-9d8d-40a1-bc63-0168e67fd803");

    private final BluetoothAdapter bluetoothAdapter;
    private Context ctx;

    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;

    private BluetoothDevice device;
    private UUID deviceUUID;

    private ProgressDialog progressDialog;
    private Toast connectionFailedToast, playerJoinedToast;

    private static BluetoothConnectionService instance;
    private BluetoothSocket socket;

    private BluetoothConnectionService(Context ctx) {
        this.ctx = ctx;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        connectionFailedToast = Toast.makeText(ctx, "Could not connect to game.", Toast.LENGTH_LONG);
        playerJoinedToast = Toast.makeText(ctx, "Connection successful.", Toast.LENGTH_SHORT);

        start();
    }

    public static BluetoothConnectionService getInstance(Context ctx){
        if(instance == null){
            instance = new BluetoothConnectionService(ctx);
        }

        return instance;
    }

    public synchronized void start() {
        if (connectThread != null) {
            connectThread.cancel();
            connectThread = null;
        }
    }

    private class AcceptThread extends Thread {

        private final BluetoothServerSocket bluetoothServerSocket;

        public AcceptThread() {
            BluetoothServerSocket tmp = null;

            //Create new server socket
            try {
                tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, MY_UUID);
                Log.d(TAG, "AcceptThread: Setting up server.");
            } catch (IOException e) {
                Log.e("ACCEPT_THREAD", "SOCKET listen() failed", e);
            }

            bluetoothServerSocket = tmp;
        }

        public void run() {
            Log.d(TAG, "run: AcceptThread running.");

            BluetoothSocket socket = null;

            try {
                socket = bluetoothServerSocket.accept();

                Log.d(TAG, "run: Server accepted a connection.");
            } catch (IOException e) {
                Log.d("SERVER_SOCKET", "could not accept server socket connection");
            }

            if (socket != null) {
                connected(socket);
                Log.d("SERVER_SOCKET", "CONNECTED");
            }
        }

        public void cancel() {
            try {
                bluetoothServerSocket.close();
            } catch (IOException e) {
                Log.d("SERVER_SOCKET", "could not close server socket");
            }
        }
    }

    private class ConnectThread extends Thread {

        private BluetoothSocket bluetoothSocket;

        public ConnectThread(BluetoothDevice mDevice, UUID uuid) {
            device = mDevice;
            deviceUUID = uuid;
        }

        public void run() {
            BluetoothSocket tmp = null;

            try {
                tmp = device.createInsecureRfcommSocketToServiceRecord(deviceUUID);
            } catch (IOException e) {
                Log.d("SERVER_SOCKET", "could not create server socket");
            }

            bluetoothSocket = tmp;

            bluetoothAdapter.cancelDiscovery();

            try {
                bluetoothSocket.connect();
                Log.d(TAG, "run: ConnectThread connected.");
            } catch (IOException e) {
                try {
                    bluetoothSocket.close();
                    Log.d(TAG, "run: Closed Socket.");
                } catch (IOException ex) {
                    Log.d("SERVER_SOCKET", "could not close server socket");
                }

                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + e.getMessage());
                progressDialog.dismiss();
                connectionFailedToast.show();
                return;
            }

            connected(bluetoothSocket);
        }

        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.d("SERVER_SOCKET", "could not close server socket");
            }
        }
    }

    private class ConnectedThread extends Thread {
        final BluetoothSocket bluetoothSocket;
        private final InputStream InStream;
        private final OutputStream OutStream;

        public ConnectedThread(BluetoothSocket bluetoothSocket) {
            socket = bluetoothSocket;
            this.bluetoothSocket = bluetoothSocket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            progressDialog.dismiss();
            playerJoinedToast.show();

            Intent i = new Intent(ctx, LocalGameActivity.class);
            ctx.startActivity(i);

            try {
                tmpIn = bluetoothSocket.getInputStream();
                tmpOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            InStream = tmpIn;
            OutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int numBytes;

            while (true) {
                try {
                    numBytes = InStream.read(buffer);

                    String incomingMessage = new String(buffer, 0, numBytes);

                    Log.d(TAG, "InputStream: " + incomingMessage);

                    Intent incomingMessageIntent = new Intent("incomingMessage");

                    String[] msg = incomingMessage.split(":");

                    incomingMessageIntent.putExtra(msg[0], msg[1]);
                    LocalBroadcastManager.getInstance(ctx).sendBroadcast(incomingMessageIntent);

                } catch (IOException e) {
                    Log.d(TAG, "run: Error reading from Input Stream." + e.getMessage());
                }
            }
        }

        public void write(String key, String msg) {
            try {
                Thread.sleep(100);

                String text = key + ":" + msg;
                Log.d(TAG, "write: Writing to outputStream: " + text);

                OutStream.write(text.getBytes());

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.d("SERVER_SOCKET", "could not close server socket");
            }
        }
    }

    public void startServer() {
        Log.d(TAG, "startServer: Starting Server Socket.");
        progressDialog = ProgressDialog.show(ctx, "Hosting Game", "Please Wait for someone to join...", true);
        progressDialog.setCancelable(true);

        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                acceptThread.cancel();
                acceptThread = null;
            }
        });

        acceptThread = new AcceptThread();
        acceptThread.start();
    }

    public void connectToServer(BluetoothDevice device) {
        Log.d(TAG, "startClient: Started.");

        progressDialog = ProgressDialog.show(ctx, "Connecting to Game", "Please Wait...", true);

        connectThread = new ConnectThread(device, MY_UUID);
        connectThread.start();
    }

    private void connected(BluetoothSocket bluetoothSocket) {
        Log.d(TAG, "connected: Starting.");

        connectedThread = new ConnectedThread(bluetoothSocket);
        connectedThread.start();
    }

    public void write(String key, String msg) {
        Log.d(TAG, "write: Write Called.");
        connectedThread.write(key, msg);
    }

    public void disconnect(){
        try {
            socket.close();
        } catch (IOException e) {
            Log.d(TAG, "disconnect: DISCONNECTING FROM SOCKET");
            e.printStackTrace();
        }
    }
}
