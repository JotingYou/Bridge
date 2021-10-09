package com.joting;

import java.util.*;

public class Driver {
    Set<Host> hosts = new HashSet<>();
    List<Device> route = new LinkedList<>();
    Map<Node, Set<Node>> graph = new HashMap<>();
    int netCount = 0;
    int bridgeCount = 0;
    Scanner scanner;
    void init(){
        hosts = new HashSet<>();
        netCount = 0;
        route = new LinkedList<>();
        System.out.print("请输入网桥数量：");
        int n = scanner.nextInt();
        for (int i = 0; i < n; i++) {
            add();
        }
    }
    void run(){
        scanner = new Scanner(System.in);
        init();
        while (true){
            System.out.println("请输入命令：");
            System.out.println("init:初始化");
            System.out.println("add:增加网桥");
            System.out.println("addNet:为网桥添加网络");
            System.out.println("show:打印拓扑结构");
            System.out.println("send:发送数据包");
            System.out.println("back:后退");
            System.out.println("q:退出");
            dealCommand(scanner.next());
        }
    }
    void dealCommand(String cmd){
        if (cmd.equals("init")){
            init();
            return;
        }
        if (cmd.equals("add")){
            add();
            return;
        }
        if (cmd.equals("addNet")){
            addNet();
            return;
        }
        if (cmd.equals("send")){
            send();
            return;
        }
        if (cmd.equals("back")){
            back();
            return;
        }
        if (cmd.equals("show")){
            printGraph();
            return;
        }
        if (cmd.equals("q")){
            System.exit(0);
        }
    }
    void add(){
        String name = "B" + bridgeCount;
        bridgeCount ++;
        Bridge bridge = new Bridge(name);
        graph.put(bridge,new HashSet<>());
        System.out.printf("请输入网桥：%s 网段数量： ",name);
        int netCount = scanner.nextInt();
        for (int i = 0; i < netCount; i++) {
            Net net = makeNet();
            bridgeAdd(bridge,net);
        }

    }
    void addNet(){
        System.out.print("请输入网络号:");
        int netNo = scanner.nextInt();
        System.out.print("\n请输入网桥名:");
        String name = scanner.next();
        Bridge bridge = getBridge(name);
        Net net = getNet(netNo);
        bridgeAdd(bridge,net);
    }
    Net makeNet(){
        Net net = new Net(netCount);
        graph.put(net,new HashSet<>());
        netCount ++;
        System.out.printf("请输入网段%d 主机数量： ",net.id);
        int hostCount = scanner.nextInt();
        for (int j = 0; j < hostCount; j++) {
            Host host = new Host("H" + hosts.size(),net.id);
            net.addHost(host);
            hosts.add(host);
        }
        return net;
    }
    void bridgeAdd(Bridge bridge,Net net){
        bridge.nets.add(net);
        graph.get(bridge).add(net);
        graph.get(net).add(bridge);
    }
    void send(){
        System.out.print("请输入源主机名称:");
        String name = scanner.next();
        Host source = getHost(name);
        if (source == null){
            System.out.println("该主机不存在");
            return;
        }
        System.out.print("请输入目的主机名称:");
        name = scanner.next();
        Host dest = getHost(name);
        if (dest == null){
            System.out.println("该主机不存在");
            return;
        }
        System.out.println("请输入消息内容:");
        String msg = scanner.next();
        sendData(msg,source,dest);
    }
    void back(){
        for (Device device: route) {
            if (device.getClass() == Bridge.class){
                Bridge bridge = (Bridge) device;
                bridge.back();
            }
        }
    }
    void printGraph(){
        List<Node> topo = getTopoSequence(graph);
        if (topo == null){
            System.out.println("拓扑不存在");
            return;
        }
        System.out.println("****网络结构****");
        for (Node node : topo) {
            if (node.getClass() == Bridge.class){
                Bridge bridge = (Bridge)node;
                System.out.printf("网桥:%s\n",bridge.name);
            }
            if (node.getClass() == Net.class){
                Net net = (Net)node;
                System.out.printf("网段%d:",net.id);
                for (Host host:net.hosts) {
                    System.out.printf("%s ",host.name);
                }
                System.out.println();
            }
        }
        System.out.println("************");
    }
    void sendData(String message,Host source,Host destination){
        Data data = new Data(source,destination,message);
        route = new LinkedList<>();
        route.add(source);
        Net net = getNet(source.netNo);
        backward(data,net,graph,route);
    }
    void backward(Data data,Net net,Map<Node,Set<Node>> graph,List<Device>route){
        for (Node node: graph.get(net)) {
            Bridge bridge = (Bridge) node;
            int port = bridge.nets.indexOf(net);
            bridge.add(data.source,port);
        }
        if (data.destination.netNo == net.id){
            //找到目的地
            route.add(data.destination);
            printRoute();
            return;
        }
        for (Node node: graph.get(net)) {
            Bridge bridge = (Bridge) node;
            int port = bridge.nets.indexOf(net);
            int forwardPort = bridge.forward(data,port,route);
            if (forwardPort == -1){
                continue;
            }else {
                Net nextnNet = bridge.nets.get(forwardPort);
                if (net != nextnNet && nextnNet.id != data.source.netNo){
                    backward(data,net,graph,route);
                }
                break;
            }
        }
    }
    void printRoute(){
        boolean isFirst = true;
        for (Device device:route) {
            if (isFirst){
                isFirst = false;
                System.out.print(device.name);
            }else {
                System.out.print("->");
                System.out.print(device.name);
            }
        }
        System.out.println();
    }
    Bridge getBridge(String name){
        for (Node node :
                graph.keySet()) {
            if (node.getClass() == Bridge.class){
                Bridge bridge = (Bridge) node;
                if (bridge.name.equals(name)){
                    return bridge;
                }
            }
        }
        return null;
    }
    Host getHost(String name){
        for (Host host :
                hosts) {
            if (host.name.equals(name)){
                return host;
            }
        }
        return null;
    }
    Net getNet(int id){
        for (Node node :
                graph.keySet()) {
            if (node.getClass() == Net.class){
                Net net = (Net) node;
                if (net.id == id){
                    return net;
                }
            }
        }
        return null;
    }
    /**
     * 获取入度
     * @param graph
     * @return
     */
    private Map<Node,Integer> getIndgrees(Map<Node,Set<Node>> graph){
        Map<Node,Integer> indgrees = new HashMap<>();
        for (Node node:graph.keySet()){
            if (!indgrees.containsKey(node)){
                indgrees.put(node,0);
            }
            for (Node neighbor:graph.get(node)){
                indgrees.put(neighbor,indgrees.getOrDefault(neighbor,0) + 1);
            }
        }
        return indgrees;
    }
    /**
     * 获取拓扑序列
     * @param graph 图
     * @return 拓扑序列
     */
    private List<Node> getTopoSequence(Map<Node,Set<Node>> graph){
        Map<Node,Integer> indgrees = getIndgrees(graph);
        List<Node> topoSeq = new LinkedList<>();
        Queue<Node> queue = new LinkedList<>();
        for (Node node:indgrees.keySet()){
            if (indgrees.get(node) == 0){
                //将入度为0的结点加入序列
                topoSeq.add(node);
                queue.offer(node);
            }
        }
        while (queue.isEmpty()){
            for (Node node:indgrees.keySet()){
                indgrees.put(node,indgrees.get(node) - 1);
                if (indgrees.get(node) == 0){
                    //将入度为0的结点加入序列
                    topoSeq.add(node);
                    queue.offer(node);
                }
            }
        }
        while (!queue.isEmpty()){
            Node node = queue.poll();
            for (Node neighbor:graph.get(node)){
                //将入度为0的结点删除，并将其下一个点入度-1
                indgrees.put(neighbor,indgrees.get(neighbor) - 1);
                if (indgrees.get(neighbor) == 0){
                    queue.offer(neighbor);
                    topoSeq.add(neighbor);
                }
            }
        }
        if (indgrees.size() != topoSeq.size()){
            //获取不到拓扑排序
            return null;
        }
        return topoSeq;
    }
}
