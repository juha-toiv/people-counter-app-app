package com.example.juha.peoplecounterapp.document;


public class Device {

    private final String id;

    private final String name;

    public Device(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
