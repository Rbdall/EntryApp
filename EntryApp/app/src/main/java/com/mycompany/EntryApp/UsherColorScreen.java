package com.mycompany.EntryApp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Set;
import java.util.Timer;
import java.util.UUID;

public class UsherColorScreen extends ActionBarActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBluetoothAdapter;

    private Ticket[] mCurrentTickets = new Ticket[6];
    private LinkedList<ValidatedTicket> mValidatedTickets = new LinkedList<ValidatedTicket>();
    private BluetoothManager[] mBluetoothManagers = new BluetoothManager[6];

    private void initializeManagers(){
        for(int i = 0; i < 6; i++){
            mBluetoothManagers[i] = new BluetoothManager(getApplicationContext(), mHandler, false, i);
        }
    }

    private void resetManagers(){
        for(int i = 0; i < 6; i++){
            mBluetoothManagers[i].reset();
        }
    }

    private void startManagers(){
        for(int i = 0; i < 6; i++){
            mBluetoothManagers[i].start();
        }
    }

    private void killManagers(){
        for(int i = 0; i < 6; i++){
            mBluetoothManagers[i] = null;
        }
    }

    public static final int COLOR_SET = 1;

    private int ticketToValidate = 1;
    private final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case COLOR_SET:
                    Button validateButton = (Button) findViewById(R.id.ValidationButton);
                    validateButton.setVisibility(View.VISIBLE);
                    ticketToValidate = msg.arg2;
                    mCurrentTickets[ticketToValidate] = (Ticket) msg.obj;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.mycompany.EntryApp.R.layout.activity_usher_color_screen);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        final Button validateButton = (Button) findViewById(R.id.ValidationButton);
        validateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                byte[] arr = new byte[1024];
                arr[0] = 4;

                ValidatedTicket resultTicket = new ValidatedTicket(mCurrentTickets[ticketToValidate],
                                                                    new Date(),
                                                                    mBluetoothAdapter.getAddress());
                try {
                    mBluetoothManagers[ticketToValidate].write(BluetoothManager.objToBytes(resultTicket));
                }
                catch(Exception e){

                }
                mValidatedTickets.addFirst(resultTicket);
                mCurrentTickets[ticketToValidate] = null;
                validateButton.setVisibility(View.GONE);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothManagers[ticketToValidate].reset();
                        mBluetoothManagers[ticketToValidate].start();
                    }
                }, 1000);

            }
        });

        showProgressBar(false);

        Switch search = (Switch) findViewById(R.id.ScanSwitch);
        search.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showProgressBar(isChecked);
                if (isChecked) {
                    if (mBluetoothAdapter == null) {
                        //Toast.makeText(findViewById(R.id.TicketText), "Your device does not support Bluetooth connections", Toast.LENGTH_LONG);
                        finish();
                        return;
                    }
                    if (!mBluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, 0);
                    }

                    //mBluetoothManager = new BluetoothManager(getApplicationContext(), mHandler, false);
                    initializeManagers();

                    if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                        Intent discoverableIntent;
                        discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600);
                        startActivity(discoverableIntent);
                    }
                    startManagers();


                } else {
                    resetManagers();
                    killManagers();
                    if (mBluetoothAdapter == null) {
                        //Nothing to turn off
                        return;
                    }
                    if (mBluetoothAdapter.isEnabled()) {
                        mBluetoothAdapter.disable();
                    }
                }

            }
        });


    }

    private void showProgressBar(boolean shouldShow){
        ProgressBar wheel = (ProgressBar) findViewById(R.id.progressBar2);
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
        getMenuInflater().inflate(com.mycompany.EntryApp.R.menu.menu_usher_color_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == com.mycompany.EntryApp.R.id.action_settings) {
            return true;
        }

        if (id == R.id.history_list) {
            Intent i = new Intent(getApplicationContext(), UsherValidatedTicketList.class);
            i.putExtra("TicketList", mValidatedTickets);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
