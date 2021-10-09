package com.joting;

public abstract class Device {
    String name = "";
    String address = "";
    public Device(){};
    public Device(String name){
        this.name = name;
    }
    public Device(String name,String address){
        this.name = name;
        this.address = address;
    }

}
