package com.joting;

import java.util.*;

/**
 * 驱动
 */
public class Driver {
    Set<Host> hosts = new HashSet<>();
    List<Device> route = new LinkedList<>();
    Map<Node, Set<Node>> graph = new HashMap<>();
    int netCount = 0;
    int bridgeCount = 0;
    Scanner scanner;
    ///初始化网络
    void init(){
        hosts = new HashSet<>();
        netCount = 0;
        route = new LinkedList<>();
        System.out.print("请输入网桥数量：");
        int n = scanner.nextInt();
        for (int i = 0; i < n; i++) {
            add();
        }
        printGraph();
    }
    //运行程序
    void run(){
        scanner = new Scanner(System.in);
        init();
        while (true){
            System.out.println("请输入命令：");
            System.out.println("init:初始化");
            System.out.println("add:增加网桥");
            System.out.println("link:连接已存在网段与网桥");
            System.out.println("show:打印网络结构");
            System.out.println("send:发送数据包");
            System.out.println("showtable:打印网桥的转发表");
            System.out.println("back:后退");
            System.out.println("q:退出");
            dealCommand(scanner.next());
        }
    }
    ///处理输入的命令
    void dealCommand(String cmd){
        if (cmd.equals("init")){
            init();
            return;
        }
        if (cmd.equals("add")){
            add();
            return;
        }
        if (cmd.equals("link")){
            link();
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
        if (cmd.equals("showtable")){
            showTable();
            return;
        }
        if (cmd.equals("q")){
            System.exit(0);
        }
    }
    ///打印转发表
    void showTable(){
        System.out.println("请输入需展示的网桥名（如 B1 ）");
        String name = scanner.next();
        Bridge bridge = getBridge(name);
        if (bridge == null){
            System.out.println("网桥不存在");
            return;
        }
        bridge.printTable();
    }

    ///新增网桥
    void add(){

        String name = "B" + bridgeCount;
        bridgeCount ++;
        Bridge bridge = new Bridge(name);
        graph.put(bridge,new HashSet<>());
        if(bridgeCount > 1){
            System.out.println("请输入网段号来将新网桥纳入原网络（如 1 ）:");
            int netNo = scanner.nextInt();
            Net net = getNet(netNo);
            bridgeAdd(bridge,net);
        }
        System.out.printf("请输入新网桥：%s 的网段数量： ",name);
        int netCount = scanner.nextInt();
        for (int i = 0; i < netCount; i++) {
            Net net = makeNet();
            bridgeAdd(bridge,net);
        }

    }
    ///连接网桥和网段
    void link(){
        System.out.print("请输入网段号（如 1 ）:");
        int netNo = scanner.nextInt();
        System.out.print("\n请输入网桥名（如 B1 ）:");
        String name = scanner.next();
        Bridge bridge = getBridge(name);
        Net net = getNet(netNo);
        bridgeAdd(bridge,net);
    }
    ///创建网段
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
    ///为网桥添加网段
    void bridgeAdd(Bridge bridge,Net net){
        if (bridge == null || net == null) return;
        bridge.nets.add(net);
        graph.get(bridge).add(net);
        graph.get(net).add(bridge);
    }
    void send(){
        System.out.print("请输入源主机名称（如 H1 ）:");
        String name = scanner.next();
        Host source = getHost(name);
        if (source == null){
            System.out.println("该主机不存在");
            return;
        }
        System.out.print("请输入目的主机名称（如 H2 ）:");
        name = scanner.next();
        Host dest = getHost(name);
        if (dest == null){
            System.out.println("该主机不存在");
            return;
        }
        System.out.println("请输入消息内容（如 123 ）:");
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
    ///打印网络
    void printGraph(){
        TreeNode rootNode = TreeNode.makeTree(graph);
        System.out.println("****网络结构****");
        rootNode.printTree();
        System.out.println("************");
    }
    ///发送数据
    void sendData(String message,Host source,Host destination){
        Data data = new Data(source,destination,message);
        route = new LinkedList<>();
        route.add(source);
        Net net = getNet(source.netNo);
        backward(data,net,graph,route,new LinkedList<>());
    }
    ///回溯
    boolean backward(Data data,Node node,Map<Node,Set<Node>> graph,List<Device>route,List<Node>visited){
        visited.add(node);//已访问
        if (visited.size() == graph.keySet().size()) return false;
        if (node.getClass() == Net.class){
            for (Node neibghbor: graph.get(node)) {
                if (visited.contains(node)) continue;
                Bridge bridge = (Bridge) neibghbor;
                int port1 = bridge.nets.indexOf(node);
                if (port1 != -1){
                    bridge.add(data.source,port1);
                }
            }
            if (data.destination.netNo == ((Net) node).id){
                //找到目的地
                route.add(data.destination);
                printRoute();
                return true;
            }
            //向四周广播
            for (Node neibghbor: graph.get(node)) {
                if (visited.contains(neibghbor)) continue; //避免回路
                if(backward(data,neibghbor,graph,route,visited)){
                    return true;
                }
                visited.remove(neibghbor);
            }
        }
        if (node.getClass() == Bridge.class){
            //网桥
            Bridge bridge = (Bridge) node;
            Net lastNode = (Net)visited.get(visited.size() - 2);
            int port = bridge.nets.indexOf(lastNode);
            int forwardPort = bridge.forward(data,port);
            if (forwardPort == -1){
                // 未知目的地
                // 对其他地方广播
                route.add(bridge);
                for (Node neibghbor: graph.get(node)) {
                    if (visited.contains(neibghbor)) continue; //避免回路
                    if(backward(data,neibghbor,graph,route,visited)){
                        return true;
                    }
                    visited.remove(neibghbor);
                }
            }else {
                // 找到目标端口
                if (forwardPort == port){
                    // 在同一网段
                    // 已到达目的地
                    route.add(data.destination);
                    printRoute();
                    return true;
                }else {
                    // 向目标端口转发
                    route.add(bridge);
                    Net nextnNet = bridge.nets.get(forwardPort);
                    if (!visited.contains(nextnNet)){
                        if(backward(data,nextnNet,graph,route,visited)){
                            return true;
                        }
                        visited.remove(nextnNet);
                    }
                }
            }
        }
        return false;
    }
    ///打印最近一次数据包发送路径
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
}
