/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package csc365_hw_2.btree;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author stephendicerce
 */
public class Category implements Serializable{
    String name;
    private ArrayList<String> keys;
    
    public Category(String n) {
        name = n;
        keys = new ArrayList<>();
    }
    
    public void addKey(String key) {
        keys.add(key);
    }
    
    public String getName() {
        return name;
    }
    /*
    * Transforms the arrayList of keys to an array and returns the array
    */
    public String[] getKeys() {
        String[] keyArray = keys.toArray(new String[keys.size()]);
        return keyArray;
    }
}
