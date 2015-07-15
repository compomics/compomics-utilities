package com.compomics.util.preferences;

import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.identification.identification_parameters.OmssaParameters;
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
     * List of the expected fixed modifications.
     */
    private ArrayList<String> fixedModifications = new ArrayList<String>();
    /**
     * List of the expected variable modifications.
     */
    private ArrayList<String> variableModifications = new ArrayList<String>();
    /**
     * List of variable modifications searched during the second pass search.
     */
    private ArrayList<String> refinementVariableModifications = new ArrayList<String>();
    /**
     * List of variable modifications searched during the second pass search.
     */
    private ArrayList<String> refinementFixedModifications = new ArrayList<String>();
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
     * Constructor creating a new Modification profile based on the given one.
     * 
     * @param modificationProfile the modification profile
     */
    public ModificationProfile(ModificationProfile modificationProfile) {
        fixedModifications = modificationProfile.getFixedModifications();
        variableModifications = modificationProfile.getVariableModifications();
        refinementFixedModifications = modificationProfile.getRefinementFixedModifications();
        refinementVariableModifications = modificationProfile.getRefinementVariableModifications();
        colors = modificationProfile.getColors();
        backUp = modificationProfile.getBackedUpPtmsMap();
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
     * Return the refinement variable modifications used for the second pass
     * search.
     *
     * @return the refinement variable modifications
     */
    public ArrayList<String> getRefinementVariableModifications() {
        return refinementVariableModifications;
    }

    /**
     * Return the refinement fixed modifications used for the second pass
     * search.
     *
     * @return the refinement fixed modifications
     */
    public ArrayList<String> getRefinementFixedModifications() {
        return refinementFixedModifications;
    }

    /**
     * Returns a list of all searched modifications.
     *
     * @return a list of all searched modifications
     */
    public ArrayList<String> getAllModifications() {
        ArrayList<String> result = new ArrayList<String>();
        result.addAll(fixedModifications);
        result.addAll(variableModifications);
        for (String ptmName : refinementFixedModifications) {
            if (!result.contains(ptmName)) {
                result.add(ptmName);
            }
        }
        for (String ptmName : refinementVariableModifications) {
            if (!result.contains(ptmName)) {
                result.add(ptmName);
            }
        }
        return result;
    }

    /**
     * Returns a list of all searched modifications but the fixed ones. Note: to
     * be fixed a modification must be fixed during the first and second pass
     * searches.
     *
     * @return a list of all searched modifications but the fixed ones
     */
    public ArrayList<String> getAllNotFixedModifications() {
        ArrayList<String> result = new ArrayList<String>();
        result.addAll(variableModifications);
        for (String ptmName : refinementVariableModifications) {
            if (!result.contains(ptmName)) {
                result.add(ptmName);
            }
        }
        for (String ptmName : refinementFixedModifications) {
            if (!fixedModifications.contains(ptmName) && !result.contains(ptmName)) {
                result.add(ptmName);
            }
        }
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
        backUp.put(modName, modification);
    }

    /**
     * Adds a variable refinement modification. The modification name is added
     * in the refinement modifications names list and the modification is saved
     * in the back-up. In case a modification with the same name was already
     * used it will be silently overwritten.
     *
     * @param modification The modification to add
     */
    public void addRefinementVariableModification(PTM modification) {
        String modName = modification.getName();
        if (!refinementVariableModifications.contains(modName)) {
            refinementVariableModifications.add(modName);
        }
        backUp.put(modName, modification);
    }

    /**
     * Adds a fixed refinement modification. The modification name is added in
     * the refinement modifications names list and the modification is saved in
     * the back-up. In case a modification with the same name was already used
     * it will be silently overwritten.
     *
     * @param modification The modification to add
     */
    public void addRefinementFixedModification(PTM modification) {
        String modName = modification.getName();
        if (!refinementFixedModifications.contains(modName)) {
            refinementFixedModifications.add(modName);
        }
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
     * Returns the modification colors as a map.
     *
     * @return the modifications colors as a map
     */
    public HashMap<String, Color> getColors() {
        return colors;
    }

    /**
     * Returns the names of the backed-up PTMs.
     *
     * @return the names of the backed-up PTMs
     */
    public Set<String> getBackedUpPtms() {
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
     * Returns the PTMs backed-up as a map. PTM name &gt; PTM.
     *
     * @return the PTMs backed-up as a map
     */
    public HashMap<String, PTM> getBackedUpPtmsMap() {
        return backUp;
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
     * Removes a variable modification from the list of refinement
     * modifications.
     *
     * @param modificationName the name of the modification
     */
    public void removeRefinementVariableModification(String modificationName) {
        while (refinementVariableModifications.contains(modificationName)) {
            refinementVariableModifications.remove(modificationName);
        }
    }

    /**
     * Removes a fixed modification from the list of refinement modifications.
     *
     * @param modificationName the name of the modification
     */
    public void removeRefinementFixedModification(String modificationName) {
        while (refinementFixedModifications.contains(modificationName)) {
            refinementFixedModifications.remove(modificationName);
        }
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
                || (refinementVariableModifications != null && refinementVariableModifications.contains(modificationName))
                || (refinementFixedModifications != null && refinementFixedModifications.contains(modificationName));
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
        if (!this.getRefinementVariableModifications().equals(otherProfile.getRefinementVariableModifications())) {
            return false;
        }
        if (!this.getRefinementFixedModifications().equals(otherProfile.getRefinementFixedModifications())) {
            return false;
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
}
