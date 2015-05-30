package com.mycompany.EntryApp;

import android.bluetooth.BluetoothDevice;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ryan on 5/30/15.
 */

public class ValidatedTicket extends Ticket{
    private Date mDate;
    private String mMACAddress;

    public ValidatedTicket(String ticketName, String venueName, String eventDate, String seatLocation,
                      String ticketID, String ticketHolder, boolean validTicket, Date time, String address){
        super(ticketName, venueName, eventDate, seatLocation, ticketID, ticketHolder, validTicket);
        mDate = time;
        mMACAddress = address;
    }

    public ValidatedTicket(Ticket ticket, Date time, String address){
        super(ticket.getTicketName(), ticket.getVenueName(), ticket.getEventDate(),
                ticket.getSeatLocation(), ticket.getTicketID(), ticket.getTicketHolder(),
                ticket.isValidTicket());
        mDate = time;
        mMACAddress = address;
    }

    public Date getValidationTime(){
        return mDate;
    }

    public String getAddress(){
        return mMACAddress;
    }

    public String toString(){
        DateFormat df = new SimpleDateFormat("HH:mm:ss MM/dd/yyyy");
        return "Ticket validated at: " + df.format(mDate) + " by device: " + mMACAddress
                + '\n' + '\n' + super.toString();
    }
}
