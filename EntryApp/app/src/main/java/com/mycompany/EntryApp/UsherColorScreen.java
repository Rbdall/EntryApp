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
import java.util.Set;
import java.util.Timer;
import java.util.UUID;

public class UsherColorScreen extends ActionBarActivity {

    private UUID appUUID = java.util.UUID.fromString("a36f2eb8-2088-408d-9506-a6789838c1ce");

    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private ListView myListView;
    private ArrayAdapter<String> BTArrayAdapter;
    private Button onButton;
    private Button offButton;
    private Handler acceptHandler = new Handler();
    private BluetoothServerSocket mServerSocket;

    private BluetoothManager mBluetoothManager;

    public static final int COLOR_SET = 1;

    private final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case COLOR_SET:
                    Button validateButton = (Button) findViewById(R.id.ValidationButton);
                    validateButton.setVisibility(View.VISIBLE);
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
                mBluetoothManager.write(arr);
                validateButton.setVisibility(View.GONE);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothManager.reset();
                        mBluetoothManager.start();
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

                    mBluetoothManager = new BluetoothManager(getApplicationContext(), mHandler, false);

                    if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                        Intent discoverableIntent;
                        discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600);
                        startActivity(discoverableIntent);
                    }

                    mBluetoothManager.start();


                } else {
                    mBluetoothManager.reset();
                    mBluetoothManager = null;
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

    //Shouldn't need this for final app, only for debugging discoverability
    private Runnable updatePairedDevices = new Runnable() {
        @Override
        public void run() {
            pairedDevices = mBluetoothAdapter.getBondedDevices();
            for(BluetoothDevice device : pairedDevices)
                BTArrayAdapter.add(device.getName()+'\n'+ device.getAddress());
            Toast.makeText(getApplicationContext(), "Supposed to be showing Paired Devices.",Toast.LENGTH_LONG).show();

            if(!mBluetoothAdapter.isEnabled())
                mHandler.postDelayed(this,1000);
        }
    };




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

        return super.onOptionsItemSelected(item);
    }
}
