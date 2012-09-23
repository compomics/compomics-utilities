/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.test.general;

import com.compomics.util.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import junit.framework.Assert;

/**
 * This class tests the general methods of the util class
 *
 * @author Marc
 */
public class UtilTest {
    
    /**
     * Tests the results of the integer alignment method.
     * 
     * input
     * serie1 = {0, 1, 13, 25, 15, 6, 99}
     * serie2 = {100, 2, 12, 14, 18, 30, 115, 1000}
     * 
     * shall return
     * result = {0->null, 1->2, 6->null, 13->12, 15->14, 25->30, 99->100}
     * 
     * input
     * serie1 = {5}
     * serie2 = {0, 1, 2, 3, 4, 7, 8, 9, 10}
     * 
     * shall return
     * result = {5->4}
     * 
     * input
     * serie1 = {0, 1, 2, 3, 4, 7, 8, 9, 10}
     * serie2 = {5}
     * 
     * shall return
     * result = {0->null, 1->null, 2->null, 3->null, 4->5, 7->null, 8->null, 9->null, 10->null}
     */
    public void testAlignment() {
        Integer[] array1 = {0, 1, 13, 25, 15, 6, 99};
        Integer[] array2 = {100, 2, 12, 14, 18, 30, 115, 1000};
        ArrayList<Integer> serie1 = new ArrayList<Integer>(Arrays.asList(array1));
        ArrayList<Integer> serie2 = new ArrayList<Integer>(Arrays.asList(array2));
        HashMap<Integer, Integer> result = Util.align(serie1, serie2);
        Assert.assertTrue(result.size() == serie1.size());
        Assert.assertTrue(result.containsKey(0));
        Assert.assertTrue(result.get(0) == null);
        Assert.assertTrue(result.containsKey(1));
        Assert.assertTrue(result.get(1) == 2);
        Assert.assertTrue(result.containsKey(6));
        Assert.assertTrue(result.get(6) == null);
        Assert.assertTrue(result.containsKey(13));
        Assert.assertTrue(result.get(13) == 12);
        Assert.assertTrue(result.containsKey(15));
        Assert.assertTrue(result.get(15) == 14);
        Assert.assertTrue(result.containsKey(25));
        Assert.assertTrue(result.get(25) == 30);
        Assert.assertTrue(result.containsKey(99));
        Assert.assertTrue(result.get(99) == 100);
        
        
        Integer[] array3 = {5};
        Integer[] array4 = {0, 1, 2, 3, 4, 7, 8, 9, 10};
        ArrayList<Integer> serie3 = new ArrayList<Integer>(Arrays.asList(array3));
        ArrayList<Integer> serie4 = new ArrayList<Integer>(Arrays.asList(array4));
        result = Util.align(serie3, serie4);
        Assert.assertTrue(result.size() == serie3.size());
        Assert.assertTrue(result.containsKey(5));
        Assert.assertTrue(result.get(5) == 4);
        
        result = Util.align(serie4, serie3);
        Assert.assertTrue(result.size() == serie4.size());
        Assert.assertTrue(result.containsKey(0));
        Assert.assertTrue(result.get(0) == null);
        Assert.assertTrue(result.containsKey(1));
        Assert.assertTrue(result.get(1) == null);
        Assert.assertTrue(result.containsKey(2));
        Assert.assertTrue(result.get(2) == null);
        Assert.assertTrue(result.containsKey(3));
        Assert.assertTrue(result.get(3) == null);
        Assert.assertTrue(result.containsKey(4));
        Assert.assertTrue(result.get(4) == 5);
        Assert.assertTrue(result.containsKey(7));
        Assert.assertTrue(result.get(7) == null);
        Assert.assertTrue(result.containsKey(8));
        Assert.assertTrue(result.get(8) == null);
        Assert.assertTrue(result.containsKey(9));
        Assert.assertTrue(result.get(9) == null);
        Assert.assertTrue(result.containsKey(10));
        Assert.assertTrue(result.get(10) == null);
    }
    
}
