package com.compomics.util.experiment.identification.tags;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.matches.ProteinMatch;
import com.compomics.util.experiment.identification.tags.tagcomponents.MassGap;
import com.compomics.util.preferences.ModificationProfile;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This class represents a sequence mass tag.
 *
 * @author Marc Vaudel
 */
public class Tag {

    /**
     * The content of the tag.
     */
    private ArrayList<TagComponent> content = new ArrayList<TagComponent>();

    /**
     * Constructor for an empty tag.
     */
    public Tag() {

    }

    /**
     * Constructor for a tag consisting of a sequence tag between two mass tags.
     *
     * @param nTermGap the N-term mass gap
     * @param sequenceTag the sequence tag with modifications
     * @param cTermGap the C-term mass gap
     */
    public Tag(double nTermGap, AminoAcidPattern sequenceTag, double cTermGap) {
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
        return content;
    }

    /**
     * Adds a mass gap to the tag.
     *
     * @param massGap the value of the mass gap
     */
    public void addMassGap(double massGap) {
        if (massGap != 0) {
            content.add(new MassGap(massGap));
        }
    }

    /**
     * Adds a sequence of amino acids to the tag.
     *
     * @param aminoAcidSequence the amino acid sequence with modifications
     */
    public void addAminoAcidSequence(AminoAcidPattern aminoAcidSequence) {
        if (aminoAcidSequence.length() > 0) {
            if (!content.isEmpty()) {
                TagComponent lastComponent = content.get(content.size() - 1);
                if (lastComponent instanceof AminoAcidPattern) {
                    AminoAcidPattern pattern = (AminoAcidPattern) lastComponent;
                    pattern.append(aminoAcidSequence);
                    return;
                }
            }
            content.add(aminoAcidSequence);
        }
    }

    /**
     * Returns the tag as intelligible sequence of tag components. For example
     * amino acid tags and mass gaps: &lt;115.2&gt;TAG&lt;110.5&gt.
     *
     * @return The tag as intelligible sequence for display.
     */
    public String asSequence() {
        String result = "";
        for (TagComponent tagComponent : content) {
            result += tagComponent.asSequence();
        }
        return result;
    }

    /**
     * Returns the longest amino acid sequence contained in this tag.
     *
     * @return the longest amino acid sequence contained in this tag
     */
    public String getLongestAminoAcidSequence() {
        String result = "";
        AminoAcidPattern lastAminoAcidPattern = null;
        for (TagComponent tagComponent : content) {
            if (tagComponent instanceof MassGap) {
                if (lastAminoAcidPattern != null && lastAminoAcidPattern.length() > result.length()) {
                    result = lastAminoAcidPattern.asSequence();
                }
                lastAminoAcidPattern = null;
            } else if (tagComponent instanceof AminoAcidPattern) {
                AminoAcidPattern currentPattern = (AminoAcidPattern) tagComponent;
                if (lastAminoAcidPattern == null) {
                    lastAminoAcidPattern = currentPattern;
                } else {
                    lastAminoAcidPattern.append(currentPattern);
                }
            }
        }
        if (lastAminoAcidPattern != null && lastAminoAcidPattern.length() > result.length()) {
            result = lastAminoAcidPattern.asSequence();
        }
        return result;
    }

    /**
     * Returns the mass of the tag.
     *
     * @return the mass of the tag
     */
    public double getMass() {
        double mass = 0;
        for (TagComponent tagComponent : content) {
            mass += tagComponent.getMass();
        }
        return mass;
    }
    
    /**
     * Returns the theoretic mass of the tag, eventually without terminal gaps.
     * 
     * @param includeCTermGap if true the C-terminal gap will be added if present
     * @param includeNTermGap if true the N-terminal gap will be added if present
     * 
     * @return the theoretic mass of the tag
     */
    public double getMass(boolean includeCTermGap, boolean includeNTermGap) {
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
     * @param useShortName if true the short names are used in the tags
     * @param includeTerminalGaps if true the terminal gaps will be displayed on
     * the sequence
     * @param excludeAllFixedModifications if true fixed modifications will not
     * be displayed on the sequence
     *
     * @return the modified sequence as a tagged string
     */
    public String getTaggedModifiedSequence(ModificationProfile modificationProfile, boolean useHtmlColorCoding, boolean includeHtmlStartEndTags, boolean useShortName, boolean excludeAllFixedModifications, boolean includeTerminalGaps) {
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
     * @param useShortName if true the short names are used in the tags
     * @param includeTerminalGaps if true the terminal gaps will be displayed on
     * the sequence
     *
     * @return the modified sequence as a tagged string
     */
    public String getTaggedModifiedSequence(ModificationProfile modificationProfile, boolean useHtmlColorCoding, boolean includeHtmlStartEndTags, boolean useShortName, boolean includeTerminalGaps) {
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
     * @param useShortName if true the short names are used in the tags
     * @return the tagged modified sequence as a string
     * @param excludeAllFixedPtms if true the fixed ptms will not be displayed
     * @param includeTerminalGaps if true the terminal gaps will be displayed on
     * the sequence
     */
    public static String getTaggedModifiedSequence(ModificationProfile modificationProfile, Tag tag,
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
            } else {
                if (includeTerminalGaps || i > 0 && i < tag.getContent().size() - 1) {
                    modifiedSequence += tagComponent.asSequence();
                }
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
        if (content.isEmpty()) {
            return "";
        }
        TagComponent firstComponent = content.get(0);
        if (firstComponent instanceof AminoAcidPattern) {
            AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) firstComponent;
            String nTerm = "NH2";
            PTMFactory ptmFactory = PTMFactory.getInstance();
            for (ModificationMatch modificationMatch : aminoAcidPattern.getModificationsAt(1)) {
                PTM ptm = ptmFactory.getPTM(modificationMatch.getTheoreticPtm());
                if (ptm.getType() != PTM.MODAA && ptm.getType() != PTM.MODMAX) {
                    nTerm = ptmFactory.getShortName(modificationMatch.getTheoreticPtm());
                }
            }
            nTerm = nTerm.replaceAll("-", " ");
            return nTerm + "-";
        } else if (firstComponent instanceof MassGap) {
            if (includeTerminalGaps) {
                return firstComponent.asSequence();
            } else {
                return "...";
            }
        } else {
            throw new IllegalArgumentException("Terminal tag not implemented for TagComponent " + firstComponent.getClass() + ".");
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
        if (content.isEmpty()) {
            return "";
        }
        TagComponent lastComponent = content.get(content.size() - 1);
        if (lastComponent instanceof AminoAcidPattern) {
            AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) lastComponent;
            String cTerm = "COOH";
            PTMFactory ptmFactory = PTMFactory.getInstance();
            for (ModificationMatch modificationMatch : aminoAcidPattern.getModificationsAt(1)) {
                PTM ptm = ptmFactory.getPTM(modificationMatch.getTheoreticPtm());
                if (ptm.getType() != PTM.MODAA && ptm.getType() != PTM.MODMAX) {
                    cTerm = ptmFactory.getShortName(modificationMatch.getTheoreticPtm());
                }
            }
            cTerm = cTerm.replaceAll("-", " ");
            return "-" + cTerm;
        } else if (lastComponent instanceof MassGap) {
            if (includeTerminalGaps) {
                return lastComponent.asSequence();
            } else {
                return "...";
            }
        } else {
            throw new IllegalArgumentException("Terminal tag not implemented for TagComponent " + lastComponent.getClass() + ".");
        }
    }

    /**
     * Returns the amino acid length of the tag when mass gaps are considered
     * like one amino acid
     * 
     * @return the amino acid length of the tag
     */
    public int getLengthInAminoAcid() {
        int length = 0;
        for (TagComponent tagComponent : content) {
            if (tagComponent instanceof AminoAcidPattern) {
                AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) tagComponent;
                length += aminoAcidPattern.length();
            } else {
                length++;
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
     * @param matchingType the type of sequence matching
     * @param massTolerance the mass tolerance for matching type
     * 'indistiguishibleAminoAcids'. Can be null otherwise
     *
     * @return a list of potential modification sites
     *
     * @throws IOException exception thrown whenever an error occurred while
     * reading a protein sequence
     * @throws IllegalArgumentException exception thrown whenever an error
     * occurred while reading a protein sequence
     * @throws InterruptedException exception thrown whenever an error occurred
     * while reading a protein sequence
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     */
    public ArrayList<Integer> getPotentialModificationSites(PTM ptm, ProteinMatch.MatchingType matchingType, Double massTolerance)
            throws IOException, IllegalArgumentException, InterruptedException, FileNotFoundException, ClassNotFoundException {

        ArrayList<Integer> possibleSites = new ArrayList<Integer>();
        AminoAcidPattern ptmPattern = ptm.getPattern();
        int patternLength = ptmPattern.length();

        switch (ptm.getType()) {
            case PTM.MODAA:
                int offset = 0;
                for (TagComponent tagComponent : content) {
                    if (tagComponent instanceof AminoAcidPattern) {
                        AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) tagComponent;
                        for (int i : ptmPattern.getIndexes(aminoAcidPattern)) {
                            possibleSites.add(i + offset);
                        }
                        offset += aminoAcidPattern.length();
                    } else {
                        offset++;
                    }
                }
            case PTM.MODC:
            case PTM.MODCP:
                possibleSites.add(patternLength);
            case PTM.MODN:
            case PTM.MODNP:
                possibleSites.add(1);
            case PTM.MODCAA:
            case PTM.MODCPAA:
                if (content.isEmpty()) {
                    return new ArrayList<Integer>();
                }
                TagComponent component = content.get(content.size() - 1);
                if (component instanceof AminoAcidPattern) {
                    AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) component;
                    if (ptmPattern.isEnding(aminoAcidPattern)) {
                        possibleSites.add(patternLength);
                    }
                } else {
                    possibleSites.add(patternLength);
                }
            case PTM.MODNAA:
            case PTM.MODNPAA:
                if (content.isEmpty()) {
                    return new ArrayList<Integer>();
                }
                component = content.get(0);
                if (component instanceof AminoAcidPattern) {
                    AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) component;
                    if (ptmPattern.isStarting(aminoAcidPattern)) {
                        possibleSites.add(patternLength);
                    }
                } else {
                    possibleSites.add(patternLength);
                }
        }
        return possibleSites;
    }

    /**
     * Indicates whether this tag is the same as another tag.
     *
     * @param anotherTag another tag
     * @return a boolean indicating whether the tag is the same as another
     */
    public boolean isSameAs(Tag anotherTag) {
        if (content.size() != anotherTag.getContent().size()) {
            return false;
        }
        for (int i = 0; i < content.size(); i++) {
            TagComponent component1 = content.get(i);
            TagComponent component2 = anotherTag.getContent().get(i);
            if (component1.isSameAs(component2)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Returns the start and end indexes of the tag in the given sequence. Null if not found.
     * 
     * @TODO: implement PTMs
     * 
     * @param sequence the sequence where to look for the tag
     * @param tagIndex the index where the tag is located
     * @param componentIndex the index of the component of the tag indexed by tagIndex in the content list
     * @param matchingType the matching type to use
     * @param massTolerance the ms2 tolerance
     * 
     * @return the start and end indexes of the tag
     */
    public int[] matches(String sequence, int tagIndex, int componentIndex, ProteinMatch.MatchingType matchingType, Double massTolerance) {
        int[] result = new int[2];
        // Check tag components to the N-term
        int aaIndex = tagIndex;
        for (int i = componentIndex ; i >= 0 ; i--) {
            TagComponent tagComponent = content.get(i);
            if (tagComponent instanceof AminoAcidPattern) {
                AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) tagComponent;
                int startIndex = aaIndex - aminoAcidPattern.length();
                if (startIndex < 0) {
                    return null;
                }
                String subSequence = sequence.substring(startIndex, aaIndex);
                if (!aminoAcidPattern.matches(subSequence, matchingType, massTolerance)) {
                    return null;
                }
                aaIndex = startIndex;
            } else if (tagComponent instanceof MassGap) {
                boolean found = false;
                double mass = 0;
                while (--aaIndex >= 0 && mass <= tagComponent.getMass() + massTolerance) {
                    mass += AminoAcid.getAminoAcid(sequence.charAt(aaIndex)).monoisotopicMass;
                    if (Math.abs(mass-tagComponent.getMass()) <= massTolerance) {
                        found = true;
                    }
                }
                if (!found) {
                    return null;
                }
            } else {
                throw new IllegalArgumentException("Tag component " + tagComponent.getClass() + " not implemented for sequence mating.");
            }
        }
        result[0] = aaIndex;
        // check tag components to the C-term
        aaIndex = tagIndex;
        for (int i = componentIndex ; i < sequence.length() ; i++) {
            TagComponent tagComponent = content.get(i);
            if (tagComponent instanceof AminoAcidPattern) {
                AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) tagComponent;
                int endIndex = aaIndex - aminoAcidPattern.length();
                if (endIndex >= sequence.length()) {
                    return null;
                }
                String subSequence = sequence.substring(aaIndex, endIndex);
                if (!aminoAcidPattern.matches(subSequence, matchingType, massTolerance)) {
                    return null;
                }
                aaIndex = endIndex;
            } else if (tagComponent instanceof MassGap) {
                boolean found = false;
                double mass = 0;
                while (++aaIndex < sequence.length() && mass <= tagComponent.getMass() + massTolerance) {
                    mass += AminoAcid.getAminoAcid(sequence.charAt(aaIndex)).monoisotopicMass;
                    if (Math.abs(mass-tagComponent.getMass()) <= massTolerance) {
                        found = true;
                    }
                }
                if (!found) {
                    return null;
                }
            } else {
                throw new IllegalArgumentException("Tag component " + tagComponent.getClass() + " not implemented for sequence mating.");
            }
        }
        result[1] = aaIndex;
        return result;
    }
}
