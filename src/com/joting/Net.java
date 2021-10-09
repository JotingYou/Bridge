package com.joting;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Net implements Node {
    int id;
    Net(int num){
        super();
        this.id = num;
    }
    List<Host> hosts = new LinkedList<>();
    void addHost(Host host){
        hosts.add(host);
    }


}
