package com.joting;

import java.util.*;

/**
 * 树
 */
public class TreeNode {
    List<TreeNode> treeNodes = new LinkedList<>();
    Node node = null;
    TreeNode preNode = null;
    TreeNode(Node node){
        this.node = node;
    }
    //创建拓扑树
    static TreeNode makeTree(Map<Node, Set<Node>> graph){
        Node rootNode = null;
        for (Node node : graph.keySet()) {
            if (node.getClass() == Bridge.class){
                //随机选取根节点
                rootNode = node;
                break;
            }
        }
        if (rootNode == null) return null;
        TreeNode root = new TreeNode(rootNode);
        Queue<TreeNode> queue = new LinkedList<>();
        List<Node> visited= new LinkedList<>();
        int level = 0;
        queue.offer(root);
        while (!queue.isEmpty()){
            TreeNode treeNode = queue.poll();
            visited.add(treeNode.node);
            for (Node neighbor : graph.get(treeNode.node)) {
                if (visited.contains(neighbor))continue;
                TreeNode newNode = new TreeNode(neighbor);
                newNode.preNode = treeNode;
                queue.offer(newNode);
                treeNode.treeNodes.add(newNode);
            }
        }
        return root;
    }
    //打印树
    void printTree(){
        TreeNode root = this;
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(root);
        while (!queue.isEmpty()){
            int level = queue.size();
            for (int i = 0; i < level; i++) {
                //遍历本层
                TreeNode treeNode = queue.poll();
                String nodeLabel = treeNode.node.name();
                if (treeNode.preNode != null){
                    nodeLabel = "(" + treeNode.preNode.node.name() + ")" + nodeLabel;
                }
                if (treeNode.node.getClass().equals(Net.class)){
                    Net net = (Net) treeNode.node;
                    nodeLabel += "( ";
                    for (Host host:net.hosts) {
                        nodeLabel += host.name;
                        nodeLabel += ' ';
                    }
                    nodeLabel += ')';
                }
                System.out.printf("%s ",nodeLabel);
                //添加下一层
                for (TreeNode child:treeNode.treeNodes) {
                    queue.offer(child);
                }
            }
            System.out.println();

        }
    }

}
