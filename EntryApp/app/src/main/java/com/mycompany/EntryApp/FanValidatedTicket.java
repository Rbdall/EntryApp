package com.mycompany.EntryApp;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;


public class FanValidatedTicket extends ActionBarActivity {

    private ValidatedTicket mTicketInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validated_ticket);
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            mTicketInfo = (ValidatedTicket) extras.getSerializable("TicketInfo");
        }
        DateFormat df = new SimpleDateFormat("HH:mm:ss MM/dd/yyyy");
        TextView ticketInformation = (TextView) findViewById(R.id.textView3);
        ticketInformation.setText("Your ticket (ID: " + mTicketInfo.getTicketID() +
                                   ") was validated at: \n\t\t" + df.format(mTicketInfo.getValidationTime())
                                    + "\n\n by device: \n\t\t" + mTicketInfo.getAddress());

        TextView firstMessage = (TextView) findViewById(R.id.textView);
        TextView secondMessage = (TextView) findViewById(R.id.textView2);

        firstMessage.setTextSize(12 * getResources().getDisplayMetrics().density);
        secondMessage.setTextSize(12 * getResources().getDisplayMetrics().density);

    }

    /*@Override
    public void onBackPressed(){
        return;
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_validated_ticket, menu);
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

        if(id == R.id.MAC){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("This is device: " + BluetoothAdapter.getDefaultAdapter().getAddress());
            AlertDialog alert = builder.create();
            alert.show();
        }

        return super.onOptionsItemSelected(item);
    }
}
