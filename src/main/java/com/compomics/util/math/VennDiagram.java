package com.compomics.util.math;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class for calculating the groups for Venn diagrams.
 *
 * @author Harald Barsnes
 */
public class VennDiagram {

    /**
     * Create the Venn diagram groupings based on the provided data.
     *
     * @param groupA the data in group A
     * @param groupB the data in group B
     * @param groupC the data in group C
     * @param groupD the data in group D
     * @return the Venn diagram groupings
     */
    public static HashMap<String, ArrayList<String>> vennDiagramMaker(ArrayList<String> groupA, ArrayList<String> groupB, ArrayList<String> groupC, ArrayList<String> groupD) {

        HashMap<String, ArrayList<String>> tempVennDiagramResults = new HashMap<>();

        ArrayList<String> a = new ArrayList<>();
        ArrayList<String> b = new ArrayList<>();
        ArrayList<String> c = new ArrayList<>();
        ArrayList<String> d = new ArrayList<>();

        ArrayList<String> ab = new ArrayList<>();
        ArrayList<String> ac = new ArrayList<>();
        ArrayList<String> ad = new ArrayList<>();
        ArrayList<String> bc = new ArrayList<>();
        ArrayList<String> bd = new ArrayList<>();
        ArrayList<String> cd = new ArrayList<>();

        ArrayList<String> abc = new ArrayList<>();
        ArrayList<String> abd = new ArrayList<>();
        ArrayList<String> acd = new ArrayList<>();
        ArrayList<String> bcd = new ArrayList<>();

        ArrayList<String> abcd = new ArrayList<>();

        ArrayList<String> allDataPoints = new ArrayList<>();

        for (String temp : groupA) {

            if (!allDataPoints.contains(temp)) {

                boolean inGroupB = groupB.contains(temp);
                boolean inGroupC = groupC.contains(temp);
                boolean inGroupD = groupD.contains(temp);

                if (!inGroupB && !inGroupC && !inGroupD) {
                    a.add(temp);
                } else {
                    if (inGroupB && !inGroupC && !inGroupD) {
                        ab.add(temp);
                    } else if (!inGroupB && inGroupC && !inGroupD) {
                        ac.add(temp);
                    } else if (!inGroupB && !inGroupC && inGroupD) {
                        ad.add(temp);
                    } else if (inGroupB && inGroupC && !inGroupD) {
                        abc.add(temp);
                    } else if (inGroupB && !inGroupC && inGroupD) {
                        abd.add(temp);
                    } else if (!inGroupB && inGroupC && inGroupD) {
                        acd.add(temp);
                    } else {
                        abcd.add(temp);
                    }
                }

                allDataPoints.add(temp);
            }
        }

        for (String temp : groupB) {

            if (!allDataPoints.contains(temp)) {

                boolean inGroupA = groupA.contains(temp);
                boolean inGroupC = groupC.contains(temp);
                boolean inGroupD = groupD.contains(temp);

                if (!inGroupA && !inGroupC && !inGroupD) {
                    b.add(temp);
                } else {
                    if (inGroupA && !inGroupC && !inGroupD) {
                        ab.add(temp);
                    } else if (!inGroupA && inGroupC && !inGroupD) {
                        bc.add(temp);
                    } else if (!inGroupA && !inGroupC && inGroupD) {
                        bd.add(temp);
                    } else if (inGroupA && inGroupC && !inGroupD) {
                        abc.add(temp);
                    } else if (inGroupA && !inGroupC && inGroupD) {
                        abd.add(temp);
                    } else if (!inGroupA && inGroupC && inGroupD) {
                        bcd.add(temp);
                    } else {
                        abcd.add(temp);
                    }
                }

                allDataPoints.add(temp);
            }
        }

        for (String temp : groupC) {

            if (!allDataPoints.contains(temp)) {

                boolean inGroupA = groupA.contains(temp);
                boolean inGroupB = groupB.contains(temp);
                boolean inGroupD = groupD.contains(temp);

                if (!inGroupA && !inGroupB && !inGroupD) {
                    c.add(temp);
                } else {
                    if (inGroupA && !inGroupB && !inGroupD) {
                        ac.add(temp);
                    } else if (!inGroupA && inGroupB && !inGroupD) {
                        bc.add(temp);
                    } else if (!inGroupA && !inGroupB && inGroupD) {
                        cd.add(temp);
                    } else if (inGroupA && inGroupB && !inGroupD) {
                        abc.add(temp);
                    } else if (inGroupA && !inGroupB && inGroupD) {
                        acd.add(temp);
                    } else if (!inGroupA && inGroupB && inGroupD) {
                        bcd.add(temp);
                    } else {
                        abcd.add(temp);
                    }
                }

                allDataPoints.add(temp);
            }
        }

        for (String temp : groupD) {

            if (!allDataPoints.contains(temp)) {

                boolean inGroupA = groupA.contains(temp);
                boolean inGroupB = groupB.contains(temp);
                boolean inGroupC = groupC.contains(temp);

                if (!inGroupA && !inGroupB && !inGroupC) {
                    d.add(temp);
                } else {
                    if (inGroupA && !inGroupB && !inGroupC) {
                        ad.add(temp);
                    } else if (!inGroupA && inGroupB && !inGroupC) {
                        bd.add(temp);
                    } else if (!inGroupA && !inGroupB && inGroupC) {
                        cd.add(temp);
                    } else if (inGroupA && inGroupB && !inGroupC) {
                        abd.add(temp);
                    } else if (inGroupA && !inGroupB && inGroupC) {
                        acd.add(temp);
                    } else if (!inGroupA && inGroupB && inGroupC) {
                        bcd.add(temp);
                    } else {
                        abcd.add(temp);
                    }
                }

                allDataPoints.add(temp);
            }
        }

        // add the results to the hashmap
        tempVennDiagramResults.put("a", a);
        tempVennDiagramResults.put("b", b);
        tempVennDiagramResults.put("c", c);
        tempVennDiagramResults.put("d", d);

        tempVennDiagramResults.put("ab", ab);
        tempVennDiagramResults.put("ac", ac);
        tempVennDiagramResults.put("ad", ad);
        tempVennDiagramResults.put("bc", bc);
        tempVennDiagramResults.put("bd", bd);
        tempVennDiagramResults.put("cd", cd);

        tempVennDiagramResults.put("abc", abc);
        tempVennDiagramResults.put("abd", abd);
        tempVennDiagramResults.put("acd", abd);
        tempVennDiagramResults.put("bcd", bcd);

        tempVennDiagramResults.put("abcd", abcd);

        boolean debug = false;

        if (debug) {

            System.out.print("a: ");
            for (String temp : a) {
                System.out.print(temp + ", ");
            }
            System.out.println();

            System.out.print("b: ");
            for (String temp : b) {
                System.out.print(temp + ", ");
            }
            System.out.println();

            System.out.print("c: ");
            for (String temp : c) {
                System.out.print(temp + ", ");
            }
            System.out.println();

            System.out.print("d: ");
            for (String temp : d) {
                System.out.print(temp + ", ");
            }
            System.out.println();

            System.out.print("ab: ");
            for (String temp : ab) {
                System.out.print(temp + ", ");
            }
            System.out.println();

            System.out.print("ac: ");
            for (String temp : ac) {
                System.out.print(temp + ", ");
            }
            System.out.println();

            System.out.print("ad: ");
            for (String temp : ad) {
                System.out.print(temp + ", ");
            }
            System.out.println();

            System.out.print("bc: ");
            for (String temp : bc) {
                System.out.print(temp + ", ");
            }
            System.out.println();

            System.out.print("bd: ");
            for (String temp : bd) {
                System.out.print(temp + ", ");
            }
            System.out.println();

            System.out.print("cd: ");
            for (String temp : cd) {
                System.out.print(temp + ", ");
            }
            System.out.println();

            System.out.print("abc: ");
            for (String temp : abc) {
                System.out.print(temp + ", ");
            }
            System.out.println();

            System.out.print("abd: ");
            for (String temp : abd) {
                System.out.print(temp + ", ");
            }
            System.out.println();

            System.out.print("bcd: ");
            for (String temp : bcd) {
                System.out.print(temp + ", ");
            }
            System.out.println();

            System.out.print("acd: ");
            for (String temp : acd) {
                System.out.print(temp + ", ");
            }
            System.out.println();

            System.out.print("abcd: ");
            for (String temp : abcd) {
                System.out.print(temp + ", ");
            }
            System.out.println();
        }

        return tempVennDiagramResults;
    }
}
