package com.mycompany.myfirstapp;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;


public class TicketList extends ActionBarActivity {

    private Ticket[] possibleTickets = {
           new Ticket("CS117 Spring 2015 - C's Get Degrees Tour", "Boelter Dungeon", "5/19/2015", "Section 1, Row 1, Seat 10", "12345", "Ryan Dall", true),
            new Ticket("You've Probably Never Head of Them Anyway", "Local coffee shop", "12/6/2014", "Sit anywhere", "12985", "Ryan Dall", true),
            new Ticket("The Artist Formally Known as \'Gerla\'", "Staples Center", "3/3/15", "Section 4, Row 9, Seat 15", "31415", "Ryan Dall", true),
            new Ticket("The 100th Turing Awards - Noah Duncan Hosting", "Dolby Theatre", "6/17/2066", "Row 1 Seat 1", "01101", "Ryan Dall", true),
            new Ticket("The Mythical Man-Moth", "Sydney Opera House", "1/6/17", "Row 1", "62954", "Ryan Dall", true)

    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_list);
        Ticket test = new Ticket("test", "test", "test", "test", "test", "test", true);
        Ticket test2 = new Ticket("test2", "test2", "test2", "test2", "test2", "test2", true);
        ListAdapter listAdapter = new ArrayAdapter<Ticket>(this, android.R.layout.simple_list_item_1, possibleTickets);
        ListView ticketList = (ListView) findViewById(R.id.listView);
        ticketList.setAdapter(listAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ticket_list, menu);
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
