package com.mateusz.uno.ui.localmultiplayer;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
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

    public BluetoothConnectionService(Context ctx) {
        this.ctx = ctx;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        start();
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
                connected(socket, device);
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

        public ConnectThread(BluetoothDevice mDevice, UUID uuid){
            device = mDevice;
            deviceUUID = uuid;
        }

        public void run(){
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
                Looper.prepare();
                Toast.makeText(ctx, "Could not connect to game.", Toast.LENGTH_LONG).show();
                return;
            }

            connected(bluetoothSocket, device);
        }

        public void cancel(){
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.d("SERVER_SOCKET", "could not close server socket");
            }
        }
    }

    private  class ConnectedThread extends Thread{
        private final BluetoothSocket bluetoothSocket;
        private final InputStream InStream;
        private final OutputStream OutStream;

        public ConnectedThread(BluetoothSocket bluetoothSocket){
            this.bluetoothSocket = bluetoothSocket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try{
                progressDialog.dismiss();
                Looper.prepare();
                Toast.makeText(ctx, "Connection Successful!", Toast.LENGTH_SHORT).show();
            }
            catch (Exception e){
                e.printStackTrace();
            }

            try {
                tmpIn = bluetoothSocket.getInputStream();
                tmpOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            InStream = tmpIn;
            OutStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024];
            int numBytes;

            while(true){
                try {
                    numBytes = InStream.read(buffer);

                    String incomingMessage = new String(buffer, 0, numBytes);
                    Log.d(TAG, "InputStream: " + incomingMessage);

                    Intent incomingMessageIntent = new Intent("incomingMessage");
                    incomingMessageIntent.putExtra("msg", incomingMessage);
                    LocalBroadcastManager.getInstance(ctx).sendBroadcast(incomingMessageIntent);

                } catch (IOException e) {
                    Log.d(TAG, "run: Error reading from Input Stream." + e.getMessage());
                }
            }
        }

        public void write(byte[] bytes){
            try {
                String text = new String(bytes, Charset.defaultCharset());
                Log.d(TAG, "write: Writing to outputStream: " + text);

                OutStream.write(bytes);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel(){
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.d("SERVER_SOCKET", "could not close server socket");
            }
        }
    }

    public synchronized void start(){
        if(acceptThread == null){
            acceptThread = new AcceptThread();
            acceptThread.start();
        }

        if(connectThread != null){
            connectThread.cancel();
            connectThread = null;
        }
    }

    public void startClient(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startClient: Started.");

        progressDialog = ProgressDialog.show(ctx, "Connecting to Game", "Please Wait...", true);

        connectThread = new ConnectThread(device, uuid);
        connectThread.start();
    }

    private void connected(BluetoothSocket bluetoothSocket, BluetoothDevice device) {
        Log.d(TAG, "connected: Starting.");

        connectedThread = new ConnectedThread(bluetoothSocket);
        connectedThread.start();
    }

    public void write(byte[] out){
        Log.d(TAG, "write: Write Called.");
        connectedThread.write(out);
    }
}
