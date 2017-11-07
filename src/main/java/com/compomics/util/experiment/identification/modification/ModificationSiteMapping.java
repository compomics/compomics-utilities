package com.compomics.util.experiment.identification.modification;

import com.compomics.util.Util;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * This class contains convenience methods to map modifications on potential sites.
 *
 * @author Marc Vaudel
 */
public class ModificationSiteMapping {

    /**
     * Aligns two series of integer, minimizing the distance between them and
     * excluding outliers.
     *
     * Example: series1 = {0, 1, 13, 25, 15, 6, 99} series2 = {100, 2, 12, 14,
     * 18, 30, 115, 1000} result = {0&gt;null, 1&gt;2, 6&gt;null, 13&gt;12,
     * 15&gt;14, 25&gt;18, 99&gt;100}
     *
     * @param serie1 first list of integer
     * @param serie2 second list of integer
     * 
     * @return a map of the doublets created.
     */
    public static HashMap<Integer, Integer> align(Collection<Integer> serie1, Collection<Integer> serie2) {
        HashMap<Integer, Integer> result = new HashMap<>();
        if (serie1 == null || serie1.isEmpty()) {
            return result;
        }
        ArrayList<Integer> sortedSerie1 = new ArrayList<>(serie1);
        Collections.sort(sortedSerie1);
        if (serie2 == null || serie2.isEmpty()) {
            for (int index : serie1) {
                result.put(index, null);
            }
            return result;
        }
        ArrayList<Integer> sortedSerie2 = new ArrayList<>(serie2);
        Collections.sort(sortedSerie2);
        int lastj = 0;
        int firsti = 0;
        for (int i = 0; i < sortedSerie1.size() - 1; i++) {
            if (Math.abs(sortedSerie1.get(i + 1) - sortedSerie2.get(0)) < Math.abs(sortedSerie1.get(i) - sortedSerie2.get(0))) {
                result.put(sortedSerie1.get(i), null);
                firsti = i + 1;
            } else {
                break;
            }
        }
        for (int i = firsti; i < sortedSerie1.size(); i++) {
            Integer bestj = null;
            Integer bestDistance = null;
            if (lastj < sortedSerie2.size()) {
                for (int j = lastj; j < sortedSerie2.size(); j++) {
                    if (i < sortedSerie1.size() - 1
                            && (sortedSerie2.get(j) >= sortedSerie1.get(i + 1)
                            || Math.abs(sortedSerie2.get(j) - sortedSerie1.get(i + 1)) < Math.abs(sortedSerie2.get(j) - sortedSerie1.get(i)))) {
                        break;
                    }
                    if (bestDistance == null || Math.abs(sortedSerie2.get(j) - sortedSerie1.get(i)) < bestDistance) {
                        bestDistance = Math.abs(sortedSerie2.get(j) - sortedSerie1.get(i));
                        bestj = j;
                    }
                }
            }
            if (bestj != null) {
                result.put(sortedSerie1.get(i), sortedSerie2.get(bestj));
                lastj = bestj + 1;
            } else {
                result.put(sortedSerie1.get(i), null);
            }
        }
        return result;
    }

    /**
     * Aligns two series of integer, minimizing the distance between them.
     *
     * Example: serie1 = {0, 1, 13, 25, 15, 6, 99} serie2 = {100, 2, 12, 14, 18,
     * 30, 115, 1000} result = {0&gt; 115, 1&gt;2, 6&gt;30, 13&gt;12, 15&gt;14,
     * 25&gt;18, 99&gt;100}
     *
     * @param serie1 first list of integer
     * @param serie2 second list of integer
     * @return a map of the doublets created.
     */
    public static HashMap<Integer, Integer> alignAll(Collection<Integer> serie1, Collection<Integer> serie2) {
        HashMap<Integer, Integer> tempResult, result = new HashMap<>();
        if (serie1 == null || serie1.isEmpty()) {
            return result;
        }
        if (serie2 == null || serie2.isEmpty()) {
            for (int index : serie1) {
                result.put(index, null);
            }
            return result;
        }
        int diff = Math.max(serie1.size() - serie2.size(), 0);
        int nNull = diff + 1;
        while (nNull > diff) {
            ArrayList<Integer> tempSerie1 = new ArrayList<>();
            ArrayList<Integer> tempSerie2 = new ArrayList<>(serie2);
            Integer i2;
            for (int i1 : serie1) {
                i2 = result.get(i1);
                if (i2 != null) {
                    tempSerie2.remove(i2);
                } else {
                    tempSerie1.add(i1);
                }
            }
            tempResult = align(tempSerie1, tempSerie2);
            nNull = 0;
            for (int i1 : tempResult.keySet()) {
                i2 = tempResult.get(i1);
                if (i2 == null) {
                    nNull++;
                }
                result.put(i1, i2);
            }
        }
        return result;
    }

    /**
     * Aligns a series of integer on possible targets maximizing the number of
     * matched targets.
     *
     * Example:
     *
     * input = { 0 &gt; {100, 2, 12, 14, 18, 30, 115, 1000}, 1 &gt; {12}, 2 &gt;
     * {3, 12, 14}, 8 &gt; {12}, 13 &gt; {3, 12, 14}, 25 &gt; {100, 2, 12, 14,
     * 18, 30, 115, 1000}, 15 &gt; {100, 2, 12, 14, 18, 30, 115, 1000}, 6 &gt;
     * {100, 2, 12, 14, 18, 30, 115, 1000}, 99 &gt; {3} }
     *
     * result = {1 &gt; null, 8 &gt; 12, 99 &gt; 3, 13 &gt; 14, 2 &gt; null, 0
     * &gt; 2, 6 &gt; 100, 15 &gt; 18, 25 &gt; 30}
     *
     * @param input the input map
     * @return a map of the doublets created.
     */
    public static HashMap<Integer, Integer> alignAll(HashMap<Integer, ArrayList<Integer>> input) {

        HashMap<Integer, Integer> tempResult, result = new HashMap<>();
        if (input == null || input.isEmpty()) {
            return result;
        }

        HashMap<Integer, ArrayList<Integer>> inputCopy = new HashMap<>();
        for (int i1 : input.keySet()) {
            inputCopy.put(i1, new ArrayList<>());
            for (int i2 : input.get(i1)) {
                inputCopy.get(i1).add(i2);
            }
        }

        while (!inputCopy.isEmpty()) {

            HashMap<Integer, ArrayList<Integer>> sizesMap = new HashMap<>();

            for (int i : inputCopy.keySet()) {
                if (inputCopy.get(i) != null && !inputCopy.get(i).isEmpty()) {
                    int size = inputCopy.get(i).size();
                    if (!sizesMap.containsKey(size)) {
                        sizesMap.put(size, new ArrayList<>());
                    }
                    sizesMap.get(size).add(i);
                } else {
                    inputCopy.remove(i);
                }
            }

            ArrayList<Integer> sizes = new ArrayList<>(sizesMap.keySet());
            Collections.sort(sizes);
            int size = sizes.get(0);
            ArrayList<Integer> serie1 = new ArrayList<>();
            int i1temp = sizesMap.get(size).get(0);
            ArrayList<Integer> serie2 = inputCopy.get(i1temp);

            for (int i1 : sizesMap.get(size)) {
                if (Util.sameLists(serie2, inputCopy.get(i1))) {
                    serie1.add(i1);
                }
            }

            tempResult = alignAll(serie1, serie2);
            result.putAll(tempResult);

            for (int i1 : inputCopy.keySet()) {
                ArrayList<Integer> toRemove2 = new ArrayList<>();
                for (int i2 : inputCopy.get(i1)) {
                    if (tempResult.containsValue(i2)) {
                        toRemove2.add(i2);
                    }
                }
                for (Integer i2 : toRemove2) {
                    inputCopy.get(i1).remove(i2);
                }
            }

            for (Integer i1 : serie1) {
                inputCopy.remove(i1);
                sizesMap.get(size).remove(i1);
            }

            ArrayList<Integer> toRemove1 = new ArrayList<>();
            for (int i1 : inputCopy.keySet()) {
                if (inputCopy.get(i1).isEmpty()) {
                    toRemove1.add(i1);
                    result.put(i1, null);
                }
            }

            for (int i1 : toRemove1) {
                inputCopy.remove(i1);
            }

            if (sizesMap.get(size).isEmpty()) {
                sizesMap.remove(size);
            }
        }

        return result;
    }
}
