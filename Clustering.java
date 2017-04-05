/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package csc365_hw_2;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author stephendicerce
 */
public class Clustering {

    public static final double[] centerPoints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
    private Category[] categories;
    private String name;

    private static Category readCategory(String n, String categoryName) throws IOException {
        try (
                FileInputStream in = new FileInputStream(n + "_" + categoryName);
                ObjectInputStream objectIn = new ObjectInputStream(in);) {
        
            return (Category)objectIn.readObject();
        } catch (IOException i) {
            i.printStackTrace();
            return null;
        } catch (ClassNotFoundException ex) {
            System.out.println("class not found");
            ex.printStackTrace();
            return null;
        }
    }
    
    private void writeCategory(Category category) {
        try(
                FileOutputStream out = new FileOutputStream(name + "_" + category.getName());
                ObjectOutputStream objectOut = new ObjectOutputStream(out);) {
            objectOut.writeObject(out);
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

    private static int findFittingCategory(double windSpeed) {
        int categoryPosition = 0;
        for (int i = 1, length = centerPoints.length; i < length; ++i) {
            if (Math.abs(windSpeed - centerPoints[i]) < Math.abs(windSpeed - centerPoints[categoryPosition])) {
                categoryPosition = i;
            }
        }

        return categoryPosition;
    }

    public void addKey(String key, double windSpeed) {
        int categoryPosition = findFittingCategory(windSpeed);
        categories[categoryPosition].addKey(key);
    }

    public void persistCategories() {
        for(int i = 0, length = centerPoints.length; i < length; ++i){
            writeCategory(categories[i]);
        }
    }
    
    public static Category getCategory(String n, double windSpeed) throws IOException {
        int position = findFittingCategory(windSpeed);
        String cName = getCategoryByName(position);
        return readCategory(n, cName);
    }
}
