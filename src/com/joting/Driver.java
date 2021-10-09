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
            System.out.println("addnet:为网桥添加网络");
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
        if (cmd.equals("addnet")){
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
        if (bridge == null || net == null) return;
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
//        List<Node> topo = getTopoSequence(graph);
//        if (topo == null){
//            System.out.println("拓扑不存在");
//            return;
//        }
//        System.out.println("****网络结构****");
//        for (Node node : topo) {
//            if (node.getClass() == Bridge.class){
//                Bridge bridge = (Bridge)node;
//                System.out.printf("网桥:%s\n",bridge.name);
//            }
//            if (node.getClass() == Net.class){
//                Net net = (Net)node;
//                System.out.printf("网段%d:",net.id);
//                for (Host host:net.hosts) {
//                    System.out.printf("%s ",host.name);
//                }
//                System.out.println();
//            }
//        }
        TreeNode rootNode = TreeNode.makeTree(graph);
        System.out.println("****网络结构****");
        rootNode.printTree();
        System.out.println("************");
    }
    void sendData(String message,Host source,Host destination){
        Data data = new Data(source,destination,message);
        route = new LinkedList<>();
        route.add(source);
        Net net = getNet(source.netNo);
        backward(data,net,graph,route,new LinkedList<>());
    }
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
