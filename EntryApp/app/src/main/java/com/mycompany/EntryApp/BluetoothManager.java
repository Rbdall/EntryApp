package com.mycompany.EntryApp;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.FrameLayout;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;

/**
 * Created by ryan on 5/29/15.
 */
public class BluetoothManager {
    private boolean DEBUG_MODE = true;

    public static final int REMOVE_COLOR = 0;
    public final Handler colorHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case REMOVE_COLOR:
                    Integer thing = new Integer(msg.arg1);
                    mColorsInUse.remove(thing);
            }
        }
    };

    //boolean which defines behavior of connected manager
    private boolean mFanManager;

    //UUID for the application
    //private final UUID appUUID = java.util.UUID.fromString("a36f2eb8-2088-408d-9506-a6789838c1ce");

    //Application name for debugging
    private final String appName = "EntryApp";

    //Data members
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mManagerID;
    private Ticket mTicket;
    private UUID mUUID;

    private ArrayList<Integer> mColorsInUse = new ArrayList<Integer>();

    //Constants to express current state
    private int mState;
    public static final int STATE_START = 0;
    public static final int STATE_ACCEPT = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    //Constructor, initializes state and adapter
    public BluetoothManager(Context context, Handler handler, boolean fanMode, int id, UUID uuid){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_START;
        mHandler = handler;
        mFanManager = fanMode;
        mManagerID = id;
        mTicket = null;
        mUUID = uuid;
    }

    public void setTicket(Ticket ticket){
        mTicket = ticket;
    }

    public void clearTicket(){
        mTicket = null;
    }

    public void write(byte[] buffer){
        if(mConnectedThread != null){
            mConnectedThread.write(buffer);
        }
    }

    private synchronized void setState(int state){
        if(DEBUG_MODE){
            Log.d(appName, "setting state: " + mState + " to " + state);
        }
        if(state < 0 || state > 3){ //invalid state
            return;
        }
        else{
            mState = state;
        }

    }

    public synchronized int getState(){
        return mState;
    }

    public synchronized void start(){
        if(DEBUG_MODE){
            Log.d(appName, "Starting service");
        }

        if(mAcceptThread != null){
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        if(mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if(mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mAcceptThread = new AcceptThread();
        mAcceptThread.start();
        setState(STATE_ACCEPT);
    }

    public synchronized void connect(BluetoothDevice device){
        if(DEBUG_MODE){
            Log.d(appName, "Starting connect");
        }

        if(mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if(mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device){
        if(DEBUG_MODE){
            Log.d(appName, "Starting connected");
        }

        if(mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if(mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        setState(STATE_CONNECTED);
    }

    public synchronized void reset(){
        if(mAcceptThread != null){
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        if(mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if(mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        setState(STATE_START);
    }

    private void serverConnectionFailed(){
        setState(STATE_ACCEPT);
    }
    private void clientConnectionFailed(){
        setState(STATE_START);
    }

    public class AcceptThread extends Thread{
        private BluetoothServerSocket mmSeverSocket;

        public AcceptThread(){
            BluetoothServerSocket temp = null;
            try{
                temp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(appName, mUUID);
            }
            catch(Exception e){
                Log.e(appName, "Accept thread listen failed", e);
            }
            mmSeverSocket = temp;
        }

        public void run(){
            if(DEBUG_MODE){
                Log.d(appName, "Starting accept thread");
            }
            setName("AcceptThread");
            BluetoothSocket socket = null;

            while(mState != STATE_CONNECTED){
                try{
                    Log.d(appName, "Manager" + mManagerID + " waiting to accept");
                    socket = mmSeverSocket.accept();
                }
                catch (Exception e){
                    Log.e(appName, "Failed to accept connection", e);
                    break;
                }

                if(socket != null){
                    synchronized (BluetoothManager.this){
                        Log.d(appName, "Manager" + mManagerID + " In sync");
                        switch(mState){
                            case STATE_ACCEPT:
                            case STATE_CONNECTING:
                                try{
                                    mmSeverSocket.close();
                                }
                                catch (Exception e){
                                    Log.e(appName, "Could not close socket while accepting", e);
                                }
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_START:
                            case STATE_CONNECTED:
                                try{
                                    socket.close();
                                }
                                catch (Exception e){
                                    Log.e(appName, "Could not close socket while accepting", e);
                                }
                                break;
                        }
                    }
                }
            }
        }

        public void cancel(){
            if(DEBUG_MODE){
                Log.d(appName, "Cancelling accept thread");
            }
            try{
                mmSeverSocket.close();
            }
            catch (Exception e){
                Log.e(appName, "Failed to close socket on accept cancel");
            }
        }
    }

    public class ConnectThread extends Thread{
        private BluetoothDevice mmDevice;
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device){
            mmDevice = device;
            BluetoothSocket temp = null;

            try{
                temp = device.createRfcommSocketToServiceRecord(mUUID);
            }
            catch (Exception e){
                Log.e(appName, "ConnectThread create failed", e);
            }
            mmSocket = temp;
        }

        public void run(){
            if(DEBUG_MODE){
                Log.d(appName, "Beginning connect thread");
            }
            setName("ConnectThread");

            mBluetoothAdapter.cancelDiscovery();

            try{
                mmSocket.connect();
            }
            catch(Exception e){
                Log.d(appName, "Connect thread could not connect");
                try{
                    mHandler.obtainMessage(FanSelectedTicket.CONNECTION_FAILED).sendToTarget();
                    mBluetoothAdapter.startDiscovery();
                    mmSocket.close();
                }
                catch(Exception e2){
                    Log.e(appName, "Failed to close socket", e2);
                }
                //mConnectThread.start();
                return;
            }
            synchronized (BluetoothManager.this){
                mConnectThread = null;
            }

            connected(mmSocket, mmDevice);

        }

        public void cancel(){
            try{
                mmSocket.close();
            }
            catch (Exception e){
                Log.e(appName, "ConnectThread socket close failed");
            }
        }


    }

    public class ConnectedThread extends Thread{
        private BluetoothSocket mmSocket;
        private InputStream mmInStream;
        private OutputStream mmOutStream;

        public  ConnectedThread(BluetoothSocket socket){
            if(DEBUG_MODE){
                Log.d(appName, "Creating connected thread");
            }
            mmSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try{
                tempIn = socket.getInputStream();
                tempOut = socket.getOutputStream();
            }
            catch(Exception e){
                Log.e(appName, "Failed to create streams in connected thread", e);
            }

            mmInStream = tempIn;
            mmOutStream = tempOut;
        }

        public void run(){
            if(DEBUG_MODE){
                Log.d(appName, "Beginning connected thread");
            }
            byte[] buffer = new byte[1024];
            int bytes;
            if(mFanManager){
                try{
                    Log.d(appName, "Fan writing ticket: " + mTicket.getTicketID());
                    mmOutStream.write(objToBytes(mTicket));
                    bytes = mmInStream.read(buffer);
                    Log.d(appName, "Fan recieved color " + buffer[0]);
                    mHandler.obtainMessage(FanSelectedTicket.COLOR_SET, buffer[0], -1, null).sendToTarget();
                    Log.d(appName, "Fan ack");
                    buffer = new byte[1024];

                    mmOutStream.write("ACK".getBytes());

                    ValidatedTicket ticketResponse;
                    while(true){
                        mmInStream.read(buffer);
                        try {
                            ticketResponse = (ValidatedTicket) bytesToObj(buffer);
                            if(ticketResponse.isValidTicket()){
                                break;
                            }
                            buffer = new byte[1024];
                        }
                        catch(Exception e){

                        }

                    }

                    Log.d(appName, "Fan recieved validation " + new String(buffer, "UTF-8"));
                    mmOutStream.write("ACK".getBytes());
                    mHandler.obtainMessage(FanSelectedTicket.TICKET_VALIDATED, ticketResponse).sendToTarget();

                }
                catch(Exception e){
                    Log.e(appName, "Can't read in connected thread");
                    clientConnectionFailed();
                }
            }
            else{
                try{
                    Log.d(appName, "Usher reading ticket data");
                    mmInStream.read(buffer);
                    Ticket ticket = (Ticket) bytesToObj(buffer);

                    Log.d(appName, "Usher recieved ticket: " + ticket.getTicketID());
                    buffer = new byte[1024];

                    //This is where we would do computation to ensure the ticket is valid

                    Log.d(appName, "Usher writing color");
                    Integer color;
                    do {
                        color = (int) (Math.random() * 6);
                    }while(mColorsInUse.contains(color));
                    mColorsInUse.add(color);
                    buffer[0] = (byte) color.intValue();
                    mmOutStream.write(buffer);
                    buffer = new byte[1024];

                    bytes = mmInStream.read(buffer);

                    Log.d(appName, "Usher recieved ack " + new String(buffer, "UTF-8"));
                    mHandler.obtainMessage(UsherColorScreen.COLOR_SET, color, mManagerID, ticket).sendToTarget();
                    buffer = new byte[1024];
                    mmInStream.read();
                }
                catch(Exception e){
                    Log.e(appName, "Can't read in connected thread");
                    serverConnectionFailed();
                }
            }

        }

        public void write(byte[] buffer){
            try{
                mmOutStream.write(buffer);
                //DO SOMETHING
            }
            catch(Exception e){
                Log.e(appName, "Can't write in connected thread");
            }
        }

        public void cancel(){
            try{
                mmSocket.close();
            }
            catch (Exception e){
                Log.e(appName, "Failed to close socket in connected thread");
            }
        }
    }

    public static byte[] objToBytes(Object obj) throws IOException{
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    public static Object bytesToObj(byte[] data) throws IOException, ClassNotFoundException{
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

}


