package com.joting;

/**
 * 设备基础类
 */
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
