/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package csc365_hw_2.btree;

import java.io.Serializable;

/**
 *
 * @author stephendicerce
 */
public class HashMap implements Serializable {

    /**
     * entry class within the Hashmap class, this class creates all of the
     * objects that are being stored in the hashTable
     */
    static class Entry implements Serializable {

        final String key;
        Object value;
        Entry next;
        int hash;

        /**
         * The constructor for the Entry Object
         */
        Entry(String k, Object v, Entry p, int h) {
            key = k;
            value = v;
            next = p;
            hash = h;
        }
    }
    Entry[] tab;

    /**
     * Constructor for the HashTable
     */
    public HashMap() {
        tab = new Entry[16];
    }

    /**
     * This method takes a key (string) as its input, and uses that key to find
     * the value associated with that key. This is very similar to the
     * containsKey method, the major difference being that the value returned is
     * the actual value of the object not just a boolean that says if it is
     * there or not. This method searches through all of the entries in the
     * hashmap and returns the value with the matching key
     */
    public Object getEntryValue(String key) {
        int h = key.hashCode();
        int i = h & (tab.length - 1);

        for (Entry e = tab[i]; e != null; e = e.next) {
            if (e.hash == h && key.equals(e.key)) {
                return e.value;
            }
        }
        return null;
    }

    /**
     * containsKey method to find if there is an Entry that matches the key that
     * is input into the function. This method is very similar to the previous
     * method
     */
    public boolean containsKey(String key) {
        int h = key.hashCode();
        Entry[] t = tab;
        int i = h & (t.length - 1);

        for (Entry e = t[i]; e != null; e = e.next) {
            if (e.hash == h && key.equals(e.key)) {
                return true;
            }
        }
        return false;
    }
    //Variable to keep track of the amount of entries in the HashMap
    int count = 0;

    /**
     * method used to input new entries into the hashmap. This method also keeps
     * track of how many entries are in the map and will resize the map to
     * accommodate for the input of more entries
     */
    public void put(String key, Object value) {
        //System.out.println("Inside put");
        int h = key.hashCode();
        Entry[] t = tab;
        int i = h & (t.length - 1);
        for (Entry e = t[i]; e != null; e = e.next) {
            if (e.hash == h && key.equals(e.key)) {
                e.value = value;
                return;
            }
        }
        Entry p = new Entry(key, value, t[i], h);
        t[i] = p;
        int c = ++count;
        double f = t.length;
        if ((c / f) < 0.75) {
            return;
        }

        int n = t.length;
        int newN = n << 1;
        Entry[] newTab = new Entry[newN];
        for (int j = 0; j < n; ++j) {
            Entry e;
            while ((e = t[j]) != null) {
                t[j] = e.next;
                int k = e.hash & (newN - 1);
                e.next = newTab[k];
                newTab[k] = e;
            }
        }
        tab = newTab;
    }

    /**
     * Method to remove entries from the hashmap. This method is not needed for
     * the current assignment, however, to increase the functionality of the map
     * it was decided to keep this method incase this project evolves to the
     * point where it is needed
     */
    public void remove(String key) {
        int h = key.hashCode();
        Entry[] t = tab;
        int i = h & (t.length - 1);
        Entry pred = null;
        Entry p = t[i];

        while (p != null) {
            if (p.hash == h && key.equals(p.key)) {
                if (pred == null) {
                    t[i] = p.next;
                } else {
                    pred.next = p.next;
                }
                return;
            }
            pred = p;
            p = p.next;
        }
    }

}
