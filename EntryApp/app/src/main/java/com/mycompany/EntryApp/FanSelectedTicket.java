package com.mycompany.EntryApp;

import android.bluetooth.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class FanSelectedTicket extends ActionBarActivity {
    private Ticket selectedTicket;

    private BluetoothSocket mConnection = null;
    private BluetoothAdapter mBluetoothAdapter;

    private final android.content.BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                if(mConnection == null) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    TextView ticketInformation = (TextView) findViewById(R.id.pairedText);
                    ticketInformation.setText("You are paired with " + device.getAddress());
                    ConnectThread connection = new ConnectThread(device);
                    connection.run();
                    if(connection.getSocket() != null){
                        mConnection = connection.getSocket();
                        manageConnection();
                    }

                }
            }
        }
    };

    public void manageConnection(){
        rwThread thread = new rwThread(mConnection);
        thread.write(selectedTicket.getTicketID().getBytes());
        byte[] color = thread.read();
        //show thing based on color
        thread.write("ACK".getBytes());
        byte[] validated = thread.read();
        thread.write("ACK".getBytes());
        thread.cancel();
        //transition when the ticket is validated

    }

    private class rwThread extends Thread{
        private BluetoothSocket mSocket;
        private InputStream inStream;
        private OutputStream outStream;

        public byte lastRead;

        public rwThread(BluetoothSocket socket){
            mSocket = socket;

            try{
                inStream = mSocket.getInputStream();
                outStream = mSocket.getOutputStream();
            }
            catch(IOException e){
                inStream = null;
                outStream = null;
            }


        }

        public void run(){
            byte[] buffer = new byte[1024];
            while(true) {
                try {
                    lastRead = (byte) inStream.read(buffer);

                } catch (IOException e) {

                }
            }
        }

        public byte[] read(){
            byte[] buffer = new byte[1024];
            try {
                inStream.read(buffer);
                return buffer;

            } catch (IOException e) {

            }
            return null;
        }
        public void write(byte[] buffer) {
            try {
                outStream.write(buffer);
            }
            catch(IOException e) {

            }
        }

        public void cancel(){
            try{
                mSocket.close();
            }
            catch(IOException cantClose){

            }
        }
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket mSocket;
        private BluetoothDevice mDevice;

        public ConnectThread(BluetoothDevice device){
            try {
                mDevice = device;
                mSocket = mDevice.createRfcommSocketToServiceRecord(new UUID(0, 0));
            }
            catch (IOException e){

            }
        }

        public BluetoothSocket getSocket(){
            return mSocket;
        }
        public void run(){
            mBluetoothAdapter.cancelDiscovery();
            try{
                mSocket.connect();
            }
            catch(IOException cantConnect){
                try{
                    mSocket.close();
                    mSocket = null;
                    mBluetoothAdapter.startDiscovery();
                }
                catch(IOException cantClose){

                }
                return;
            }
        }

        public void cancel(){
            try{
                mSocket.close();
                mBluetoothAdapter.startDiscovery();
            }
            catch(IOException cantClose){

            }
        }
    }

    private boolean DEMO_MODE = false;
    private class changeScreen extends TimerTask{
        public void run(){
            Intent i = new Intent(getApplicationContext(), ValidatedTicket.class);
            startActivity(i);
            finish();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fan_selected_ticket);
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            selectedTicket = (Ticket) extras.getSerializable("ChosenTicket");
        }
        TextView ticketInformation = (TextView) findViewById(R.id.TicketText);
        ticketInformation.setText(selectedTicket.toString());

        showProgressBar(false);

        Switch search = (Switch) findViewById(R.id.searchSwitch);
        search.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showProgressBar(isChecked);
                if(isChecked) {
                    if(DEMO_MODE){
                        Timer timer = new Timer();
                        FrameLayout colorView = (FrameLayout) findViewById(R.id.ColorView);
                        int color = Color.parseColor("#00aacc");
                        colorView.setBackgroundColor(color);
                        timer.schedule(new changeScreen(), 6000);

                        return;
                    }
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (mBluetoothAdapter == null) {
                        //display bad things
                        System.out.println("Device has no bluetooth functionality");
                    } else {
                        if (!mBluetoothAdapter.isEnabled()) {
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBtIntent, 0);
                        }
                        Intent discoverableIntent;

                        // Register the BroadcastReceiver
                        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

                        discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                        startActivity(discoverableIntent);
                        mBluetoothAdapter.startDiscovery();



                    }
                }
                else{
                    mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    mConnection = null;
                    if (mBluetoothAdapter == null) {
                       //Nothing to turn off
                        return;
                    }
                    else if(mBluetoothAdapter.isDiscovering()){
                        mBluetoothAdapter.cancelDiscovery();
                        unregisterReceiver(mReceiver);
                    }
                }

            }
        });

    }

    private void showProgressBar(boolean shouldShow){
        ProgressBar wheel = (ProgressBar) findViewById(R.id.progressBar);
        if(shouldShow){
            wheel.setVisibility(View.VISIBLE);
        }
        else{
            wheel.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_fan_selected_ticket, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
