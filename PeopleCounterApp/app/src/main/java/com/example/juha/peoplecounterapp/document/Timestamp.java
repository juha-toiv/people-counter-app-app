package com.example.juha.peoplecounterapp.document;

import java.util.Date;


public class Timestamp {

    private final Date timestamp;

    private final int count;

    private final String direction;

    public Timestamp(Date timestamp, int count, String direction) {
        this.timestamp = timestamp;
        this.count = count;
        this.direction = direction;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public int getCount() {
        return count;
    }

    public String getDirection() {
        return direction;
    }

}
