/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package csc365_hw_2.btree;

/**
 *
 * @author stephendicerce
 */
public class Similarity {

    BTree bTree = GetData.getBTree();
    String[] valuesString = GetData.getValues();
    double[] values = new double[valuesString.length];
    String location, month, year, key, returnLocation;
    int indexOfWantedLocation, returnIndex, numberOfStations, firstYear, lastYear;
    double sum, distance, distanceNotSquared, min;
    String[][] locations;
    double[][] summedData = new double[12][29]; //stores the average monthly windspeed for each location 
    double[] distanceArray = new double[30]; // stores the Euclidian distance for each location based on the input location
    
    public void valuesConversion() {
        for(int i = 0, length = valuesString.length; i < length; ++i) {
            values[i] = Double.parseDouble(valuesString[i]);
        }
    }

    /**
     * Method to compare values of locations. This method queries the bTree
     * for all of the data to be compared and sorts them by location, and
     * calculates the Euclidian distance using the formula given in class (Ed =
     * (((x1-y1)^2)+((x2-y2)^2)...+ ... + ((xn-yn)^2))^(1/2))
     *
     * @param l= locations array
     * @param index = the index of the wanted location
     * @param s = the number of stations
     * @param fy = the first year of data
     * @param ly = the last year of data
     * @return the name of the city that is most similar to the requested 
     */
    public String getSimilarityMetric(String l, int index, int s, String fy, String ly) {
        System.out.println("Calculating Similarity...");
        valuesConversion();
        numberOfStations = s;
        locations = weatherGUI.getLocations();
        indexOfWantedLocation = (index - 1);
        
        firstYear = Integer.parseInt(fy);
        lastYear = Integer.parseInt(ly);
        
        // averages each month for all the years by each station, and places in an array, for later computation
        for (int k = 0; k < numberOfStations; ++k) {
            location = locations[k][0];
            for (int i = 0; i < 12; ++i) {
                for (int intYear = firstYear; intYear <= lastYear; ++intYear) {
                    if (i < 9) {
                        month = "0" + (i + 1);
                    } else {
                        month = "" + (i + 1);
                    }
                    year = "" + intYear;
                    key = location + " " + year + " " + month;
                    
                    sum += values[bTree.search(key)];
                }
                summedData[i][k] = (sum / 10);
                sum = 0;
            }
            
        }
        //calculates the Euclidian distance using the formula

        for (int i = 0; i < numberOfStations; ++i) {
            for (int j = 0; j < 12; ++j) {
                if (i != indexOfWantedLocation) {
                    distance += Math.pow(((double) summedData[j][indexOfWantedLocation] - (double) summedData[j][i]), 2);
                } else {
                    distance = 100000; // sets a high number for the distance to the city we are comparing all other cities to, so this will never be set to the minimum
                }
            }
            distanceArray[i] = Math.sqrt(distance);
//            System.out.println("SQRT of Distance: " + distanceArray[i]);
            distance = 0;

        }

        //finds the smallest Euclidian distance
        min = distanceArray[0];
        returnIndex = 0;
        for (int i = 1; i < numberOfStations; ++i) {
            if (distanceArray[i] < min) {
                min = distanceArray[i];
                returnIndex = i;
            }
//            System.out.println("Min: " + min);
        }
        
        //sets the return value based on the return index in a readable string
        returnLocation = locations[returnIndex][0];
        return returnLocation;
    }
}
