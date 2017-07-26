package com.compomics.util.test.general;

import com.compomics.util.experiment.identification.ptm.PtmSiteMapping;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * This class tests the general methods of the util class.
 *
 * @author Marc Vaudel
 */
public class PtmsiteMappingTest extends TestCase {

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
    public void testAlign() {

        Integer[] array1 = {0, 1, 13, 25, 15, 6, 99};
        Integer[] array2 = {100, 2, 12, 14, 18, 30, 115, 1000};
        ArrayList<Integer> serie1 = new ArrayList<>(Arrays.asList(array1));
        ArrayList<Integer> serie2 = new ArrayList<>(Arrays.asList(array2));
        HashMap<Integer, Integer> result = PtmSiteMapping.align(serie1, serie2);
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
        ArrayList<Integer> serie3 = new ArrayList<>(Arrays.asList(array3));
        ArrayList<Integer> serie4 = new ArrayList<>(Arrays.asList(array4));
        result = PtmSiteMapping.align(serie3, serie4);
        Assert.assertTrue(result.size() == serie3.size());
        Assert.assertTrue(result.containsKey(5));
        Assert.assertTrue(result.get(5) == 4);

        result = PtmSiteMapping.align(serie4, serie3);
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
    
    /**
     * Tests the results of the integer full alignment method.
     * 
     * input
     * serie1 = {0, 1, 13, 25, 15, 6, 99}
     * serie2 = {100, 2, 12, 14, 18, 30, 115, 1000}
     * 
     * shall return
     * result = {0-> 115, 1->2, 6-> 18, 13->12, 15->14, 25->30, 99->100}
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
    public void testAlignAll() {

        Integer[] array1 = {0, 1, 13, 25, 15, 6, 99};
        Integer[] array2 = {100, 2, 12, 14, 18, 30, 115, 1000};
        ArrayList<Integer> serie1 = new ArrayList<>(Arrays.asList(array1));
        ArrayList<Integer> serie2 = new ArrayList<>(Arrays.asList(array2));
        HashMap<Integer, Integer> result = PtmSiteMapping.alignAll(serie1, serie2);
        Assert.assertTrue(result.size() == serie1.size());
        Assert.assertTrue(result.containsKey(0));
        Assert.assertTrue(result.get(0) == 115);
        Assert.assertTrue(result.containsKey(1));
        Assert.assertTrue(result.get(1) == 2);
        Assert.assertTrue(result.containsKey(6));
        Assert.assertTrue(result.get(6) == 18);
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
        ArrayList<Integer> serie3 = new ArrayList<>(Arrays.asList(array3));
        ArrayList<Integer> serie4 = new ArrayList<>(Arrays.asList(array4));
        result = PtmSiteMapping.align(serie3, serie4);
        Assert.assertTrue(result.size() == serie3.size());
        Assert.assertTrue(result.containsKey(5));
        Assert.assertTrue(result.get(5) == 4);

        result = PtmSiteMapping.align(serie4, serie3);
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
    
    /**
     * Tests the results of the integer full alignment with constraints method.
     * 
     * input
     * { 
     * 0 -> {100, 2, 12, 14, 18, 30, 115, 1000}, 
     * 1 -> {12}, 
     * 2 -> {3, 12, 14}, 
     * 8 -> {12},
     * 13 -> {3, 12, 14}, 
     * 25 -> {100, 2, 12, 14, 18, 30, 115, 1000}, 
     * 15 -> {100, 2, 12, 14, 18, 30, 115, 1000}, 
     * 6 -> {100, 2, 12, 14, 18, 30, 115, 1000}, 
     * 99 -> {3} }
     * }
     *
     * shall return
     * {1 -> null, 8 -> 12, 99 -> 3, 13 -> 14, 2 -> null, 0 -> 2, 6 -> 100, 15 -> 18, 25 -> 30}
     * 
     * input
     * {5 -> {0, 1, 2, 3, 4, 7, 8, 9, 10}}
     * 
     * shall return
     * result = {5->4}
     * 
     * input
     * {0 -> {5}, 1 -> {5}, 2 -> {5}, 3 -> {5}, 4 -> {5}, 7 -> {5}, 8 -> {5}, 9 -> {5}, 10 -> {5}}
     * 
     * shall return
     * result = {0->null, 1->null, 2->null, 3->null, 4->5, 7->null, 8->null, 9->null, 10->null}
     */
    public void testAlignMap() {

        HashMap<Integer, ArrayList<Integer>> input = new HashMap<>();
        input.put(0, new ArrayList<>());
        input.get(0).add(100);
        input.get(0).add(2);
        input.get(0).add(12);
        input.get(0).add(14);
        input.get(0).add(18);
        input.get(0).add(30);
        input.get(0).add(115);
        input.get(0).add(1000);
        input.put(1, new ArrayList<>());
        input.get(1).add(12);
        input.put(2, new ArrayList<>());
        input.get(2).add(3);
        input.get(2).add(12);
        input.get(2).add(14);
        input.put(8, new ArrayList<>());
        input.get(8).add(12);
        input.put(13, new ArrayList<>());
        input.get(13).add(3);
        input.get(13).add(12);
        input.get(13).add(14);
        input.put(25, new ArrayList<>());
        input.get(25).add(100);
        input.get(25).add(2);
        input.get(25).add(12);
        input.get(25).add(14);
        input.get(25).add(18);
        input.get(25).add(30);
        input.get(25).add(115);
        input.get(25).add(1000);
        input.put(15, new ArrayList<>());
        input.get(15).add(100);
        input.get(15).add(2);
        input.get(15).add(12);
        input.get(15).add(14);
        input.get(15).add(18);
        input.get(15).add(30);
        input.get(15).add(115);
        input.get(15).add(1000);
        input.put(6, new ArrayList<>());
        input.get(6).add(100);
        input.get(6).add(2);
        input.get(6).add(12);
        input.get(6).add(14);
        input.get(6).add(18);
        input.get(6).add(30);
        input.get(6).add(115);
        input.get(6).add(1000);
        input.put(99, new ArrayList<>());
        input.get(99).add(3);

        HashMap<Integer, Integer> result = PtmSiteMapping.alignAll(input);
        Assert.assertTrue(result.size() == 9);
        Assert.assertTrue(result.containsKey(1));
        Assert.assertTrue(result.get(1) == null);
        Assert.assertTrue(result.containsKey(8));
        Assert.assertTrue(result.get(8) == 12);
        Assert.assertTrue(result.containsKey(99));
        Assert.assertTrue(result.get(99) == 3);
        Assert.assertTrue(result.containsKey(2));
        Assert.assertTrue(result.get(2) == null);
        Assert.assertTrue(result.containsKey(13));
        Assert.assertTrue(result.get(13) == 14);
        Assert.assertTrue(result.containsKey(0));
        Assert.assertTrue(result.get(0) == 2);
        Assert.assertTrue(result.containsKey(6));
        Assert.assertTrue(result.get(6) == 100);
        Assert.assertTrue(result.containsKey(15));
        Assert.assertTrue(result.get(15) == 18);
        Assert.assertTrue(result.containsKey(25));
        Assert.assertTrue(result.get(25) == 30);

        input = new HashMap<>();
        input.put(5, new ArrayList<>());
        input.get(5).add(0);
        input.get(5).add(1);
        input.get(5).add(2);
        input.get(5).add(3);
        input.get(5).add(4);
        input.get(5).add(7);
        input.get(5).add(8);
        input.get(5).add(9);
        input.get(5).add(10);
        result = PtmSiteMapping.alignAll(input);
        Assert.assertTrue(result.size() == 1);
        Assert.assertTrue(result.containsKey(5));
        Assert.assertTrue(result.get(5) == 4);

        input = new HashMap<>();
        input.put(0, new ArrayList<>());
        input.get(0).add(5);
        input.put(1, new ArrayList<>());
        input.get(1).add(5);
        input.put(2, new ArrayList<>());
        input.get(2).add(5);
        input.put(3, new ArrayList<>());
        input.get(3).add(5);
        input.put(4, new ArrayList<>());
        input.get(4).add(5);
        input.put(7, new ArrayList<>());
        input.get(7).add(5);
        input.put(8, new ArrayList<>());
        input.get(8).add(5);
        input.put(9, new ArrayList<>());
        input.get(9).add(5);
        input.put(10, new ArrayList<>());
        input.get(10).add(5);
        result = PtmSiteMapping.alignAll(input);
        Assert.assertTrue(result.size() == 9);
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
