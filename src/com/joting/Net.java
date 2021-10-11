package com.joting;

import java.util.LinkedList;
import java.util.List;

/**
 * 自治网段类
 */
public class Net implements Node {
    int id;
    Net(int num){
        super();
        this.id = num;
    }
    ///网段包含的主机
    List<Host> hosts = new LinkedList<>();

    void addHost(Host host){
        hosts.add(host);
    }

    @Override
    public String name() {
        return "网段" + id;
    }
}
