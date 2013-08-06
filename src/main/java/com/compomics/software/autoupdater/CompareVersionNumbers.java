package com.compomics.software.autoupdater;

import java.util.Comparator;
import java.util.Scanner;

/**
 *
 * @author Davy
 */
public class CompareVersionNumbers implements Comparator<String> {

    @Override
    public int compare(String oldVersionNumber, String newVersionNumber) {
        int compareInt = -1;
        Scanner a = (new Scanner(oldVersionNumber)).useDelimiter("\\.");
        Scanner b = (new Scanner(newVersionNumber)).useDelimiter("\\.");
        int oldversionnumber = 0, newversionnumber = 0;
        if (!newVersionNumber.contains("b") || !newVersionNumber.contains("beta")) {
            while (a.hasNext() && b.hasNext()) {
                oldversionnumber = Integer.parseInt(a.next());
                newversionnumber = Integer.parseInt(b.next());
                if (newversionnumber > oldversionnumber) {
                    compareInt = 1;
                }
            }
            if (b.hasNext() && !a.hasNext()) {
                compareInt = 1;
            } else if (!b.hasNext() && !a.hasNext() && oldversionnumber == newversionnumber) {
                compareInt = 0;
            }
        }
        return compareInt;
    }
}
