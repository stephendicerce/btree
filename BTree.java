/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package csc365_hw_2;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 _______________________________________________________________________________
| This Class "Btree" contains all methods and means to create a persistent BTree|
| capable of persisting nodes that contain key value pairs in an array as well  |
| as an array of pointers to load children nodes, in an effort to store data in |
| an effective, and efficient way.                                              |
|Contains:                                                                      |
|@Node - rootNode:              The root node of the btree                      |
|@Variable - int numberOfNodes: the number of nodes in the b tree, used to name |
|                               the nodes.                                      |
|@Variable - String name:       The name of the tree used to find the directory |
|                               containing all of the nodes in the tree         |
|_______________________________________________________________________________|
 */
public class BTree implements Serializable {

    Node rootNode;
    int numberOfNodes;
    String name;

    /*
    * Method to persist node in the btree
    * @Param node: node to be persisted
    * @Param nodeNumber: the "id" number of the node
     */
    public void writeNode(Node node, int nodeNumber) {
        try (
                FileOutputStream fout = new FileOutputStream("btree_data/node" + nodeNumber + ".txt");
                ObjectOutputStream out = new ObjectOutputStream(fout);) {

            out.writeObject(node);
            out.flush();
            out.close();
            System.out.println("Node" + nodeNumber + "successfully written.\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /*
    * Method to retrieve a persisted node from disk
    * @Param nodeNumber: the "id" number of the node
    * returns the node from persisted data
     */
    public Node readNode(int nodeNumber) {
        try (
                ObjectInputStream in = new ObjectInputStream(new FileInputStream("btree_data/node" + nodeNumber + ".txt"));) {

            Node n = (Node) in.readObject();
            in.close();
            return n;
        } catch (FileNotFoundException ex) {
            System.out.println("File: btree_data/node" + nodeNumber + ".txt not found.");
            return null;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } catch (ClassNotFoundException ex) {
            System.out.println("Class Node not found.");
            return null;
        }
    }

    /*
    ____________________________________________________________________________
   | This internal Node class defines what is stored and traversed with the B-  |
   | tree. The tree is made up from many nodes, which build bottom up, starting |
   | with one node which then splits into many nodes depending on the user's    |
   | desire. This particular tree can hold 101 Key Value Points, therefore each |
   | node can contain 102 max children.                                         |
   | Contains:                                                                  |
   |@Variable - int nodeNumber:   The node's unique identifying number, which is|
   |                              calculated by the number of previously entered|
   |                              nodes.                                        |
   |@Variable - int numberOfKeys: The number of keys contained in the Node at   |
   |                              any given time.                               |
   |@Variable - int t:            The minimum degree of the tree                |
   |@Variable - int sizeOfNode:   The max number of Key Value Pairs that can be |
   |                              in a node.                                    |
   |@Array - KeyValuePair keyValuePairs: The array containing all the key value |
   |                              points of that specific node.                 |
   |@Array - int Children:        An array containing the nodeNumber of the     |
   |                              node's children nodes                         |
   |@Variable - boolean leaf:     tells whether or not a node is a leaf node or |                              
   |                              interior node (leaf = true, interior = false) |
   |@Variable - boolean full:     tells whether or not a node is full of key    |
   |                              value points (full = true, not full = false)  |
   |____________________________________________________________________________|
     */
    private class Node implements Serializable {

        int nodeNumber;
        int numberOfKeys;
        int t = 50;
        int sizeOfNode; //101
        KeyValuePair keyValuePairs[] = new KeyValuePair[sizeOfNode];
        int Children[] = new int[sizeOfNode + 1]; //if Node is a leaf
        boolean leaf;
        boolean full;

        public Node() {
            sizeOfNode = ((2 * t) + 1); //101
            keyValuePairs = new KeyValuePair[sizeOfNode];
            leaf = false;
            full = false;
        }

        public void setNodeNumber(int n) {
            nodeNumber = n;
        }

        public int getNodeNumber() {
            return nodeNumber;
        }

        public void setT(int n) {
            t = n;
        }

        public int getT() {
            return t;
        }

        public void setNumberOfKeys(int n) {
            numberOfKeys = n;
        }

        public int getNumberOfKeys() {
            return numberOfKeys;
        }

        public void splitNumberOfKeys() {
            numberOfKeys = ((numberOfKeys - 1) / 2);
        }

        public void setLeaf() {
            leaf = true;
            Children = null;
        }

        public boolean isLeaf() {
            return leaf;
        }

        /*
        * method to return the key value pair that is associated with a specific key
        * @Param: key A string that relates the position of the pointer to the user request
        * Returns: The key Value Pair that is associated with the specified key
         */
        public KeyValuePair getKeyValuePair(String key) {
            for (int i = 0, length = keyValuePairs.length; i < length; ++i) {
                if (key.equals(keyValuePairs[i].getKey())) {
                    return keyValuePairs[i];
                }
            }
            return null;
        }

        public String getKeyValuePairKey(KeyValuePair kvp) {
            return kvp.getKey();
        }
    }

    /*
     ________________________________________________________________________________
    |This internal class defines the Key Value pairs that make up the nodes          |
    |Contains-                                                                       |
    |@Variable- String key:      The unique identifier, in which to find the pointer |
    |                            to the correct node containing the pointer to the   |
    |                            datafile                                            |
    |@Variable- String pointer: The fileName to be loaded that contains the data the |
    |                            user is looking for                                 |
    |                                                                                |
    |________________________________________________________________________________|
     */
    private static class KeyValuePair implements Comparable<String>, Serializable {

        String key;
        double value;

        public KeyValuePair(String k, double v) {
            key = k;
            value = v;
        }

        public double getPointerByKey(String p) {
            if (p.equals(key)) {
                return value;
            }
            return -1;
        }

        @Override
        public int compareTo(String o) {
            return o.compareTo(key);
        }

        public String getKey() {
            return key;
        }

    }

    /*
    * method to search the tree for a node with a specific key
    * @Param node - the node to start searching at
    * @Param key - a string value representing the key to search for
    * returns: the node where the key is located || null, if the key is not found
     */
    public Node searchTree(Node node, String key) throws IOException, FileNotFoundException, ClassNotFoundException {
        int i = 0;
        while (i < node.numberOfKeys && node.keyValuePairs[i].getKey().compareTo(key) < 0) {
            ++i;
        }
        if (i < node.numberOfKeys && node.keyValuePairs[i].getKey().equals(key)) {
            return node;
        }
        if (node.isLeaf()) {
            return null;
        } else {
            Node child = null;
            for (int j = 0, length = node.Children.length; j < length; ++j) {
                child = readNode(node.Children[i]);
            }
            if (child != null) {
                return searchTree(child, key);
            }
            return null;
        }
    }

    public BTree(String n) throws IOException {

        name = n;
        Node root = new Node();
        ++numberOfNodes;
        root.setLeaf();
        root.setNumberOfKeys(0);
        root.setT(50);
        root.setNodeNumber(numberOfNodes);
        rootNode = root;
        writeNode(root, root.getNodeNumber());
    }

    /*
    * Method to spilt the full child into 2 half full nodes. The middle value
    * gets moved to the parent node
    * @Param parent The node the midkey will get moved too
    * @Param i index
    * @Param child node to get split in two
     */
    public void splitChild(Node parent, int i, Node child) throws IOException {
        int t = child.getT();
        Node secondChild = new Node();
        ++numberOfNodes;
        secondChild.setNodeNumber(numberOfNodes);
        secondChild.leaf = child.leaf;
        secondChild.setNumberOfKeys(t - 1);

        for (int j = 0; j < t - 1; ++j) {
            secondChild.keyValuePairs[j] = child.keyValuePairs[j + t];
        }

        if (!child.isLeaf()) {
            for (int j = 0; j < t; ++j) {
                secondChild.Children[j] = child.Children[j + t];
            }
        }

        child.setNumberOfKeys(t - 1);

        for (int j = parent.getNumberOfKeys(); j > i; --j) {
            parent.Children[j + 1] = parent.Children[j];
        }

        parent.Children[i + 1] = secondChild.nodeNumber;

        for (int j = parent.getNumberOfKeys(); j > i; --j) {
            parent.keyValuePairs[j + 1] = parent.keyValuePairs[j];
        }
        parent.keyValuePairs[i] = child.keyValuePairs[t];
        parent.setNumberOfKeys(parent.getNumberOfKeys() + 1);

        writeNode(child, child.nodeNumber);
        writeNode(secondChild, secondChild.nodeNumber);
        writeNode(parent, parent.nodeNumber);
    }

    /*
    * method to insert a kvp into the btree
    * uses split child if the node is full
    * and uses insertNonfull if the node isn't full
     */
    public void insertKeyValuePair(String key, double value) throws IOException, FileNotFoundException, ClassNotFoundException {
        KeyValuePair kvp = new KeyValuePair(key, value);
        Node r = readNode(rootNode.nodeNumber);
        int t = r.getT();
        if (r.getNumberOfKeys() == ((2 * t) - 1)) {
            Node s = new Node();
            ++numberOfNodes;
            s.setNodeNumber(numberOfNodes);

            rootNode = s;
            rootNode.setNumberOfKeys(0);
            rootNode.setT(50);
            s.Children[0] = r.nodeNumber;

            splitChild(s, t, r);
            insertNonfull(s, kvp);
        } else {
            insertNonfull(r, kvp);
        }

    }

    /*
    * method used to insert a kvp into a nonfull node
    * @Param node the node the requested kvp will be inserted into
    * @Param kvp the kvp to be inserted into the correct node
     */
    public void insertNonfull(Node node, KeyValuePair kvp) throws IOException, FileNotFoundException, ClassNotFoundException {
        int i = node.getNumberOfKeys();
        int t = node.getT();

        if (node.isLeaf()) {
            while (i >= 1 && (kvp.getKey().compareTo(node.keyValuePairs[i].getKey()) < 0)) {
                node.keyValuePairs[i + 1] = node.keyValuePairs[i];
                i = i - 1;
                node.keyValuePairs[i + 1] = kvp;
                node.setNumberOfKeys(node.getNumberOfKeys() + 1);
                writeNode(node, node.nodeNumber);
            }

        } else {
            while (i >= 1 && (kvp.getKey().compareTo(node.keyValuePairs[i].getKey())) < 0) {
                i = i - 1;
            }
            i = i + 1;
            Node child = readNode(node.Children[i]); //DISK_READ node.Children[i]
            if (child.numberOfKeys == ((2 * t) - 1)) {
                splitChild(node, i, child);
                if (kvp.getKey().compareTo(child.keyValuePairs[i].getKey()) > 0) {
                    i = i + 1;
                }
            }
            insertNonfull(child, kvp);
        }
    }

    public String getTreeName() {
        return name;
    }

}
