package com.mycompany.EntryApp;

import java.io.Serializable;

/**
 * Created by ryan on 5/19/15.
 */
public class Ticket implements Serializable{
    private String ticketName;
    private String venueName;
    private String eventDate;
    private String seatLocation;
    private String ticketID;
    private String ticketHolder;
    private boolean validTicket;


    public Ticket(String ticketName, String venueName, String eventDate, String seatLocation,
                  String ticketID, String ticketHolder, boolean validTicket){
        this.ticketName = ticketName;
        this.venueName = venueName;
        this.eventDate = eventDate;
        this.seatLocation = seatLocation;
        this.ticketID = ticketID;
        this.ticketHolder=ticketHolder;
        this.validTicket = validTicket;
    }

    public String getTicketName(){
        return ticketName;
    }

    public String getVenueName(){
        return venueName;
    }

    public String getEventDate(){
        return eventDate;
    }

    public String getSeatLocation(){
        return seatLocation;
    }

    public String getTicketID(){
        return ticketID;
    }

    public String getTicketHolder(){
        return ticketHolder;
    }

    public boolean isValidTicket(){
        return validTicket;
    }

    public String toString(){
        return ticketName + '\n' + '\n' +
                '\t' + "Venue: " + venueName + '\n' +
                '\t' + "Date: " + eventDate + '\n' +
                '\t' + seatLocation + '\n' +
                '\t' + "TicketID: " + ticketID + '\n';
    }

}
