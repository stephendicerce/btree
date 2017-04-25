/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package csc365_hw_2.btree;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import javax.security.sasl.AuthenticationException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Method to pull the average wind speed from 30 different stations given by
 * NOAA. This method creates an array list of station ids to update the URL to
 * provide this program with new data. It then receives a large JSON output from
 * the URL which it then parses, using JSON simple, into usable objects and
 * stores them in a hashmap.
 */
public class GetData implements Serializable {

    static HashMap hashMap;
    static String location;
    static String[][] locations;
    static int numberOfStations;
    static ArrayList<String> keys;
    static boolean newTree = true;
    static BTree bTree;
    static String[] values = new String[3480];
    static int index = 0;

    /*
    * Finds the location of the value that is related to its corresponding key
    * @Param key - the key whos value is being searched for
    */
    public static int getValuePositionByKeyNumber(String key) {
        for (int i = 0, length = keys.size(); i < length; ++i) {
            if (keys.get(i).equals(key)) {
                return i;
            }

        }
        return -1;
    }

    /*
    * adds the incoming values to an array to be stored on disk for later use
    * @Param d - the double to be added
    * returns the index that the value was added at
    */
    public static int addValues(double d) {
        values[index] = Double.toString(d);
        ++index;

        return index - 1;
    }
    
    /*
    * writes the array containing all the keys to disk
    * @Param bTreeName - The name of the btree being used
    */
    public static void writeKeys(String bTreeName) {
        try(
                FileOutputStream fout = new FileOutputStream("btree_data/" + bTreeName + "_keys.txt");
                ObjectOutputStream out = new ObjectOutputStream(fout); ) {
            out.writeObject(keys);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /*
    * reads the array of keys for the btree being used
    * @Param - bTreeName - The name of the btree being used
    */
    public static boolean readKeys(String bTreeName) {
        try (
                FileInputStream fin = new FileInputStream("btree_data/" + bTreeName + "_keys.txt");
                ObjectInputStream in = new ObjectInputStream(fin); ) {
            keys = (ArrayList<String>)in.readObject();
            return true;
        } catch (FileNotFoundException ex) {
            keys = new ArrayList<>();
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        } catch (ClassNotFoundException ex) {
            System.out.println("Class \"ArrayList<String>\" not found.");
            return false;
        }
    }

    /*
    * Writes the bTree to disk
    * @Param tree - the btree to be written to disk
    */
    public static void writeTree(BTree tree) {
        try (
                FileOutputStream fout = new FileOutputStream("btree_data/" + tree.name + ".txt");
                ObjectOutputStream out = new ObjectOutputStream(fout);) {
            out.writeObject(tree);
            System.out.println("Tree successfully written");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /*
    * reads the btree from disk
    * @Param bTreeName - the name of the tree to read from disk 
    */
    public static BTree readTree(String bTreeName) {
        try (
                FileInputStream fin = new FileInputStream("btree_data/" + bTreeName + ".txt");
                ObjectInputStream in = new ObjectInputStream(fin);) {
            BTree tree = (BTree) in.readObject();
            return tree;
        } catch (FileNotFoundException ex) {
            System.out.println("File: \"btree_data/" + bTreeName + ".txt\" not found.");
            return null;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } catch (ClassNotFoundException ex) {
            System.out.println("Class: BTree not found.");
            return null;
        }
    }

    
    public static void writeValues(String bTreeName) {
        try (
                FileOutputStream fout = new FileOutputStream("btree_data/" + bTreeName + "_values.txt");
                ObjectOutputStream out = new ObjectOutputStream(fout);) {
            out.writeObject(values);
            System.out.println("Values successfully written.");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static boolean readValues(String bTreeName) {
        try (
                FileInputStream fin = new FileInputStream("btree_data/" + bTreeName + "_values.txt");
                ObjectInputStream in = new ObjectInputStream(fin);) {
            values = (String[]) in.readObject();
            return true;
        } catch (FileNotFoundException ex) {
            values = new String[3480];
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        } catch (ClassNotFoundException ex) {
            System.out.println("Class: double[] not found");
            return false;
        }
    }

    /**
     * Method to persist the current hashmap as a cache to check if the btree is
     * up to date
     *
     * @param bTreeName - A string representing the name of the btree used to
     * find the correct file to
     */
    public static void writeCache(String bTreeName) {
        try (
                FileOutputStream fout = new FileOutputStream("btree_data/" + bTreeName + "_cache.txt");
                ObjectOutputStream out = new ObjectOutputStream(fout);) {
            out.writeObject(hashMap);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static boolean readCache(String bTreeName) {
        try (
                FileInputStream fin = new FileInputStream("btree_data/" + bTreeName + "_cache.txt");
                ObjectInputStream in = new ObjectInputStream(fin);) {
            hashMap = (HashMap) in.readObject();
            return true;
        } catch (FileNotFoundException ex) {
            hashMap = new HashMap();
            return false;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        } catch (ClassNotFoundException ex) {
            System.out.println("Class HashMap not found.");
            return false;
        }
    }

    public static void pullDataFromWebsite(String[][] l, int n) throws MalformedURLException, IOException, AuthenticationException, InterruptedException, ParseException {
        numberOfStations = n;
        locations = l;
        keys = new ArrayList<>();
        //only computes if the btree is already saved to disk
        if ( readCache("BTree")) {
            readValues("BTree");
            readKeys("BTree");
            newTree = false;
        }
        //pulls data from NOAA website
        for (int i = 0; i < numberOfStations; ++i) {
            if ((i + 1) % 5 == 0) {
                Thread.sleep(1000);
            }

            URL url = new URL("https://www.ncdc.noaa.gov/cdo-web/api/v2/data?datasetid=GSOM&datatypeid=AWND&stationid=" + locations[i][1] + "&units=standard&startdate=2006-01-01&enddate=2015-12-31&limit=120");

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "text/plain");
            connection.setRequestProperty("token", "MGlrmVMdnmEUMBkpdeXAZEyaKMPugdeX");
            connection.connect();

            BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String dataString;
            while ((dataString = br.readLine()) != null) {
                JSONParser jParser = new JSONParser();
                Object object = jParser.parse(dataString);
                JSONObject jObject = (JSONObject) object;
                JSONArray jArray = (JSONArray) jObject.get("results");
                Iterator iterator = jArray.iterator();

                while (iterator.hasNext()) {
                    double doubleValue = 0;
                    JSONObject jObject2 = (JSONObject) iterator.next();
                    String date = (String) jObject2.get("date"); //creating different strings to be used by JSON simple
                    double dataPoint = (double) jObject2.get("value");
                    String year = date.substring(0, 4); // separates the single date string into month and year
                    String month = date.substring(5, 7);
                    location = locations[i][0];
                    String key = location + " " + year + " " + month;
                    
                    Double value = null;
                    if (hashMap.getEntryValue(key) != null) {
                        if (hashMap.getEntryValue(key) instanceof Number) {
                            value = ((Number) hashMap.getEntryValue(key)).doubleValue();
                            doubleValue = value;
                        }
                    }
                    if (!hashMap.containsKey(key) || value == null || doubleValue != dataPoint) {
                        keys.add(key);
                        addValues(dataPoint);
                        hashMap.put(key, dataPoint); // places the object in the hashmap using the key created by the object
                        newTree = true;
                    }

                }

            }
        }
        System.out.println(hashMap.count); // prints the number of objects inside the hashmap for verification
        //only computes if the tree needs to be updated
        if (newTree == true) {

            writeCache("BTree");
            writeValues("BTree");
            writeKeys("BTree");
            System.out.println("Values successfully written");
            System.out.println("Cache successfully written");
            System.out.println("Updating BTree");
            bTree = new BTree("BTree");
            for (int i = 0, length = keys.size(); i < length; ++i) {
                Double val = null;
                if (hashMap.getEntryValue(keys.get(i)) instanceof Number) {
                    val = ((Number) hashMap.getEntryValue(keys.get(i))).doubleValue();
                }
                bTree.insertKeyValuePair(keys.get(i), getValuePositionByKeyNumber(keys.get(i)));
            }
            writeTree(bTree);
        } else {
            System.out.println("Tree successfully loaded");
            bTree = readTree("BTree");
        }
        System.out.println("Done.");

    }

    /**
     * method to make the created hashmap available to the rest of the program
     */
    public static HashMap getHashMap() {
        return hashMap;
    }

    public static BTree getBTree() {
        return bTree;
    }

    public static String[] getKeys() {
        return keys.toArray(new String[keys.size()]);
    }

    public static String[] getValues() {
        return values;
    }
    
    public static String[] getKeysInCategory(String key) {
        Category c = Clustering.getCategory(bTree.getName(), bTree.search(key));
        if(c == null)
            return new String[0];
        return c.getKeys();
    }
    
}
