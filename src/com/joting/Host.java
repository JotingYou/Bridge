package com.joting;

/**
 * 主机类
 */
public class Host extends Device{
    ///所属网段编号
    int netNo;
    public Host(String name,int netNo) {
        super(name);
        this.netNo = netNo;
    }
}
