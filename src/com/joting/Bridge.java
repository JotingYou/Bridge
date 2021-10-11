package com.joting;

import java.util.*;

/**
 * 网桥类
 */
public class Bridge extends Device implements Node{
    HashMap<Device, Integer> table = new HashMap<>();
    HashMap<Device,Integer> old_table = new HashMap<>();
    List<Net> nets = new LinkedList<>();
    public Bridge(String name) {
        super(name);
    }

    void add(Device device,Integer port){
        old_table = new HashMap<>(table);
        table.put(device,port);
        printTable();
    }

    /**
     * 转发数据包
     * @param data
     * @return 是否要继续转发
     */
    int forward(Data data,int sourcePort){
        add(data.source,sourcePort);
        if (!table.containsKey(data.destination)){
            System.out.printf("%s:未知目的地址，需要广播\n",name);
            return -1;
        }
        int port = table.get(data.destination);
        if (port == sourcePort){
            System.out.printf("%s:目的地址与源地址属于同一网段，目的主机已收到数据\n",name);
            return port;
        }
        System.out.printf("%s:需从端口:%d,转发数据包\n",name,port);
        return port;
    }
    void printTable(){
        System.out.printf("------%s转发表---------\n",name);
        System.out.println("主机名称   端口");
        for (Device device:
             table.keySet()) {
            System.out.printf("  %s      %d\n",device.name,table.get(device));
        }
        System.out.println();
    }
    void back(){
        table = old_table;
    }

    @Override
    public String name() {
        return name;
    }
}
