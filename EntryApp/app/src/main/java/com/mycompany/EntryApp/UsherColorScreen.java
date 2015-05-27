package com.mycompany.EntryApp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Set;

public class UsherColorScreen extends ActionBarActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;
    private ListView myListView;
    private ArrayAdapter<String> BTArrayAdapter;
    private Button onButton;
    private Button offButton;
    private Handler mHandler = new Handler();

    private final BroadcastReceiver bReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // add the name and the MAC address of the object to the arrayAdapter
                BTArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                //BTArrayAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.mycompany.myfirstapp.R.layout.activity_usher_color_screen);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),"This device does not support Bluetooth.", Toast.LENGTH_LONG).show();
        } else {
            onButton = (Button)findViewById(com.mycompany.myfirstapp.R.id.onButton);
            onButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            try {
                                                on(v);
                                            } catch (IOException e) {
                                                //
                                            }
                                        }
                                     });
            offButton = (Button)findViewById(com.mycompany.myfirstapp.R.id.offButton);
            offButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    off(v);
                }
            });


            //Set the BTArrayAdapter to the listview for debugging purposes
            myListView = (ListView)findViewById(com.mycompany.myfirstapp.R.id.listView1);
            BTArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
            myListView.setAdapter(BTArrayAdapter);


        }


    }

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

    //Turn Bluetooth On if not already
    public void on(View view) throws IOException {
        if (!mBluetoothAdapter.isEnabled()){
            Intent turnOnIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOnIntent, 0);

            //Open ServerSocket
//            BluetoothServerSocket tmp = null;
//            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord();
//            BluetoothServerSocket mServerSocket = tmp;
//            BluetoothSocket socket = mServerSocket.accept();
//            if(socket != null)
//                manageConnectedSocket(socket);

            //Start discovery
            BTArrayAdapter.clear();

            registerReceiver(bReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);

            mBluetoothAdapter.startDiscovery();

            //Start Handler to update grid
            mHandler.removeCallbacks(updatePairedDevices);
            mHandler.postDelayed(updatePairedDevices, 1000);
        }
        else{
            Toast.makeText(getApplicationContext(),"Bluetooth is already on",Toast.LENGTH_LONG).show();
        }
    }
    //Turn Bluetooth Off if not already
    public void off(View view){
        if (mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.cancelDiscovery();
            mBluetoothAdapter.disable();
        }
    }

    public void manageConnectedSocket(BluetoothSocket socket){
        //do some stuff with the connected device info

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.mycompany.myfirstapp.R.menu.menu_usher_color_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == com.mycompany.myfirstapp.R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
