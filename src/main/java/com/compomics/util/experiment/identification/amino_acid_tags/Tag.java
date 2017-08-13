package com.compomics.util.experiment.identification.amino_acid_tags;

import com.compomics.util.db.object.ObjectsDB;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.biology.MassGap;
import com.compomics.util.experiment.biology.modifications.ModificationType;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import no.uib.jsparklines.renderers.util.Util;

/**
 * This class represents a sequence mass tag.
 *
 * @author Marc Vaudel
 */
public class Tag extends ExperimentObject {

    /**
     * Serial number for backward compatibility.
     */
    static final long serialVersionUID = 1625541843008045218L;
    /**
     * The content of the tag.
     */
    private ArrayList<TagComponent> content = new ArrayList<>();

    /**
     * Constructor for an empty tag.
     */
    public Tag() {

    }

    /**
     * Creates a new tag instance based on the given one.
     *
     * @param tag the reference tag
     */
    public Tag(Tag tag) {
        for (TagComponent tagComponent : tag.getContent()) {
            if (tagComponent instanceof MassGap) {
                MassGap massGap = (MassGap) tagComponent;
                addMassGap(massGap.getMass());
            } else if (tagComponent instanceof AminoAcidPattern) {
                AminoAcidPattern aminoAcidPattern = new AminoAcidPattern((AminoAcidPattern) tagComponent);
                addAminoAcidPattern(aminoAcidPattern);
            } else if (tagComponent instanceof AminoAcidSequence) {
                AminoAcidSequence aminoAcidSequence = new AminoAcidSequence((AminoAcidSequence) tagComponent);
                addAminoAcidSequence(aminoAcidSequence);
            } else {
                throw new UnsupportedOperationException("Tag constructor not implemeted for tag component " + tagComponent.getClass() + ".");
            }
        }
    }

    /**
     * Constructor for a tag consisting of a pattern tag between two mass tags.
     *
     * @param nTermGap the N-term mass gap
     * @param sequenceTag the sequence tag with modifications
     * @param cTermGap the C-term mass gap
     */
    public Tag(double nTermGap, AminoAcidPattern sequenceTag, double cTermGap) {
        addMassGap(nTermGap);
        addAminoAcidPattern(sequenceTag);
        addMassGap(cTermGap);
    }

    /**
     * Constructor for a tag consisting of a sequence tag between two mass tags.
     *
     * @param nTermGap the N-term mass gap
     * @param sequenceTag the sequence tag with modifications
     * @param cTermGap the C-term mass gap
     */
    public Tag(double nTermGap, AminoAcidSequence sequenceTag, double cTermGap) {
        addMassGap(nTermGap);
        addAminoAcidSequence(sequenceTag);
        addMassGap(cTermGap);
    }

    /**
     * Returns the content of this tag as a list.
     *
     * @return the content of this tag as a list
     */
    public ArrayList<TagComponent> getContent() {
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        return content;
    }
    
    public void setContent(ArrayList<TagComponent> content){
        ObjectsDB.increaseRWCounter(); zooActivateWrite(); ObjectsDB.decreaseRWCounter();
        this.content = content;
    }

    /**
     * Adds a mass gap to the tag.
     *
     * @param massGap the value of the mass gap
     */
    public void addMassGap(double massGap) {
        ObjectsDB.increaseRWCounter(); zooActivateWrite(); ObjectsDB.decreaseRWCounter();
        if (massGap != 0) {
            content.add(new MassGap(massGap));
        }
    }

    /**
     * Adds a sequence of amino acids to the tag.
     *
     * @param aminoAcidPattern the amino acid sequence with modifications
     */
    public void addAminoAcidPattern(AminoAcidPattern aminoAcidPattern) {
        ObjectsDB.increaseRWCounter(); zooActivateWrite(); ObjectsDB.decreaseRWCounter();
        if (aminoAcidPattern.length() > 0) {
            if (!content.isEmpty()) {
                TagComponent lastComponent = content.get(content.size() - 1);
                if (lastComponent instanceof AminoAcidPattern) {
                    AminoAcidPattern pattern = (AminoAcidPattern) lastComponent;
                    pattern.append(aminoAcidPattern);
                    return;
                }
            }
            content.add(aminoAcidPattern);
        }
    }

    /**
     * Adds a sequence of amino acids to the tag.
     *
     * @param aminoAcidSequence the amino acid sequence with modifications
     */
    public void addAminoAcidSequence(AminoAcidSequence aminoAcidSequence) {
        ObjectsDB.increaseRWCounter(); zooActivateWrite(); ObjectsDB.decreaseRWCounter();
        if (aminoAcidSequence.length() > 0) {
            if (!content.isEmpty()) {
                TagComponent lastComponent = content.get(content.size() - 1);
                if (lastComponent instanceof AminoAcidSequence) {
                    AminoAcidSequence sequence = (AminoAcidSequence) lastComponent;
                    sequence.appendCTerm(aminoAcidSequence);
                    return;
                }
            }
            content.add(aminoAcidSequence);
        }
    }

    /**
     * Returns the tag as intelligible sequence of tag components. For example
     * amino acid tags and mass gaps: &lt;115.2&gt;TAG&lt;110.5&gt;.
     *
     * @return The tag as intelligible sequence for display.
     */
    public String asSequence() {
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        StringBuilder result = new StringBuilder(content.size() * 4);
        for (TagComponent tagComponent : content) {
            result.append(tagComponent.asSequence());
        }
        return result.toString();
    }

    /**
     * Returns the longest amino acid sequence contained in this tag.
     *
     * @return the longest amino acid sequence contained in this tag
     */
    public String getLongestAminoAcidSequence() {
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        String result = "";
        AminoAcidPattern lastAminoAcidPattern = null;
        AminoAcidSequence lastAminoAcidSequence = null;
        for (TagComponent tagComponent : content) {
            if (tagComponent instanceof MassGap) {
                if (lastAminoAcidPattern != null && lastAminoAcidPattern.length() > result.length()) {
                    result = lastAminoAcidPattern.asSequence();
                }
                lastAminoAcidPattern = null;
                if (lastAminoAcidSequence != null && lastAminoAcidSequence.length() > result.length()) {
                    result = lastAminoAcidSequence.asSequence();
                }
                lastAminoAcidSequence = null;
            } else if (tagComponent instanceof AminoAcidPattern) {
                AminoAcidPattern currentPattern = (AminoAcidPattern) tagComponent;
                if (lastAminoAcidPattern == null) {
                    if (lastAminoAcidSequence == null) {
                        lastAminoAcidPattern = currentPattern;
                    } else {
                        lastAminoAcidPattern = new AminoAcidPattern(lastAminoAcidSequence);
                        lastAminoAcidPattern.append(currentPattern);
                        lastAminoAcidSequence = null;
                    }
                } else {
                    lastAminoAcidPattern.append(currentPattern);
                }
            } else if (tagComponent instanceof AminoAcidSequence) {
                AminoAcidSequence currentSequence = (AminoAcidSequence) tagComponent;
                if (lastAminoAcidSequence == null) {
                    if (lastAminoAcidPattern == null) {
                        lastAminoAcidSequence = currentSequence;
                    } else {
                        lastAminoAcidPattern.append(new AminoAcidPattern(currentSequence));
                    }
                } else {
                    lastAminoAcidSequence.appendCTerm(currentSequence);
                }
            } else {
                throw new UnsupportedOperationException("Longest amino acid sequence not implemented for tag component " + tagComponent.getClass() + ".");
            }
        }
        if (lastAminoAcidPattern != null && lastAminoAcidPattern.length() > result.length()) {
            result = lastAminoAcidPattern.asSequence();
        }
        if (lastAminoAcidSequence != null && lastAminoAcidSequence.length() > result.length()) {
            result = lastAminoAcidSequence.asSequence();
        }
        return result;
    }

    /**
     * Returns the mass of the tag.
     *
     * @return the mass of the tag
     */
    public double getMass() {
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        double mass = Atom.H.getMonoisotopicMass();
        for (TagComponent tagComponent : content) {
            mass += tagComponent.getMass();
        }
        mass += Atom.H.getMonoisotopicMass() + Atom.O.getMonoisotopicMass();
        return mass;
    }

    /**
     * Returns the theoretic mass of the tag, eventually without terminal gaps.
     *
     * @param includeCTermGap if true the C-terminal gap will be added if
     * present
     * @param includeNTermGap if true the N-terminal gap will be added if
     * present
     *
     * @return the theoretic mass of the tag
     */
    public double getMass(boolean includeCTermGap, boolean includeNTermGap) {
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        double mass = getMass();
        if (!includeCTermGap) {
            mass -= getCTerminalGap();
        }
        if (!includeNTermGap) {
            mass -= getNTerminalGap();
        }
        return mass;
    }

    /**
     * Returns the N-terminal gap of the tag.
     *
     * @return the N-terminal gap of the tag
     */
    public double getNTerminalGap() {
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        if (content.isEmpty()) {
            return 0;
        }
        TagComponent tagComponent = content.get(0);
        if (tagComponent instanceof MassGap) {
            return tagComponent.getMass();
        } else {
            return 0;
        }
    }

    /**
     * Returns the C-terminal gap of the tag.
     *
     * @return the C-terminal gap of the tag
     */
    public double getCTerminalGap() {
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        if (content.isEmpty()) {
            return 0;
        }
        TagComponent tagComponent = content.get(content.size() - 1);
        if (tagComponent instanceof MassGap) {
            return tagComponent.getMass();
        } else {
            return 0;
        }
    }

    /**
     * Returns the modified sequence as an tagged string with potential
     * modification sites color coded or with PTM tags, e.g, &lt;mox&gt;. /!\
     * this method will work only if the PTM found in the peptide are in the
     * PTMFactory. /!\ This method uses the modifications as set in the
     * modification matches of this peptide and displays all of them.
     *
     * @param modificationProfile the modification profile of the search
     * @param useHtmlColorCoding if true, color coded HTML is used, otherwise
     * PTM tags, e.g, &lt;mox&gt;, are used
     * @param includeHtmlStartEndTags if true, start and end HTML tags are added
     * @param useShortName if true. the short names are used in the tags
     * @param includeTerminalGaps if true. the terminal gaps will be displayed on
     * the sequence
     * @param excludeAllFixedModifications if true. fixed modifications will not
     * be displayed on the sequence
     *
     * @return the modified sequence as a tagged string
     */
    public String getTaggedModifiedSequence(PtmSettings modificationProfile, boolean useHtmlColorCoding, boolean includeHtmlStartEndTags, boolean useShortName, boolean excludeAllFixedModifications, boolean includeTerminalGaps) {
        
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        return getTaggedModifiedSequence(modificationProfile, this, useHtmlColorCoding, includeHtmlStartEndTags, useShortName, excludeAllFixedModifications, includeTerminalGaps);
    }

    /**
     * Returns the modified sequence as an tagged string with potential
     * modification sites color coded or with PTM tags, e.g, &lt;mox&gt;. /!\
     * this method will work only if the PTM found in the peptide are in the
     * PTMFactory. /!\ This method uses the modifications as set in the
     * modification matches of this peptide and displays all of them.
     *
     * @param modificationProfile the modification profile of the search
     * @param useHtmlColorCoding if true, color coded HTML is used, otherwise
     * PTM tags, e.g, &lt;mox&gt;, are used
     * @param includeHtmlStartEndTags if true, start and end HTML tags are added
     * @param useShortName if true, the short names are used in the tags
     * @param includeTerminalGaps if true, the terminal gaps will be displayed on
     * the sequence
     *
     * @return the modified sequence as a tagged string
     */
    public String getTaggedModifiedSequence(PtmSettings modificationProfile, boolean useHtmlColorCoding, boolean includeHtmlStartEndTags, boolean useShortName, boolean includeTerminalGaps) {
        
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        return getTaggedModifiedSequence(modificationProfile, this, useHtmlColorCoding, includeHtmlStartEndTags, useShortName, false, includeTerminalGaps);
    }

    /**
     * Returns the modified sequence as an tagged string with potential
     * modification sites color coded or with PTM tags, e.g, &lt;mox&gt;. /!\
     * This method will work only if the PTM found in the peptide are in the
     * PTMFactory. /!\ This method uses the modifications as set in the
     * modification matches of this peptide and displays all of them.
     *
     * @param modificationProfile the modification profile of the search
     * @param tag the tag
     * @param includeHtmlStartEndTags if true, start and end HTML tags are added
     * @param useHtmlColorCoding if true, color coded HTML is used, otherwise
     * PTM tags, e.g, &lt;mox&gt;, are used
     * @param useShortName if true, the short names are used in the tags
     * @return the tagged modified sequence as a string
     * @param excludeAllFixedPtms if true, the fixed PTMs will not be displayed
     * @param includeTerminalGaps if true, the terminal gaps will be displayed on
     * the sequence
     */
    public static String getTaggedModifiedSequence(PtmSettings modificationProfile, Tag tag,
            boolean useHtmlColorCoding, boolean includeHtmlStartEndTags, boolean useShortName, boolean excludeAllFixedPtms, boolean includeTerminalGaps) {

        String modifiedSequence = "";

        if (useHtmlColorCoding && includeHtmlStartEndTags) {
            modifiedSequence += "<html>";
        }

        modifiedSequence += tag.getNTerminal(includeTerminalGaps);

        for (int i = 0; i < tag.getContent().size(); i++) {
            TagComponent tagComponent = tag.getContent().get(i);
            if (tagComponent instanceof AminoAcidPattern) {
                AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) tagComponent;
                modifiedSequence += aminoAcidPattern.getTaggedModifiedSequence(modificationProfile, useHtmlColorCoding, useShortName, excludeAllFixedPtms);
            } else if (tagComponent instanceof AminoAcidSequence) {
                AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) tagComponent;
                modifiedSequence += aminoAcidSequence.getTaggedModifiedSequence(modificationProfile, useHtmlColorCoding, useShortName, excludeAllFixedPtms);
            } else if (tagComponent instanceof MassGap) {
                if (includeTerminalGaps || i > 0 && i < tag.getContent().size() - 1) {
                    modifiedSequence += tagComponent.asSequence();
                }
            } else {
                throw new UnsupportedOperationException("Tagged sequence not implemented for tag component " + tagComponent.getClass() + ".");
            }
        }

        modifiedSequence += tag.getCTerminal(includeTerminalGaps);

        if (useHtmlColorCoding && includeHtmlStartEndTags) {
            modifiedSequence += "</html>";
        }

        return modifiedSequence;
    }

    /**
     * Returns the N-terminal tag of this tag as a string for sequence display.
     *
     * @param includeTerminalGaps indicates whether mass gaps shall be included
     *
     * @return the N-terminal tag of this tag
     */
    public String getNTerminal(boolean includeTerminalGaps) {
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        if (content.isEmpty()) {
            return "";
        }
        TagComponent firstComponent = content.get(0);
        if (firstComponent instanceof AminoAcidPattern) {
            AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) firstComponent;
            String nTerm = "NH2";
            ModificationFactory ptmFactory = ModificationFactory.getInstance();
            for (ModificationMatch modificationMatch : aminoAcidPattern.getModificationsAt(1)) {
                Modification ptm = ptmFactory.getModification(modificationMatch.getModification());
                if (ptm.getModificationType() != ModificationType.modaa) {
                    nTerm = ptm.getShortName();
                }
            }
            nTerm = nTerm.replaceAll("-", " ");
            return nTerm + "-";
        } else if (firstComponent instanceof AminoAcidSequence) {
            AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) firstComponent;
            String nTerm = "NH2";
            ModificationFactory ptmFactory = ModificationFactory.getInstance();
            for (ModificationMatch modificationMatch : aminoAcidSequence.getModificationsAt(1)) {
                Modification ptm = ptmFactory.getModification(modificationMatch.getModification());
                if (ptm.getModificationType() != ModificationType.modaa) {
                    nTerm = ptm.getShortName();
                }
            }
            nTerm = nTerm.replaceAll("-", " ");
            return nTerm + "-";
        } else if (firstComponent instanceof MassGap) {
            if (includeTerminalGaps) {
                return firstComponent.asSequence();
            } else {
                return Util.roundDouble(firstComponent.getMass(), 2) + "-";
            }
        } else {
            throw new UnsupportedOperationException("N-terminal tag not implemented for tag component " + firstComponent.getClass() + ".");
        }
    }

    /**
     * Returns the C-terminal tag of this tag as a string for sequence display.
     *
     * @param includeTerminalGaps indicates whether mass gaps shall be included
     *
     * @return the C-terminal tag of this tag
     */
    public String getCTerminal(boolean includeTerminalGaps) {
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        if (content.isEmpty()) {
            return "";
        }
        TagComponent lastComponent = content.get(content.size() - 1);
        if (lastComponent instanceof AminoAcidPattern) {
            AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) lastComponent;
            String cTerm = "COOH";
            ModificationFactory ptmFactory = ModificationFactory.getInstance();
            for (ModificationMatch modificationMatch : aminoAcidPattern.getModificationsAt(aminoAcidPattern.length())) {
                Modification ptm = ptmFactory.getModification(modificationMatch.getModification());
                if (ptm.getModificationType() != ModificationType.modaa) {
                    cTerm = ptm.getShortName();
                }
            }
            cTerm = cTerm.replaceAll("-", " ");
            return "-" + cTerm;
        } else if (lastComponent instanceof AminoAcidSequence) {
            AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) lastComponent;
            String cTerm = "COOH";
            ModificationFactory ptmFactory = ModificationFactory.getInstance();
            for (ModificationMatch modificationMatch : aminoAcidSequence.getModificationsAt(aminoAcidSequence.length())) {
                Modification ptm = ptmFactory.getModification(modificationMatch.getModification());
                if (ptm.getModificationType() != ModificationType.modaa) {
                    cTerm = ptm.getShortName();
                }
            }
            cTerm = cTerm.replaceAll("-", " ");
            return "-" + cTerm;
        } else if (lastComponent instanceof MassGap) {
            if (includeTerminalGaps) {
                return lastComponent.asSequence();
            } else {
                return "-" + Util.roundDouble(lastComponent.getMass(), 2);
            }
        } else {
            throw new UnsupportedOperationException("C-terminal tag not implemented for tag component " + lastComponent.getClass() + ".");
        }
    }

    /**
     * Returns the amino acid length of the tag when mass gaps are considered
     * like one amino acid
     *
     * @return the amino acid length of the tag
     */
    public int getLengthInAminoAcid() {
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        int length = 0;
        for (TagComponent tagComponent : content) {
            if (tagComponent instanceof AminoAcidPattern) {
                AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) tagComponent;
                length += aminoAcidPattern.length();
            } else if (tagComponent instanceof AminoAcidSequence) {
                AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) tagComponent;
                length += aminoAcidSequence.length();
            } else if (tagComponent instanceof MassGap) {
                length++;
            } else {
                throw new UnsupportedOperationException("Tag length in amino acid not implemented for tag component " + tagComponent.getClass() + ".");
            }
        }
        return length;
    }

    /**
     * Returns the potential modification sites as an ordered list of string. 1
     * is the first amino acid. An empty list is returned if no possibility was
     * found. This method does not account for protein terminal modifications.
     *
     * @param ptm the PTM considered
     * @param ptmSequenceMatchingPreferences the sequence matching preferences
     * for the PTM to amino acid sequence mapping
     *
     * @return a list of potential modification sites
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading a protein sequence
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while reading a protein sequence
     * @throws InterruptedException exception thrown whenever an error occurred
     * while reading a protein sequence
     * @throws FileNotFoundException if a FileNotFoundException occurs
     * @throws ClassNotFoundException if a ClassNotFoundException occurs
     */
    public ArrayList<Integer> getPotentialModificationSites(Modification ptm, SequenceMatchingPreferences ptmSequenceMatchingPreferences)
            throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException {

        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        ArrayList<Integer> possibleSites = new ArrayList<>();
        AminoAcidPattern ptmPattern = ptm.getPattern(); 
        int patternLength = ptmPattern.length(); // @TODO: what if pattern is null..?

        switch (ptm.getModificationType()) {
            case modaa:
                int offset = 0;
                for (TagComponent tagComponent : content) {
                    if (tagComponent instanceof AminoAcidPattern) {
                        AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) tagComponent;
                        for (int i : ptmPattern.getIndexes(aminoAcidPattern, ptmSequenceMatchingPreferences)) {
                            possibleSites.add(i + offset);
                        }
                        offset += aminoAcidPattern.length();
                    } else if (tagComponent instanceof AminoAcidSequence) {
                        AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) tagComponent;
                        for (int i : ptmPattern.getIndexes(aminoAcidSequence.getSequence(), ptmSequenceMatchingPreferences)) {
                            possibleSites.add(i + offset);
                        }
                        offset += aminoAcidSequence.length();
                    } else {
                        offset++;
                    }
                }
                return possibleSites;
            case modc_protein:
            case modc_peptide:
                possibleSites.add(patternLength);
                return possibleSites;
            case modn_peptide:
            case modn_protein:
                possibleSites.add(1);
                return possibleSites;
            case modcaa_peptide:
            case modcaa_protein:
                if (content.isEmpty()) {
                    return new ArrayList<>();
                }
                TagComponent component = content.get(content.size() - 1);
                if (component instanceof AminoAcidPattern) {
                    AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) component;
                    if (ptmPattern.isEnding(aminoAcidPattern, ptmSequenceMatchingPreferences)) {
                        possibleSites.add(patternLength);
                    }
                } else if (component instanceof AminoAcidSequence) {
                    AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) component;
                    if (ptmPattern.isEnding(aminoAcidSequence.getSequence(), ptmSequenceMatchingPreferences)) {
                        possibleSites.add(patternLength);
                    }
                } else if (component instanceof MassGap) {
                    possibleSites.add(patternLength);
                } else {
                    throw new UnsupportedOperationException("Possible modifications not implemnted for tag component " + component.getClass() + ".");
                }
                return possibleSites;
            case modnaa_peptide:
            case modnaa_protein:
                if (content.isEmpty()) {
                    return new ArrayList<>();
                }
                component = content.get(0);
                if (component instanceof AminoAcidPattern) {
                    AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) component;
                    if (ptmPattern.isStarting(aminoAcidPattern, ptmSequenceMatchingPreferences)) {
                        possibleSites.add(patternLength);
                    }
                } else if (component instanceof AminoAcidSequence) {
                    AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) component;
                    if (ptmPattern.isStarting(aminoAcidSequence.getSequence(), ptmSequenceMatchingPreferences)) {
                        possibleSites.add(patternLength);
                    }
                } else if (component instanceof MassGap) {
                    possibleSites.add(patternLength);
                } else {
                    throw new UnsupportedOperationException("Possible modifications not implemnted for tag component " + component.getClass() + ".");
                }
                return possibleSites;
            default:
                throw new IllegalArgumentException("PTM type " + ptm.getModificationType() + " not recognized.");
        }
    }

    /**
     * Indicates whether this tag is the same as another tag. Note: this method
     * accounts for modification localization.
     *
     * @param anotherTag another tag
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return a boolean indicating whether the tag is the same as another
     */
    public boolean isSameAs(Tag anotherTag, SequenceMatchingPreferences sequenceMatchingPreferences) {
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        if (content.size() != anotherTag.getContent().size()) {
            return false;
        }
        for (int i = 0; i < content.size(); i++) {
            TagComponent component1 = content.get(i);
            TagComponent component2 = anotherTag.getContent().get(i);
            if (!component1.isSameAs(component2, sequenceMatchingPreferences)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Indicates whether this tag is the same as another tag without accounting
     * for modification localization.
     *
     * @param anotherTag another tag
     * @param sequenceMatchingPreferences the sequence matching preferences
     *
     * @return a boolean indicating whether the tag is the same as another
     */
    public boolean isSameSequenceAndModificationStatusAs(Tag anotherTag, SequenceMatchingPreferences sequenceMatchingPreferences) {
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        if (content.size() != anotherTag.getContent().size()) {
            return false;
        }
        for (int i = 0; i < content.size(); i++) {
            TagComponent component1 = content.get(i);
            TagComponent component2 = anotherTag.getContent().get(i);
            if (!component1.isSameSequenceAndModificationStatusAs(component2, sequenceMatchingPreferences)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the tag modifications as a string.
     *
     * @param tag the tag
     * @return the peptide modifications as a string
     */
    public static String getTagModificationsAsString(Tag tag) {
        HashMap<String, ArrayList<Integer>> modMap = new HashMap<>();
        int offset = 0;
        for (TagComponent tagComponent : tag.getContent()) {
            if (tagComponent instanceof MassGap) {
                offset++;
            } else if (tagComponent instanceof AminoAcidPattern) {
                AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) tagComponent;
                for (int i = 1; i <= aminoAcidPattern.length(); i++) {
                    for (ModificationMatch modificationMatch : aminoAcidPattern.getModificationsAt(i)) {
                        if (modificationMatch.getVariable()) {
                            if (!modMap.containsKey(modificationMatch.getModification())) {
                                modMap.put(modificationMatch.getModification(), new ArrayList<>());
                            }
                            modMap.get(modificationMatch.getModification()).add(i + offset);
                        }
                    }
                }
                offset += aminoAcidPattern.length();
            } else if (tagComponent instanceof AminoAcidSequence) {
                AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) tagComponent;
                for (int i = 1; i <= aminoAcidSequence.length(); i++) {
                    for (ModificationMatch modificationMatch : aminoAcidSequence.getModificationsAt(i)) {
                        if (modificationMatch.getVariable()) {
                            if (!modMap.containsKey(modificationMatch.getModification())) {
                                modMap.put(modificationMatch.getModification(), new ArrayList<>());
                            }
                            modMap.get(modificationMatch.getModification()).add(i + offset);
                        }
                    }
                }
                offset += aminoAcidSequence.length();
            } else {
                throw new IllegalArgumentException("Modification summary not implemented for TagComponent " + tagComponent.getClass() + ".");
            }
        }

        StringBuilder result = new StringBuilder();
        boolean first = true, first2;
        ArrayList<String> mods = new ArrayList<>(modMap.keySet());
        Collections.sort(mods);
        for (String mod : mods) {
            if (first) {
                first = false;
            } else {
                result.append(", ");
            }
            first2 = true;
            result.append(mod);
            result.append(" (");
            for (int aa : modMap.get(mod)) {
                if (first2) {
                    first2 = false;
                } else {
                    result.append(", ");
                }
                result.append(aa);
            }
            result.append(")");
        }

        return result.toString();
    }

    /**
     * Returns a new tag instance which is a reversed version of the current
     * tag.
     *
     * @param yIon indicates whether the tag is based on y ions
     *
     * @return a new tag instance which is a reversed version of the current tag
     */
    public Tag reverse(boolean yIon) {
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        double water = 2 * Atom.H.getMonoisotopicMass() + Atom.O.getMonoisotopicMass();
        Tag newTag = new Tag();
        for (int i = content.size() - 1; i >= 0; i--) {
            TagComponent tagComponent = content.get(i);
            if (tagComponent instanceof MassGap) {
                double mass = tagComponent.getMass();
                if (i == content.size() - 1) {
                    if (yIon) {
                        mass += water;
                    } else {
                        mass -= water;
                    }
                } else if (i == 0) {
                    if (yIon) {
                        mass -= water;
                    } else {
                        mass += water;
                    }
                }
                newTag.addMassGap(mass);
            } else if (tagComponent instanceof AminoAcidPattern) {
                newTag.addAminoAcidPattern(((AminoAcidPattern) tagComponent).reverse());
            } else if (tagComponent instanceof AminoAcidSequence) {
                newTag.addAminoAcidSequence(((AminoAcidSequence) tagComponent).reverse());
            } else {
                throw new UnsupportedOperationException("Reverse method not implemented for tag component " + tagComponent.getClass() + ".");
            }
        }
        return newTag;
    }

    /**
     * Indicates whether the tag can be reversed (ie if termini are mass gaps
     * with mass &ge; water).
     *
     * @return whether the tag can be reversed
     */
    public boolean canReverse() {
        ObjectsDB.increaseRWCounter(); zooActivateRead(); ObjectsDB.decreaseRWCounter();
        double water = 2 * Atom.H.getMonoisotopicMass() + Atom.O.getMonoisotopicMass();
        TagComponent terminalComponent = content.get(0);
        if (terminalComponent instanceof MassGap) {
            MassGap terminalGap = (MassGap) terminalComponent;
            if (terminalGap.getMass() >= water) {
                terminalComponent = content.get(content.size() - 1);
                if (terminalComponent instanceof MassGap) {
                    terminalGap = (MassGap) terminalComponent;
                    if (terminalGap.getMass() >= water) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return asSequence();
    }
}
