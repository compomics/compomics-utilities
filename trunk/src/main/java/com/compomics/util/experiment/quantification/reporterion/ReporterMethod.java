package com.compomics.util.experiment.quantification.reporterion;

import com.compomics.util.experiment.biology.ions.ReporterIon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * This class contains information relative to a reporter quantification method.
 *
 * @author Marc Vaudel Date: Sep 29, 2010 Time: 5:52:30 PM
 */
public class ReporterMethod {

    /**
     * Map of the reagents. Reagent name -> reagent
     */
    private HashMap<String, Reagent> reagents;
    /**
     * The name of the method.
     */
    private String name;
    /**
     * The names of the reporter ions in this method
     */
    private HashMap<String, ReporterIon> reporterIonsMap;

    /**
     * Constructor for a reporter method.
     *
     * @param name the name of the method
     * @param reagents list of reagents
     */
    public ReporterMethod(String name, ArrayList<Reagent> reagents) {
        this.name = name;
        this.reagents = new HashMap<String, Reagent>(reagents.size());
        reporterIonsMap = new HashMap<String, ReporterIon>(reagents.size());
        for (Reagent reagent : reagents) {
            String reagentName = reagent.getName();
            if (this.reagents.containsKey(reagentName)) {
                throw new IllegalArgumentException("Reagent name " + reagentName + " is duplicated in the reporter method " + name + ".");
            }
            this.reagents.put(reagent.getName(), reagent);
            ReporterIon reporterIon = reagent.getReporterIon();
            String reporterIonName = reporterIon.getName();
            if (reporterIonsMap.containsKey(reporterIonName)) {
                throw new IllegalArgumentException("Reporter ion name " + reporterIonName + " is duplicated in the reporter method " + name + ".");
            }
            reporterIonsMap.put(reporterIonName, reporterIon);
        }
    }

    /**
     * Returns a list containing the names of the reporter ions.
     *
     * @return a list containing the names of the reporter ions
     */
    public Set<String> getReporterIonNames() {
        return reporterIonsMap.keySet();
    }

    /**
     * Returns the reporter ion of the given name, null if not found.
     *
     * @param reporterIonName the name of the reporter ion
     *
     * @return the reporter ion of interest
     */
    public ReporterIon getReporterIon(String reporterIonName) {
        return reporterIonsMap.get(reporterIonName);
    }
    
    /**
     * Returns the reagents available in this method.
     * 
     * @return the reagents available in this method
     */
    public Set<String> getReagentNames() {
        return reagents.keySet();
    }

    /**
     * Returns the reagent of the given name, null if not found.
     * 
     * @param reagentName the reagent name
     * 
     * @return the reagent of interest
     */
    public Reagent getReagent(String reagentName) {
        return reagents.get(name);
    }

    /**
     * Returns the name of the method.
     *
     * @return the name of the method
     */
    public String getName() {
        return name;
    }
}
