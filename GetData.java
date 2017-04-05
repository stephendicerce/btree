/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package csc365_hw_2;

import java.io.BufferedReader;
import java.io.File;
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
import java.util.logging.Level;
import java.util.logging.Logger;
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
public class GetData implements Serializable{

    static HashMap hashMap;
    static String location;
    static String[][] locations;
    static int numberOfStations;
    static ArrayList<String> keys;
    static boolean newTree;
    
    public static void writeCache(String bTreeName) {
        try (
            FileOutputStream fout = new FileOutputStream("btree_data/" + bTreeName + "_cache.txt");
            ObjectOutputStream out = new ObjectOutputStream(fout);) {
            out.writeObject(hashMap);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public static HashMap readCache(String bTreeName) {
        try (
            FileInputStream fin = new FileInputStream("btree_data/" + bTreeName + "_cache.txt");
            ObjectInputStream in = new ObjectInputStream(fin);) {
            hashMap = (HashMap)in.readObject();
            return hashMap;
        } catch (FileNotFoundException ex) {
            System.out.println("File: btree_data/" + bTreeName + "_cache.txt not found");
            return null;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } catch (ClassNotFoundException ex) {
            System.out.println("Class HashMap not found.");
            return null;
        }
    }

    public static void pullDataFromWebsite(String[][] l, int n) throws MalformedURLException, IOException, AuthenticationException, InterruptedException, ParseException {
        numberOfStations = n;
        locations = l;
        keys = new ArrayList<>();
        //Prints how many stations are getting data pulled from
        System.out.println(locations.length);

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
                    double mapValue;
                    JSONObject jObject2 = (JSONObject) iterator.next();
                    String date = (String) jObject2.get("date"); //creating different strings to be used by JSON simple
                    double dataPoint = (double) jObject2.get("value");
                    String year = date.substring(0, 4); // separates the single date string into month and year
                    String month = date.substring(5, 7);
                    location = locations[i][0];
                    String key = location + " " + year + " " + month;
                    if(readCache("BTree") != null) {
                        hashMap = readCache("BTree");
                    } else {
                        hashMap = new HashMap();
                    }
                    if (dataPoint != (double) hashMap.getEntryValue(key)) {
                        keys.add(key);
                        hashMap.put(key, dataPoint); // places the object in the hashmap using the key created by the object
                        newTree = true;
                    }
                    System.out.println(hashMap.count); // prints the number of objects inside the hashmap for verification

                }

            }
        }
        if(newTree = true) {
            System.out.println("Updating BTree");
            BTree bTree = new BTree("BTree");
            try {
                for(int i = 0, length = keys.size(); i < length; ++i){
                bTree.insertKeyValuePair(keys.get(i), (double)hashMap.getEntryValue(keys.get(i)));
                }
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (ClassNotFoundException ex) {
                System.out.println("Class KeyValuePair not found");
            }
        }

    }

    /**
     * method to make the created hashmap available to the rest of the program
     */
    public static HashMap getHashMap() {
        return hashMap;
    }
}
