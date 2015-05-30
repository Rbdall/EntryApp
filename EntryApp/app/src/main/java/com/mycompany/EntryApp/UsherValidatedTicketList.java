package com.mycompany.EntryApp;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.mycompany.EntryApp.R;

import java.util.ArrayList;
import java.util.LinkedList;

public class UsherValidatedTicketList extends ActionBarActivity {
    private ArrayList<ValidatedTicket> mTicketList = new ArrayList<ValidatedTicket>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usher_validated_ticket_list);
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            mTicketList = (ArrayList) extras.getSerializable("TicketList");
        }
        ListAdapter listAdapter = new ArrayAdapter<ValidatedTicket>(this, android.R.layout.simple_list_item_1, mTicketList);
        ListView ticketList = (ListView) findViewById(R.id.listView2);
        ticketList.setAdapter(listAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_usher_validated_ticket_list, menu);
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
