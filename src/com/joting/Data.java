package com.joting;

/**
 * 数据包
 */
public class Data {
    Host source;
    Host destination;
    String message = "";

    public Data(Host source,Host destination,String message){
        this.destination = destination;
        this.source = source;
        this.message = message;
    }

}
