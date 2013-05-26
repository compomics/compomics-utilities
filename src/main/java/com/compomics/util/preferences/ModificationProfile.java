package com.compomics.util.preferences;

import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import java.awt.Color;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * This class stores the information about the modification preferences (colors,
 * names) used for the selected project.
 *
 * @author Marc Vaudel
 */
public class ModificationProfile implements Serializable {

    /**
     * Serial version number for serialization compatibility.
     */
    static final long serialVersionUID = 342611308111304721L;
    /**
     * Mapping of the utilities modification names to the PeptideShaker names.
     *
     * @deprecated use the expected variable modification lists.
     */
    private HashMap<String, String> modificationNames = new HashMap<String, String>();
    /**
     * List of the expected fixed modifications.
     */
    private ArrayList<String> fixedModifications = new ArrayList<String>();
    /**
     * List of the expected variable modifications.
     */
    private ArrayList<String> variableModifications = new ArrayList<String>();
    /**
     * List of modifications searched during the second pass search.
     */
    private ArrayList<String> refinementModifications = new ArrayList<String>();
    /**
     * Map of the OMSSA indexes used for user modifications in this search.
     */
    private HashMap<Integer, String> omssaIndexes = new HashMap<Integer, String>();
    /**
     * Mapping of the expected modification names to the color used.
     */
    private HashMap<String, Color> colors = new HashMap<String, Color>();
    /**
     * Back-up mapping of the PTMs for portability.
     */
    private HashMap<String, PTM> backUp = new HashMap<String, PTM>();

    /**
     * Constructor.
     */
    public ModificationProfile() {
    }

    /**
     * Returns the expected variable modification names included in this
     * profile.
     *
     * @return the expected variable modification names included in this profile
     */
    public ArrayList<String> getVariableModifications() {
        return variableModifications;
    }

    /**
     * Returns the searched fixed modifications names.
     *
     * @return the searched fixed modifications names
     */
    public ArrayList<String> getFixedModifications() {
        return fixedModifications;
    }

    /**
     * Return the refinement modifications used for the second pass search.
     *
     * @return the refinement modifications
     */
    public ArrayList<String> getRefinementModifications() {
        return refinementModifications;
    }

    /**
     * Returns a list of all searched modifications.
     *
     * @return a list of all searched modifications
     */
    public ArrayList<String> getAllModifications() {
        compatibilityCheck();
        ArrayList<String> result = new ArrayList<String>();
        result.addAll(fixedModifications);
        result.addAll(variableModifications);
        result.addAll(refinementModifications);
        return result;
    }

    /**
     * Returns a list of all searched modifications but the fixed ones.
     *
     * @return a list of all searched modifications but the fixed ones
     */
    public ArrayList<String> getAllNotFixedModifications() {
        ArrayList<String> result = new ArrayList<String>();
        result.addAll(variableModifications);
        result.addAll(refinementModifications);
        return result;
    }

    /**
     * Adds a variable modification. The modification name is added in the
     * variable modifications names list and the modification is saved in the
     * back-up. In case a modification with the same name was already used it
     * will be silently overwritten.
     *
     * @param modification The modification to add
     */
    public void addVariableModification(PTM modification) {
        String modName = modification.getName();
        if (!variableModifications.contains(modName)) {
            variableModifications.add(modName);
        }
        modification.setShortName(PTMFactory.getInstance().getShortName(modName));
        backUp.put(modName, modification);
    }

    /**
     * Adds a refinement modification. The modification name is added in the
     * refinement modifications names list and the modification is saved in the
     * back-up. In case a modification with the same name was already used it
     * will be silently overwritten.
     *
     * @param modification The modification to add
     */
    public void addRefinementModification(PTM modification) {
        String modName = modification.getName();
        if (!refinementModifications.contains(modName)) {
            refinementModifications.add(modName);
        }
        modification.setShortName(PTMFactory.getInstance().getShortName(modName));
        backUp.put(modName, modification);
    }

    /**
     * Adds a fixed modification. The modification name is added in the fixed
     * modifications names list and the modification is saved in the back-up. In
     * case a modification with the same name was already used it will be
     * silently overwritten.
     *
     * @param modification The modification to add
     */
    public void addFixedModification(PTM modification) {
        String modName = modification.getName();
        if (!fixedModifications.contains(modName)) {
            fixedModifications.add(modName);
        }
        modification.setShortName(PTMFactory.getInstance().getShortName(modName));
        backUp.put(modName, modification);
    }

    /**
     * Sets a new color for the given expected modification.
     *
     * @param expectedModification the name of the expected modification
     * @param color the new color
     */
    public void setColor(String expectedModification, Color color) {
        colors.put(expectedModification, color);
    }

    /**
     * Returns the color used to code the given modification.
     *
     * @param modification the name of the given expected modification
     * @return the corresponding color
     */
    public Color getColor(String modification) {
        if (!colors.containsKey(modification)) {
            PTMFactory ptmFactory = PTMFactory.getInstance();
            setColor(modification, ptmFactory.getColor(modification));
        }
        return colors.get(modification);
    }

    /**
     * Checks the compatibility with older versions of the class and makes the
     * necessary changes. By default all modifications are set as variable.
     */
    public void compatibilityCheck() {
        if (fixedModifications == null) {
            fixedModifications = new ArrayList<String>();
        }
        if (variableModifications == null) {
            variableModifications = new ArrayList<String>();
            for (String modName : modificationNames.values()) {
                variableModifications.add(modName);
            }
        }
        if (refinementModifications == null) {
            refinementModifications = new ArrayList<String>();
        }
        if (backUp == null) {
            backUp = new HashMap<String, PTM>();
        }
    }

    /**
     * Returns the names of the backed-up PTMs.
     *
     * @return the names of the backed-up PTMs
     */
    public Set<String> getBackedUpPtms() {
        if (backUp == null) {
            repair();
        }
        return backUp.keySet();
    }

    /**
     * Returns the back-ed up PTM with the given name.
     *
     * @param modName the name of the PTM of interest
     * @return the corresponding PTM. Null if not found.
     */
    public PTM getPtm(String modName) {
        return backUp.get(modName);
    }

    /**
     * Removes a modification from the list of variable modifications.
     *
     * @param modificationName the name of the modification
     */
    public void removeVariableModification(String modificationName) {
        while (variableModifications.contains(modificationName)) {
            variableModifications.remove(modificationName);
        }
    }

    /**
     * Removes a modification from the list of fixed modifications.
     *
     * @param modificationName the name of the modification
     */
    public void removeFixedModification(String modificationName) {
        while (fixedModifications.contains(modificationName)) {
            fixedModifications.remove(modificationName);
        }
    }

    /**
     * Removes a modification from the list of refinement modifications
     * modifications.
     *
     * @param modificationName the name of the modification
     */
    public void removeRefinementModification(String modificationName) {
        while (refinementModifications.contains(modificationName)) {
            refinementModifications.remove(modificationName);
        }
    }

    /**
     * Sets the OMSSA index for a given modification. If another modification
     * was already given with the same index the previous setting will be
     * silently overwritten.
     *
     * @param modificationName the name of the modification
     * @param omssaIndex the OMSSA index of the modification
     */
    public void setOmssaIndex(String modificationName, int omssaIndex) {
        omssaIndexes.put(omssaIndex, modificationName);
    }

    /**
     * Returns the name of the modification indexed by the given OMSSA index.
     * Null if not found.
     *
     * @param omssaIndex the OMSSA index of the modification to look for
     * @return the name of the modification indexed by the given OMSSA index
     */
    public String getModification(int omssaIndex) {
        return omssaIndexes.get(omssaIndex);
    }
    
    /**
     * Indicates whether the modification profile has OMSSA indexes. 
     * 
     * @return true if an OMSSA indexes map is set
     */
    public boolean hasOMSSAIndexes() {
        return omssaIndexes != null && !omssaIndexes.isEmpty();
    }

    /**
     * Returns the OMSSA index of a given modification, null if not found.
     *
     * @param modificationName the name of the modification
     * @return the corresponding OMSSA index
     */
    public Integer getOmssaIndex(String modificationName) {
        for (int index : omssaIndexes.keySet()) {
            if (modificationName.equalsIgnoreCase(omssaIndexes.get(index))) {
                return index;
            }
        }
        return null;
    }

    /**
     * Indicates whether the modification is contained in the profile, looking
     * into all modifications (fixed, variable and refinement)
     *
     * @param modificationName the name of the modification
     * @return a boolean indicating whether the modification is contained in the
     * mapping
     */
    public boolean contains(String modificationName) {
        return variableModifications.contains(modificationName)
                || fixedModifications.contains(modificationName)
                || refinementModifications.contains(modificationName);
    }

    /**
     * Returns a list containing all not fixed modifications with the same mass.
     * Warning: all modifications of the profile must be loaded in the PTM
     * factory.
     *
     * @param ptmMass the mass
     * @return a list of all not fixed modifications with the same mass
     */
    public ArrayList<String> getSimilarNotFixedModifications(Double ptmMass) {
        PTMFactory ptmFactory = PTMFactory.getInstance();
        ArrayList<String> ptms = new ArrayList<String>();
        for (String ptmName : getAllNotFixedModifications()) {
            PTM ptm = ptmFactory.getPTM(ptmName);
            if (!ptms.contains(ptmName) && ptm.getMass() == ptmMass) {
                ptms.add(ptmName);
            }
        }
        return ptms;
    }

    /**
     * Returns true of the two profiles are identical.
     *
     * @param otherProfile the profile to compare against
     * @return true of the two profiles are identical
     */
    public boolean equals(ModificationProfile otherProfile) {

        if (otherProfile == null) {
            return false;
        }

        // note that the following three tests results in false even if only the order is different

        if (!this.getVariableModifications().equals(otherProfile.getVariableModifications())) {
            return false;
        }
        if (!this.getFixedModifications().equals(otherProfile.getFixedModifications())) {
            return false;
        }
        if (!this.getRefinementModifications().equals(otherProfile.getRefinementModifications())) {
            return false;
        }

        if (this.omssaIndexes.size() != otherProfile.omssaIndexes.size()) {
            return false;
        }

        Iterator<Integer> omssaIndexkeys = this.omssaIndexes.keySet().iterator();

        while (omssaIndexkeys.hasNext()) {
            Integer tempKey = omssaIndexkeys.next();
            if (!otherProfile.omssaIndexes.containsKey(tempKey)) {
                return false;
            }
            if (!this.omssaIndexes.get(tempKey).equals(otherProfile.omssaIndexes.get(tempKey))) {
                return false;
            }
        }

        if (this.colors.size() != otherProfile.colors.size()) {
            return false;
        }

        Iterator<String> colorKeys = this.colors.keySet().iterator();

        while (colorKeys.hasNext()) {
            String tempKey = colorKeys.next();
            if (!otherProfile.colors.containsKey(tempKey)) {
                return false;
            }
            if (!this.colors.get(tempKey).equals(otherProfile.colors.get(tempKey))) {
                return false;
            }
        }

        if (this.backUp.size() != otherProfile.backUp.size()) {
            return false;
        }

        Iterator<String> backupKeys = this.backUp.keySet().iterator();

        while (backupKeys.hasNext()) {
            String tempKey = backupKeys.next();
            if (!otherProfile.backUp.containsKey(tempKey)) {
                return false;
            }

            // @TODO: a test for identical ptms (and not just name as above) should be added
//            if (!this.backUp.get(tempKey).equals(otherProfile.backUp.get(tempKey))) {
//                return false;
//            }
        }

        return true;
    }

    /**
     * Sets empty lists and maps to the values lost due to backward
     * compatability issues.
     */
    public void repair() {
        if (fixedModifications == null) {
            fixedModifications = new ArrayList<String>();
        }
        if (variableModifications == null) {
            variableModifications = new ArrayList<String>();
        }
        if (refinementModifications == null) {
            refinementModifications = new ArrayList<String>();
        }
        if (omssaIndexes == null) {
            omssaIndexes = new HashMap<Integer, String>();
        }
        if (colors == null) {
            colors = new HashMap<String, Color>();
        }
        if (backUp == null) {
            backUp = new HashMap<String, PTM>();
        }
    }
}
