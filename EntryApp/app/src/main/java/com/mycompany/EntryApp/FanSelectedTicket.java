package com.mycompany.EntryApp;

import android.bluetooth.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


public class FanSelectedTicket extends ActionBarActivity {
    private boolean DEMO_MODE = false;

    private Ticket selectedTicket;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;

    private int[] mPossibleColors = {Color.YELLOW, Color.RED, Color.MAGENTA, Color.GREEN,
                                     Color.BLACK, Color.BLUE, Color.CYAN};

    public static final int BLUETOOTH_ON = 0;

    public static final int COLOR_SET = 1;
    public static final int TICKET_VALIDATED = 2;
    public static final int CONNECTION_FAILED = 3;
    private final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case COLOR_SET:
                    FrameLayout colorView = (FrameLayout) findViewById(R.id.ColorView);
                    Log.d("EntryApp", "Arg 1: " + msg.arg1);
                    Log.d("EntryApp", "Arg 2: " + msg.arg2);
                    colorView.setBackgroundColor(mPossibleColors[msg.arg1]);
                    TextView MACText = (TextView) findViewById(R.id.MAC);
                    MACText.setText(mBluetoothAdapter.getAddress());
                    break;
                case TICKET_VALIDATED:
                    mBluetoothManager.reset();
                    mBluetoothManager = null;
                    ValidatedTicket ticket = (ValidatedTicket) msg.obj;
                    Intent i = new Intent(getApplicationContext(), FanValidatedTicket.class);
                    i.putExtra("TicketInfo", ticket);
                    startActivity(i);
                    finish();
                    break;
                case CONNECTION_FAILED:
                    failedConnection = true;

            }
        }
    };

    public static String[] Possible_UUIDs = {"a36f2eb8-2088-408d-9506-a6789838c1ca",
                                             "a36f2eb8-2088-408d-9506-a6789838c1cb",
                                             "a36f2eb8-2088-408d-9506-a6789838c1cc",
                                             "a36f2eb8-2088-408d-9506-a6789838c1cd",
                                             "a36f2eb8-2088-408d-9506-a6789838c1ce",
                                             "a36f2eb8-2088-408d-9506-a6789838c1cf"};

    private boolean failedConnection = false;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                BluetoothSocket temp = null;
                int connectedUUID = -1;
                for (int i = 0; i < 6; i++) {
                    try {
                        temp = device.createRfcommSocketToServiceRecord(java.util.UUID.fromString(Possible_UUIDs[i]));
                    } catch (Exception e) {
                        Log.e("EntryApp", "ConnectThread create failed", e);
                    }
                    mBluetoothAdapter.cancelDiscovery();
                    try {
                        temp.connect();
                        connectedUUID = i;
                        break;
                    } catch (Exception e) {
                        try {
                            mBluetoothAdapter.startDiscovery();
                            temp.close();

                        } catch (Exception e2) {

                        }
                    }
                }
                if (connectedUUID == -1) {
                    return;
                }
                mBluetoothManager = new BluetoothManager(getApplicationContext(), mHandler,
                        true, 0, java.util.UUID.fromString(Possible_UUIDs[connectedUUID]));
                mBluetoothManager.setTicket(selectedTicket);

                mBluetoothManager.connected(temp, device);
            }
        }
    };
    private class changeScreen extends TimerTask{
        public void run(){
            Intent i = new Intent(getApplicationContext(), FanValidatedTicket.class);
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

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();



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
                    if(mBluetoothAdapter == null){
                        //Toast.makeText(findViewById(R.id.TicketText), "Your device does not support Bluetooth connections", Toast.LENGTH_LONG);
                        finish();
                        return;
                    }
                    if (!mBluetoothAdapter.isEnabled()) {
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(enableBtIntent, BLUETOOTH_ON);
                    }
                    else {
                        selectedTicket.setRedeemingDevice(mBluetoothAdapter.getAddress());
                        Intent discoverableIntent;

                        // Register the BroadcastReceiver
                        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

                        /*discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                        startActivity(discoverableIntent);*/
                        Log.d("EntryApp", "Starting Discovery");
                        mBluetoothAdapter.startDiscovery();
                    }


                }
                else{
                    mBluetoothManager.reset();
                    mBluetoothManager = null;
                    if (mBluetoothAdapter == null) {
                       //Nothing to turn off
                        return;
                    }
                    if(mBluetoothAdapter.isEnabled()){
                        mBluetoothAdapter.disable();
                    }
                    if(mBluetoothAdapter.isDiscovering()){
                        mBluetoothAdapter.cancelDiscovery();
                        //unregisterReceiver(mReceiver);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case BLUETOOTH_ON:
                selectedTicket.setRedeemingDevice(mBluetoothAdapter.getAddress());
                Intent discoverableIntent;

                // Register the BroadcastReceiver
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

                        /*discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                        startActivity(discoverableIntent);*/
                Log.d("EntryApp", "Starting Discovery");
                mBluetoothAdapter.startDiscovery();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
