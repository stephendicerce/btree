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

/**
 *
 * @author stephendicerce
 */
public class Clustering implements Serializable{

    public static final double[] centerPoints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    private Category[] categories;
    private String name;
    
    

    /*
    * Method to read the persisted category from disk
    * @Param n - label name
    * @Param categoryName - the name of the category to be persisted
    * returns - the loaded category
    */
    public static Category readCategory(String n, String categoryName) {
        try (
                FileInputStream in = new FileInputStream("category_data/" + n + "_" + categoryName +".txt");
                ObjectInputStream objectIn = new ObjectInputStream(in);) {
        
            return (Category)objectIn.readObject();
        } catch (FileNotFoundException ex) {
            return new Category(categoryName);
        } catch (IOException i) {
            i.printStackTrace();
            return null;
        } catch (ClassNotFoundException ex) {
            System.out.println("class not found");
            ex.printStackTrace();
            return null;
        }
    }
    
    /*
    * Method to write the category to disk
    * @Param category - the category to be persisted
    */
    public void writeCategory(Category category) {
        try(
                FileOutputStream out = new FileOutputStream("category_data/" + name + "_" + category.getName() + ".txt");
                ObjectOutputStream objectOut = new ObjectOutputStream(out);) {
            objectOut.writeObject(category);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public Clustering(String n) {
        categories = new Category[centerPoints.length];
        for (int i = 0, length = centerPoints.length; i < length; ++i) {
            categories[i] = new Category(getCategoryByName(i));
        }
        name = n;
    }

    private static String getCategoryByName(int i) {
        return Double.toString(centerPoints[i]);
    }

    /*
    * Fits a wind speed with its correct category. This is done by checking to 
    * find which center point is closest to the windspeed
    * @Param windSpeed - the windspeed that is to be added to a category
    * returns - the position of the array that the category is held in
    */
    public static int findFittingCategory(double windSpeed) {
        int categoryPosition = 0;
        for (int i = 1, length = centerPoints.length; i < length; ++i) {
            if (Math.abs(windSpeed - centerPoints[i]) < Math.abs(windSpeed - centerPoints[categoryPosition])) {
                categoryPosition = i;
            }
        }

        return categoryPosition;
    }

    /*
    * Adds a key to a category. First finds the category that the value should be added
    * to, then adds the key related to the value to an arrayList, housed in the 
    * category object. Then calls a method to write the category to disk.
    * @Param key - the key to be added to the category
    * @Param windSpeed - the value related to the key
    */
    public void addKey(String key, double windSpeed) {
        int categoryPosition = findFittingCategory(windSpeed);
        categories[categoryPosition].addKey(key);
        writeCategory(categories[categoryPosition]);
    }

    public void persistCategories() {
        for(int i = 0, length = centerPoints.length; i < length; ++i){
            writeCategory(categories[i]);
        }
    }
    /*
    * Gets the category that is related to the input windspeed
    * @Param n - label name
    * @Param windSpeed - the value whos category will be returned
    */
    public static Category getCategory(String n, double windSpeed) {
        int position = findFittingCategory(windSpeed);
        String cName = getCategoryByName(position);
        return readCategory(n, cName);
    }
}
