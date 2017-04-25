/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package csc365_hw_2.btree;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/*
 _______________________________________________________________________________
| This Class "Btree" contains all methods and means to create a persistent BTree|
| capable of persisting nodes that contain key value pairs in an array as well  |
| as an array of pointers to load children nodes, in an effort to store data in |
| an effective, and efficient way.                                              |
|Contains:                                                                      |
|@Variable - root:              The name of the root node of the btree          |
|@Variable - int t:             the order of the nodes in the btree             |
|                               the nodes.                                      |
|@Variable - int numberOfNodes: the total number of nodes in the btree          |
|@Variable - String name:       The name of the tree used to find the directory |
|                               containing all of the nodes in the tree         |
|_______________________________________________________________________________|
 */
public class BTree implements Serializable {

   final int t = 100;
   static int numberOfNodes = 0;
   String name;
   String root;
   
   public String getName() {
       return name;
   }

    /*
    * Method to persist node in the btree
    * @Param node: node to be persisted
    * @Param nodeNumber: the "id" number of the node
     */
    public void writeNode(Node node, String n) {
        try (
                FileOutputStream fout = new FileOutputStream("btree_data/" + n + ".txt");
                ObjectOutputStream out = new ObjectOutputStream(fout);) {

            out.writeObject(node);
            out.flush();
            out.close();
            System.out.println("Node " + n + " successfully written.\n");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /*
    * Method to retrieve a persisted node from disk
    * @Param nodeNumber: the "id" number of the node
    * returns the node from persisted data
     */
    public Node readNode(String n) {
        try (
                ObjectInputStream in = new ObjectInputStream(new FileInputStream("btree_data/" + n + ".txt"));) {

            Node node = (Node) in.readObject();
            in.close();
            return node;
        } catch (FileNotFoundException ex) {
            System.out.println("File: btree_data/node" + n + ".txt not found.");
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
   |@Variable - String name:   The node's unique identifying name, which is     |
   |                              calculated by the number of previously entered|
   |                              nodes.                                        |
   |@Variable - int numberOfKeys: The number of keys contained in the Node at   |
   |                              any given time.                               |
   |@Array - KeyValuePair keyValuePairs: The array containing all the key value |
   |                              points of that specific node.                 |
   |@Array - String Children:     An array containing the nodeNumber of the     |
   |                              node's children nodes                         |
   |@Variable - boolean leaf:     tells whether or not a node is a leaf node or |                              
   |                              interior node (leaf = true, interior = false) |
   
   |____________________________________________________________________________|
     */
    private class Node implements Serializable {
        String name;
        int numberOfKeys;
        KeyValuePair[] keyValuePairs;
        boolean leaf;
        String[] Children;
        
        public Node() {
            name = "Node" + (++numberOfNodes);
            keyValuePairs = new KeyValuePair[2*t - 1];
            numberOfKeys = 0;
            leaf = true;
            Children = new String[2*t];
        }
        
    }

    /*
     ________________________________________________________________________________
    |This internal class defines the Key Value pairs that make up the nodes          |
    |Contains-                                                                       |
    |@Variable- String key:      The unique identifier, in which to find the pointer |
    |                            to the correct node containing the pointer to the   |
    |                            datafile                                            |
    |@Variable- String value: The placeholder to be loaded that contains the data the|
    |                            user is looking for                                 |
    |                                                                                |
    |________________________________________________________________________________|
     */
    private static class KeyValuePair implements Comparable, Serializable {

        String key;
        int value;

        public KeyValuePair(String k, int v) {
            key = k;
            value = v;
        }

        @Override
        public int compareTo(Object o) {
            if(!(o instanceof KeyValuePair))
                return 0;
            KeyValuePair k = (KeyValuePair)o;
            return this.key.compareTo(k.key);
        }


    }

    /*
    * method to search the tree for a node with a specific key
    * @Param node - the node to start searching at
    * @Param key - a string value representing the key to search for
    * @Param min - the minimum index to search 
    * @Param max - the maximum index to search
    * returns: the node where the key is located || null, if the key is not found
     */
    public KeyValuePair searchTree(Node node, String key, int min, int max) {
       int i = (max + min) / 2;
       
       int compared = key.compareTo(node.keyValuePairs[i].key);
       // checks to see if the key value pair is in the current
       if(compared == 0) {
           return node.keyValuePairs[i];
       } else if (compared < 0 && min != i){
           return searchTree(node, key, min, i - 1);
       } else if(compared > 0 && max != i) {
           return searchTree(node, key, i + 1, max);
       }
       if(node.leaf) // if key value pair isn't in the current node and it is a leaf, return null
           return null;
       
       // finds the index of the child node to load
       int childI = (compared < 0) ? i : i + 1;
       Node child = readNode(node.Children[childI]);
       return searchTree(child, key, 0, child.numberOfKeys - 1);//recursively search for the key in the child node
    }
    
    public int search(String key) {
       Node rootNode = readNode(root);
       KeyValuePair kvp = searchTree(rootNode, key, 0, rootNode.numberOfKeys - 1);
       return (key != null) ? kvp.value : -1;
    }

    public BTree(String n) {

        name = n;
        Node node = new Node();
        root = node.name;
        writeNode(node, root);
    }

    /*
    * Method to spilt the full child into 2 half full nodes. The middle value
    * gets moved to the parent node
    * @Param parent The node the midkey will get moved too
    * @Param i index
    * @Param child node to get split in two
     */
    public void splitChild(Node parent, int i, Node child) {
        Node secondChild = new Node();
        secondChild.leaf = child.leaf;
        secondChild.numberOfKeys = t-1;

        //exchanges values of the new node and the node to be split
        for(int j = 0, length = t - 1; j < length; ++j){
            secondChild.keyValuePairs[j] = child.keyValuePairs[j + t];
            child.keyValuePairs[j + t] = null;
        }
        if(!child.leaf){
            //exchanges the pointers to the children nodes
            for(int j =0, length = t; j < length; ++j) {
                secondChild.Children[j] = child.Children[j + t];
                child.Children[j + t] = null;
            }
        }
        
        child.numberOfKeys = t-1;
        
        for(int j = parent.numberOfKeys, minimum = i; j > minimum; --j)
            parent.Children[j+1] = parent.Children[j];
        parent.Children[i+1] = secondChild.name;
        
        for(int j = parent.numberOfKeys-1, minimum = i-1; j > minimum; --j)
            parent.keyValuePairs[j+1] = parent.keyValuePairs[j];
        parent.keyValuePairs[i] = child.keyValuePairs[t-1];
        child.keyValuePairs[t-1] = null;
        
        ++parent.numberOfKeys;
        
        writeNode(child, child.name);
        writeNode(secondChild, secondChild.name);
        writeNode(parent, parent.name);
    }

    /*
    * method to insert a kvp into the btree
    * uses split child if the node is full
    * and uses insertNonfull if the node isn't full
     */
    public void insertKeyValuePair(String key, int value) {
        KeyValuePair kvp = new KeyValuePair(key, value);
        Node r = readNode(root);
        if (r.numberOfKeys == ((2 * t) - 1)) {
            Node s = new Node();
            root = s.name;
            s.leaf = false;
            s.Children[0] = r.name;
            splitChild(s, 0, r);
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
    public void insertNonfull(Node node, KeyValuePair kvp) {
        int i = node.numberOfKeys - 1;

        if (node.leaf) {
         while(i >= 0 && kvp.compareTo(node.keyValuePairs[i]) < 0)
             node.keyValuePairs[i+1] = node.keyValuePairs[i--];
         node.keyValuePairs[i+1] = kvp;
         ++node.numberOfKeys;
         writeNode(node, node.name);
        } else {
            while(i >= 0 && kvp.compareTo(node.keyValuePairs[i]) < 0)
                --i;
            ++i;
            Node child = readNode(node.Children[i]);
            if(child.numberOfKeys == ((2*t) - 1)) {
                splitChild(node, i, child);
                if(kvp.compareTo(node.keyValuePairs[i]) > 0)
                    ++i;
            }
            if(node.Children[i] != child.name)
                child = readNode(node.Children[i]);
            insertNonfull(child, kvp);
        }
    }

    public String getTreeName() {
        return name;
    }

}
