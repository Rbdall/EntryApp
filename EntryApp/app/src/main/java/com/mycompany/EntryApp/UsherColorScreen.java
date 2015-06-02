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
import android.graphics.drawable.Drawable;
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

    private static final int DISCOVERY_ON = 0;
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter mBluetoothAdapter;

    private Ticket[] mCurrentTickets = new Ticket[6];
    private LinkedList<ValidatedTicket> mValidatedTickets = new LinkedList<ValidatedTicket>();
    private BluetoothManager[] mBluetoothManagers = new BluetoothManager[6];

    private int[] mPossibleColors = {Color.YELLOW, Color.RED, Color.MAGENTA, Color.GREEN,
            Color.BLACK, Color.BLUE, Color.CYAN};

    public static String[] Possible_UUIDs = {"a36f2eb8-2088-408d-9506-a6789838c1ca",
            "a36f2eb8-2088-408d-9506-a6789838c1cb",
            "a36f2eb8-2088-408d-9506-a6789838c1cc",
            "a36f2eb8-2088-408d-9506-a6789838c1cd",
            "a36f2eb8-2088-408d-9506-a6789838c1ce",
            "a36f2eb8-2088-408d-9506-a6789838c1cf"};

    private void initializeManagers(){
        for(int i = 0; i < 6; i++){
            mBluetoothManagers[i] = new BluetoothManager(getApplicationContext(), mHandler,
                    false, i, java.util.UUID.fromString(Possible_UUIDs[i]));
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

    private final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case COLOR_SET:
                    Button validateButton;
                    switch(msg.arg2){
                        case 0:
                            validateButton = (Button) findViewById(R.id.UsherButton1);
                            break;
                        case 1:
                            validateButton = (Button) findViewById(R.id.UsherButton2);
                            break;
                        case 2:
                            validateButton = (Button) findViewById(R.id.UsherButton3);
                            break;
                        case 3:
                            validateButton = (Button) findViewById(R.id.UsherButton4);
                            break;
                        case 4:
                            validateButton = (Button) findViewById(R.id.UsherButton5);
                            break;
                        case 5:
                            validateButton = (Button) findViewById(R.id.UsherButton6);
                            break;
                        default:
                            Log.d("EntryApp", "BAD THINGS");
                            return;
                    }
                    validateButton.setEnabled(true);
                    validateButton.setBackgroundColor(mPossibleColors[msg.arg1]);
                    mCurrentTickets[msg.arg2] = (Ticket) msg.obj;
                    Log.d("EntryApp", "Ticket validated via Manager" + msg.arg2);

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.mycompany.EntryApp.R.layout.activity_usher_color_screen);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        initializeUsherButtons();

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
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    }
                    else {

                        //mBluetoothManager = new BluetoothManager(getApplicationContext(), mHandler, false);
                        initializeManagers();

                        if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                            Intent discoverableIntent;
                            discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600);
                            startActivityForResult(discoverableIntent, DISCOVERY_ON);
                        }
                        else {
                            startManagers();
                        }
                    }


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

    private void initializeUsherButtons(){
        final Button b = new Button(getApplicationContext());
        final Drawable d = b.getBackground();
        final Button usherButtonOne = (Button) findViewById(R.id.UsherButton1);
        usherButtonOne.setEnabled(false);
        usherButtonOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ValidatedTicket resultTicket = new ValidatedTicket(mCurrentTickets[0],
                        new Date(),
                        mBluetoothAdapter.getAddress());
                try {
                    mBluetoothManagers[0].write(BluetoothManager.objToBytes(resultTicket));
                } catch (Exception e) {

                }
                mValidatedTickets.addFirst(resultTicket);
                mCurrentTickets[0] = null;
                usherButtonOne.setBackgroundDrawable(d);
                usherButtonOne.setEnabled(false);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothManagers[0].reset();
                        mBluetoothManagers[0].start();
                    }
                }, 1000);

            }
        });
        final Button usherButtonTwo = (Button) findViewById(R.id.UsherButton2);
        usherButtonTwo.setEnabled(false);
        usherButtonTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ValidatedTicket resultTicket = new ValidatedTicket(mCurrentTickets[1],
                        new Date(),
                        mBluetoothAdapter.getAddress());
                try {
                    mBluetoothManagers[1].write(BluetoothManager.objToBytes(resultTicket));
                } catch (Exception e) {

                }
                mValidatedTickets.addFirst(resultTicket);
                mCurrentTickets[1] = null;
                usherButtonTwo.setBackgroundDrawable(d);
                usherButtonTwo.setEnabled(false);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothManagers[1].reset();
                        mBluetoothManagers[1].start();
                    }
                }, 1000);

            }
        });
        final Button usherButtonThree = (Button) findViewById(R.id.UsherButton3);
        usherButtonThree.setEnabled(false);
        usherButtonThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ValidatedTicket resultTicket = new ValidatedTicket(mCurrentTickets[2],
                        new Date(),
                        mBluetoothAdapter.getAddress());
                try {
                    mBluetoothManagers[2].write(BluetoothManager.objToBytes(resultTicket));
                } catch (Exception e) {

                }
                mValidatedTickets.addFirst(resultTicket);
                mCurrentTickets[2] = null;
                usherButtonThree.setBackgroundDrawable(d);
                usherButtonThree.setEnabled(false);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothManagers[2].reset();
                        mBluetoothManagers[2].start();
                    }
                }, 1000);

            }
        });
        final Button usherButtonFour = (Button) findViewById(R.id.UsherButton4);
        usherButtonFour.setEnabled(false);
        usherButtonFour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ValidatedTicket resultTicket = new ValidatedTicket(mCurrentTickets[3],
                        new Date(),
                        mBluetoothAdapter.getAddress());
                try {
                    mBluetoothManagers[3].write(BluetoothManager.objToBytes(resultTicket));
                } catch (Exception e) {

                }
                mValidatedTickets.addFirst(resultTicket);
                mCurrentTickets[3] = null;
                usherButtonFour.setBackgroundDrawable(d);
                usherButtonFour.setEnabled(false);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothManagers[3].reset();
                        mBluetoothManagers[3].start();
                    }
                }, 1000);

            }
        });

        final Button usherButtonFive = (Button) findViewById(R.id.UsherButton5);
        usherButtonFive.setEnabled(false);
        usherButtonFive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ValidatedTicket resultTicket = new ValidatedTicket(mCurrentTickets[4],
                        new Date(),
                        mBluetoothAdapter.getAddress());
                try {
                    mBluetoothManagers[4].write(BluetoothManager.objToBytes(resultTicket));
                } catch (Exception e) {

                }
                mValidatedTickets.addFirst(resultTicket);
                mCurrentTickets[4] = null;
                usherButtonFive.setBackgroundDrawable(d);
                usherButtonFive.setEnabled(false);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothManagers[4].reset();
                        mBluetoothManagers[4].start();
                    }
                }, 1000);

            }
        });

        final Button usherButtonSix = (Button) findViewById(R.id.UsherButton6);
        usherButtonSix.setEnabled(false);
        usherButtonSix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ValidatedTicket resultTicket = new ValidatedTicket(mCurrentTickets[5],
                        new Date(),
                        mBluetoothAdapter.getAddress());
                try {
                    mBluetoothManagers[5].write(BluetoothManager.objToBytes(resultTicket));
                }
                catch(Exception e){

                }
                mValidatedTickets.addFirst(resultTicket);
                mCurrentTickets[5] = null;
                usherButtonSix.setBackgroundDrawable(d);
                usherButtonSix.setEnabled(false);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBluetoothManagers[5].reset();
                        mBluetoothManagers[5].start();
                    }
                }, 1000);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case REQUEST_ENABLE_BT:
                //mBluetoothManager = new BluetoothManager(getApplicationContext(), mHandler, false);
                initializeManagers();

                if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    Intent discoverableIntent;
                    discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 3600);
                    startActivityForResult(discoverableIntent, DISCOVERY_ON);
                }
                else {
                    startManagers();
                }
                break;
            case DISCOVERY_ON:
                startManagers();
                break;
        }

    }
}
