package com.compomics.util.experiment.identification.tags;

import com.compomics.util.experiment.biology.AminoAcid;
import com.compomics.util.experiment.biology.AminoAcidPattern;
import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.Atom;
import com.compomics.util.experiment.biology.PTM;
import com.compomics.util.experiment.biology.PTMFactory;
import com.compomics.util.experiment.biology.Peptide;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.tags.tagcomponents.MassGap;
import com.compomics.util.experiment.personalization.ExperimentObject;
import com.compomics.util.preferences.ModificationProfile;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import no.uib.jsparklines.renderers.util.Util;
import org.tukaani.xz.UnsupportedOptionsException;

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
    private ArrayList<TagComponent> content = new ArrayList<TagComponent>();

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
     * @param aminoAcidPattern the amino acid sequence with modifications
     */
    public void addAminoAcidPattern(AminoAcidPattern aminoAcidPattern) {
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
        if (aminoAcidSequence.length() > 0) {
            if (!content.isEmpty()) {
                TagComponent lastComponent = content.get(content.size() - 1);
                if (lastComponent instanceof AminoAcidSequence) {
                    AminoAcidSequence sequence = (AminoAcidSequence) lastComponent;
                    sequence.append(aminoAcidSequence);
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
                    lastAminoAcidSequence.append(currentSequence);
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
            } else if (tagComponent instanceof AminoAcidSequence) {
                AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) tagComponent;
                modifiedSequence += aminoAcidSequence.getTaggedModifiedSequence(modificationProfile, useHtmlColorCoding, useShortName, excludeAllFixedPtms);
            } else if (tagComponent instanceof AminoAcidSequence) {
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
        } else if (firstComponent instanceof AminoAcidSequence) {
            AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) firstComponent;
            String nTerm = "NH2";
            PTMFactory ptmFactory = PTMFactory.getInstance();
            for (ModificationMatch modificationMatch : aminoAcidSequence.getModificationsAt(1)) {
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
        } else if (lastComponent instanceof AminoAcidSequence) {
            AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) lastComponent;
            String cTerm = "COOH";
            PTMFactory ptmFactory = PTMFactory.getInstance();
            for (ModificationMatch modificationMatch : aminoAcidSequence.getModificationsAt(1)) {
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
    public ArrayList<Integer> getPotentialModificationSites(PTM ptm, AminoAcidPattern.MatchingType matchingType, Double massTolerance)
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
                return possibleSites;
            case PTM.MODC:
            case PTM.MODCP:
                possibleSites.add(patternLength);
                return possibleSites;
            case PTM.MODN:
            case PTM.MODNP:
                possibleSites.add(1);
                return possibleSites;
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
                } else if (component instanceof AminoAcidSequence) {
                    AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) component;
                    if (ptmPattern.isEnding(aminoAcidSequence.getSequence())) {
                        possibleSites.add(patternLength);
                    }
                } else if (component instanceof MassGap) {
                    possibleSites.add(patternLength);
                } else {
                    throw new UnsupportedOperationException("Possible modifications not implemnted for tag component " + component.getClass() + ".");
                }
                return possibleSites;
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
                } else if (component instanceof AminoAcidSequence) {
                    AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) component;
                    if (ptmPattern.isStarting(aminoAcidSequence.getSequence())) {
                        possibleSites.add(patternLength);
                    }
                } else if (component instanceof MassGap) {
                    possibleSites.add(patternLength);
                } else {
                    throw new UnsupportedOperationException("Possible modifications not implemnted for tag component " + component.getClass() + ".");
                }
                return possibleSites;
            default:
                throw new IllegalArgumentException("PTM type " + ptm.getType() + " not recognized.");
        }
    }

    /**
     * Indicates whether this tag is the same as another tag. Note: this method
     * accounts for modification localization.
     *
     * @param anotherTag another tag
     * @param matchingType the amino acid matching type
     * @param massTolerance the mass tolerance to use to consider amino acids as
     * indistinguishable
     *
     * @return a boolean indicating whether the tag is the same as another
     */
    public boolean isSameAs(Tag anotherTag, AminoAcidPattern.MatchingType matchingType, Double massTolerance) {
        if (content.size() != anotherTag.getContent().size()) {
            return false;
        }
        for (int i = 0; i < content.size(); i++) {
            TagComponent component1 = content.get(i);
            TagComponent component2 = anotherTag.getContent().get(i);
            if (!component1.isSameAs(component2, matchingType, massTolerance)) {
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
     * @param matchingType the amino acid matching type
     * @param massTolerance the mass tolerance to use to consider amino acids as
     * indistinguishable
     *
     * @return a boolean indicating whether the tag is the same as another
     */
    public boolean isSameSequenceAndModificationStatusAs(Tag anotherTag, AminoAcidPattern.MatchingType matchingType, Double massTolerance) {
        if (content.size() != anotherTag.getContent().size()) {
            return false;
        }
        for (int i = 0; i < content.size(); i++) {
            TagComponent component1 = content.get(i);
            TagComponent component2 = anotherTag.getContent().get(i);
            if (!component1.isSameSequenceAndModificationStatusAs(component2, matchingType, massTolerance)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the possible peptides which can be created on this sequence
     * indexed by their start index. Null if not found. Note: PTMs must be in
     * the ptm factory. PTMs are considered at a target amino acid only, longer
     * patterns are not taken into account.
     *
     * @param sequence the sequence where to look for the tag
     * @param tagIndex the index where the tag is located
     * @param componentIndex the index of the component of the tag indexed by
     * tagIndex in the content list
     * @param matchingType the matching type to use
     * @param massTolerance the ms2 tolerance
     * @param fixedModifications the fixed modifications to consider
     * @param variableModifications the variable modifications to consider
     * @param reportFixedPtms a boolean indicating whether fixed PTMs should be
     * reported in the Peptide object
     *
     * @return the possible peptides which can be created on this sequence
     * indexed by their start index
     */
    public HashMap<Integer, ArrayList<Peptide>> getPeptideMatches(String sequence, int tagIndex, int componentIndex, AminoAcidPattern.MatchingType matchingType, Double massTolerance, ArrayList<String> fixedModifications, ArrayList<String> variableModifications, boolean reportFixedPtms) { // @TODO: implement PTMs

        HashMap<Integer, ArrayList<Peptide>> result = new HashMap<Integer, ArrayList<Peptide>>();
        double minMod = 0;
        for (String modificationName : variableModifications) {
            PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
            if (ptm.getMass() < minMod) {
                minMod = ptm.getMass();
            }
        }
        int sequenceLastIndex = sequence.length() - 1;

        // Check tag components to the N-term
        ArrayList<AminoAcidSequence> nTermPossibleSequences = new ArrayList<AminoAcidSequence>();
        ArrayList<Integer> nTermPossibleIndexes = new ArrayList<Integer>();
        nTermPossibleSequences.add(new AminoAcidSequence());
        nTermPossibleIndexes.add(tagIndex);
        for (int i = componentIndex - 1; i >= 0; i--) {
            TagComponent tagComponent = content.get(i);
            ArrayList<AminoAcidSequence> newSequences = new ArrayList<AminoAcidSequence>();
            ArrayList<Integer> newIndexes = new ArrayList<Integer>();
            Iterator<Integer> nTermPossibleIndexesIterator = nTermPossibleIndexes.iterator();
            if (tagComponent instanceof AminoAcidPattern) {
                for (AminoAcidSequence nTermSequence : nTermPossibleSequences) {
                    int aaIndex = nTermPossibleIndexesIterator.next();
                    AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) tagComponent;
                    int startIndex = aaIndex - aminoAcidPattern.length();
                    if (startIndex >= 0) {
                        String subSequence = sequence.substring(startIndex, aaIndex);
                        if (aminoAcidPattern.matches(subSequence, matchingType, massTolerance)) {
                            AminoAcidSequence newSequence = new AminoAcidSequence(subSequence, aminoAcidPattern.getModificationMatches());
                            boolean goodTerminalPTms = true;
                            if (i == 0) {
                                for (String modificationName : fixedModifications) {
                                    PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                    if (ptm.getType() == PTM.MODNP || ptm.getType() == PTM.MODN && startIndex == 0) {
                                        boolean found = false;
                                        for (ModificationMatch modificationMatch : newSequence.getModificationsAt(1)) {
                                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                                found = true;
                                                break;
                                            }
                                        }
                                        if (!found) {
                                            goodTerminalPTms = false;
                                            break;
                                        }
                                    } else if (ptm.getType() == PTM.MODNPAA || ptm.getType() == PTM.MODNAA && startIndex == 0) {
                                        if (ptm.getPattern().firstIndex(subSequence, matchingType, massTolerance) != 0) {
                                            goodTerminalPTms = false;
                                            break;
                                        }
                                        boolean found = false;
                                        for (ModificationMatch modificationMatch : newSequence.getModificationsAt(1)) {
                                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                                found = true;
                                                break;
                                            }
                                        }
                                        if (!found) {
                                            goodTerminalPTms = false;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (goodTerminalPTms) {
                                newSequence.append(nTermSequence);
                                newIndexes.add(startIndex);
                                newSequences.add(newSequence);
                            }
                        }
                    }
                }
            } else if (tagComponent instanceof AminoAcidSequence) {
                for (AminoAcidSequence nTermSequence : nTermPossibleSequences) {
                    int aaIndex = nTermPossibleIndexesIterator.next();
                    AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) tagComponent;
                    int startIndex = aaIndex - aminoAcidSequence.length();
                    if (startIndex >= 0) {
                        String subSequence = sequence.substring(startIndex, aaIndex);
                        if (aminoAcidSequence.matches(subSequence, matchingType, massTolerance)) {
                            AminoAcidSequence newSequence = new AminoAcidSequence(subSequence, aminoAcidSequence.getModificationMatches());
                            boolean goodTerminalPTms = true;
                            if (i == 0) {
                                for (String modificationName : fixedModifications) {
                                    PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                    if (ptm.getType() == PTM.MODNP || ptm.getType() == PTM.MODN && startIndex == 0) {
                                        boolean found = false;
                                        for (ModificationMatch modificationMatch : newSequence.getModificationsAt(1)) {
                                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                                found = true;
                                                break;
                                            }
                                        }
                                        if (!found) {
                                            goodTerminalPTms = false;
                                            break;
                                        }
                                    } else if (ptm.getType() == PTM.MODNPAA || ptm.getType() == PTM.MODNAA && startIndex == 0) {
                                        if (ptm.getPattern().firstIndex(subSequence, matchingType, massTolerance) != 0) {
                                            goodTerminalPTms = false;
                                            break;
                                        }
                                        boolean found = false;
                                        for (ModificationMatch modificationMatch : newSequence.getModificationsAt(1)) {
                                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                                found = true;
                                                break;
                                            }
                                        }
                                        if (!found) {
                                            goodTerminalPTms = false;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (goodTerminalPTms) {
                                newSequence.append(nTermSequence);
                                newIndexes.add(startIndex);
                                newSequences.add(newSequence);
                            }
                        }
                    }
                }
            } else if (tagComponent instanceof MassGap) {
                double massGap = tagComponent.getMass();
                for (AminoAcidSequence nTermSequence : nTermPossibleSequences) {
                    int aaIndex = nTermPossibleIndexesIterator.next();
                    int currentIndex = aaIndex;
                    ArrayList<Double> possiblePatternsMasses = new ArrayList<Double>();
                    ArrayList<AminoAcidSequence> possibleSequences = new ArrayList<AminoAcidSequence>();
                    ArrayList<AminoAcidSequence> validSequences = new ArrayList<AminoAcidSequence>();
                    while (--aaIndex >= 0) {
                        char aa = sequence.charAt(aaIndex);
                        AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
                        double fixedMass = 0;
                        ArrayList<ModificationMatch> fixedModificationMatches = new ArrayList<ModificationMatch>();
                        for (String modificationName : fixedModifications) {
                            PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                            AminoAcidPattern ptmPattern = ptm.getPattern();
                            if (ptm.getType() == PTM.MODAA && ptmPattern.isTargeted(aa, ptmPattern.getTarget(), matchingType, massTolerance)) {
                                fixedMass += ptm.getMass();
                                fixedModificationMatches.add(new ModificationMatch(modificationName, false, 1));
                            }
                        }
                        if (possiblePatternsMasses.isEmpty()) {
                            AminoAcidSequence newPattern = new AminoAcidSequence(aminoAcid.singleLetterCode);
                            if (reportFixedPtms) {
                                for (ModificationMatch modificationMatch : fixedModificationMatches) {
                                    newPattern.addModificationMatch(1, modificationMatch);
                                }
                            }
                            double noModMass = aminoAcid.monoisotopicMass + fixedMass;
                            possibleSequences.add(newPattern);
                            possiblePatternsMasses.add(noModMass);
                            for (String modificationName : variableModifications) {
                                PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                AminoAcidPattern ptmPattern = ptm.getPattern();
                                if (ptm.getType() == PTM.MODAA && ptmPattern.isTargeted(aa, ptmPattern.getTarget(), matchingType, massTolerance)) {
                                    newPattern = new AminoAcidSequence(aminoAcid.singleLetterCode);
                                    newPattern.addModificationMatch(1, new ModificationMatch(modificationName, true, 1));
                                    double newMass = noModMass + ptm.getMass();
                                    possibleSequences.add(newPattern);
                                    possiblePatternsMasses.add(newMass);
                                }
                            }
                        } else {
                            ArrayList<Double> newPossibleSequencesMasses = new ArrayList<Double>();
                            ArrayList<AminoAcidSequence> newPossibleSequences = new ArrayList<AminoAcidSequence>();
                            Iterator<AminoAcidSequence> newPossibleSequencesMassesIterator = possibleSequences.iterator();
                            for (double mass : possiblePatternsMasses) {
                                AminoAcidSequence aminoAcidSequence = newPossibleSequencesMassesIterator.next();
                                AminoAcidSequence newSequence = new AminoAcidSequence(aminoAcid.singleLetterCode);
                                double noModMass = aminoAcid.monoisotopicMass + fixedMass + mass;
                                newSequence.append(aminoAcidSequence);
                                if (reportFixedPtms) {
                                    for (ModificationMatch modificationMatch : fixedModificationMatches) {
                                        newSequence.addModificationMatch(1, modificationMatch);
                                    }
                                }
                                newPossibleSequences.add(newSequence);
                                newPossibleSequencesMasses.add(noModMass);
                                for (String modificationName : variableModifications) {
                                    PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                    AminoAcidPattern ptmPattern = ptm.getPattern();
                                    if (ptm.getType() == PTM.MODAA && ptmPattern.isTargeted(aa, ptmPattern.getTarget(), matchingType, massTolerance)) {
                                        newSequence = new AminoAcidSequence(aminoAcid.singleLetterCode);
                                        newSequence.append(aminoAcidSequence);
                                        newSequence.addModificationMatch(1, new ModificationMatch(modificationName, true, 1));
                                        double newMass = noModMass + ptm.getMass();
                                        newPossibleSequences.add(newSequence);
                                        newPossibleSequencesMasses.add(newMass);
                                    }
                                }
                            }
                            possibleSequences.clear();
                            possiblePatternsMasses.clear();
                            possibleSequences = newPossibleSequences;
                            possiblePatternsMasses = newPossibleSequencesMasses;
                        }
                        ArrayList<ModificationMatch> nTermModifications = new ArrayList<ModificationMatch>();
                        double fixedNTermModifications = 0;
                        for (String modificationName : fixedModifications) {
                            PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                            if (ptm.getType() == PTM.MODNP) {
                                fixedNTermModifications = ptm.getMass();
                                nTermModifications.add(new ModificationMatch(modificationName, false, 1));
                                break;
                            }
                        }
                        if (aaIndex == 0) {
                            for (String modificationName : fixedModifications) {
                                PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                if (ptm.getType() == PTM.MODN) {
                                    fixedNTermModifications = ptm.getMass();
                                    nTermModifications.add(new ModificationMatch(modificationName, false, 1));
                                    break;
                                }
                            }
                        }
                        Iterator<AminoAcidSequence> possibleSequencesIterator = possibleSequences.iterator();
                        Iterator<Double> possiblePatternsMassesIterator = possiblePatternsMasses.iterator();
                        while (possibleSequencesIterator.hasNext()) {
                            AminoAcidSequence aminoAcidSequence = possibleSequencesIterator.next();
                            double mass = possiblePatternsMassesIterator.next();
                            for (String modificationName : fixedModifications) {
                                PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                AminoAcidPattern ptmPattern = ptm.getPattern();
                                if (ptm.getType() == PTM.MODNPAA && ptmPattern.isTargeted(aminoAcidSequence.charAt(0), ptmPattern.getTarget(), matchingType, massTolerance)) {
                                    fixedNTermModifications = ptm.getMass();
                                    nTermModifications.add(new ModificationMatch(modificationName, false, 1));
                                    break;
                                }
                            }
                            if (aaIndex == 0) {
                                for (String modificationName : fixedModifications) {
                                    PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                    AminoAcidPattern ptmPattern = ptm.getPattern();
                                    if (ptm.getType() == PTM.MODNAA && ptmPattern.isTargeted(aminoAcidSequence.charAt(0), ptmPattern.getTarget(), matchingType, massTolerance)) {
                                        fixedNTermModifications = ptm.getMass();
                                        nTermModifications.add(new ModificationMatch(modificationName, false, 1));
                                        break;
                                    }
                                }
                            }
                            if (mass + fixedNTermModifications + minMod > massGap + massTolerance) {
                                possibleSequencesIterator.remove();
                                possiblePatternsMassesIterator.remove();
                            } else {
                                if (Math.abs(mass + fixedNTermModifications - massGap) <= massTolerance) {
                                    if (reportFixedPtms) {
                                        for (ModificationMatch modificationMatch : nTermModifications) {
                                            aminoAcidSequence.addModificationMatch(1, modificationMatch);
                                        }
                                    }
                                    validSequences.add(aminoAcidSequence);
                                    possibleSequencesIterator.remove();
                                    possiblePatternsMassesIterator.remove();
                                } else {
                                    boolean found = false;
                                    if (aaIndex == 0) {
                                        for (String modificationName : variableModifications) {
                                            PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                            AminoAcidPattern ptmPattern = ptm.getPattern();
                                            if (ptm.getType() == PTM.MODN || ptm.getType() == PTM.MODNAA && ptmPattern.isTargeted(aminoAcidSequence.charAt(0), ptmPattern.getTarget(), matchingType, massTolerance)) {
                                                if (Math.abs(mass + fixedNTermModifications + ptm.getMass() - massGap) <= massTolerance) {
                                                    aminoAcidSequence.addModificationMatch(1, new ModificationMatch(modificationName, true, 1));
                                                    if (reportFixedPtms) {
                                                        for (ModificationMatch modificationMatch : nTermModifications) {
                                                            aminoAcidSequence.addModificationMatch(1, modificationMatch);
                                                        }
                                                    }
                                                    validSequences.add(aminoAcidSequence);
                                                    possibleSequencesIterator.remove();
                                                    possiblePatternsMassesIterator.remove();
                                                    found = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    if (!found) {
                                        for (String modificationName : variableModifications) {
                                            PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                            AminoAcidPattern ptmPattern = ptm.getPattern();
                                            if (ptm.getType() == PTM.MODNP || ptm.getType() == PTM.MODNPAA && ptmPattern.isTargeted(aminoAcidSequence.charAt(0), ptmPattern.getTarget(), matchingType, massTolerance)) {
                                                if (Math.abs(mass + fixedNTermModifications + ptm.getMass() - massGap) <= massTolerance) {
                                                    aminoAcidSequence.addModificationMatch(1, new ModificationMatch(modificationName, true, 1));
                                                    if (reportFixedPtms) {
                                                        for (ModificationMatch modificationMatch : nTermModifications) {
                                                            aminoAcidSequence.addModificationMatch(1, modificationMatch);
                                                        }
                                                    }
                                                    validSequences.add(aminoAcidSequence);
                                                    possibleSequencesIterator.remove();
                                                    possiblePatternsMassesIterator.remove();
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (possibleSequences.isEmpty()) {
                            break;
                        }
                    }
                    for (AminoAcidSequence aminoAcidSequence : validSequences) {
                        aminoAcidSequence.append(nTermSequence);
                        int newIndex = currentIndex - aminoAcidSequence.length();
                        newIndexes.add(newIndex);
                        newSequences.add(aminoAcidSequence);
                    }
                }
            } else {
                throw new IllegalArgumentException("Tag component " + tagComponent.getClass() + " not implemented for sequence matching.");
            }
            if (newIndexes.isEmpty()) {
                return result;
            } else {
                nTermPossibleIndexes.clear();
                nTermPossibleSequences.clear();
                nTermPossibleIndexes = newIndexes;
                nTermPossibleSequences = newSequences;
            }
        }
        TagComponent componentAtIndex = content.get(componentIndex);
        int componentAtIndexLength;
        int endTagIndex = tagIndex - 1;
        HashMap<Integer, ArrayList<ModificationMatch>> modificationsAtIndex = null;
        if (componentAtIndex instanceof AminoAcidPattern) {
            AminoAcidPattern tagPattern = (AminoAcidPattern) componentAtIndex;
            componentAtIndexLength = tagPattern.length();
            modificationsAtIndex = tagPattern.getModificationMatches();
            endTagIndex += componentAtIndexLength;
            // if the component is at the N-term check its n-term modifications
            if (componentIndex == 0) {
                boolean goodTerminalPTms = true;
                for (String modificationName : fixedModifications) {
                    PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                    if (ptm.getType() == PTM.MODNP || ptm.getType() == PTM.MODN && tagIndex == 0) {
                        boolean found = false;
                        for (ModificationMatch modificationMatch : tagPattern.getModificationsAt(1)) {
                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            goodTerminalPTms = false;
                            break;
                        }
                    } else if (ptm.getType() == PTM.MODNPAA || ptm.getType() == PTM.MODNAA && tagIndex == 0) {
                        if (ptm.getPattern().firstIndex(tagPattern, matchingType, massTolerance) != 0) {
                            goodTerminalPTms = false;
                            break;
                        }
                        boolean found = false;
                        for (ModificationMatch modificationMatch : tagPattern.getModificationsAt(1)) {
                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            goodTerminalPTms = false;
                            break;
                        }
                    }
                }
                if (!goodTerminalPTms) {
                    return result;
                }
            }
            // if the component is at the C-term check its c-term modifications
            if (componentIndex == content.size() - 1) {
                boolean goodTerminalPTms = true;
                for (String modificationName : fixedModifications) {
                    PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                    if (ptm.getType() == PTM.MODCP || ptm.getType() == PTM.MODC && endTagIndex == sequenceLastIndex) {
                        boolean found = false;
                        for (ModificationMatch modificationMatch : tagPattern.getModificationsAt(tagPattern.length())) {
                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            goodTerminalPTms = false;
                            break;
                        }
                    } else if (ptm.getType() == PTM.MODCPAA || ptm.getType() == PTM.MODCAA && endTagIndex == sequenceLastIndex) {
                        if (ptm.getPattern().firstIndex(tagPattern, matchingType, massTolerance) != 0) {
                            goodTerminalPTms = false;
                            break;
                        }
                        boolean found = false;
                        for (ModificationMatch modificationMatch : tagPattern.getModificationsAt(tagPattern.length())) {
                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            goodTerminalPTms = false;
                            break;
                        }
                    }
                }
                if (!goodTerminalPTms) {
                    return result;
                }
            }
        } else if (componentAtIndex instanceof AminoAcidSequence) {
            AminoAcidSequence tagSequence = (AminoAcidSequence) componentAtIndex;
            componentAtIndexLength = tagSequence.length();
            modificationsAtIndex = tagSequence.getModificationMatches();
            endTagIndex += componentAtIndexLength;
            // if the component is at the N-term check its n-term modifications
            if (componentIndex == 0) {
                boolean goodTerminalPTms = true;
                for (String modificationName : fixedModifications) {
                    PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                    if (ptm.getType() == PTM.MODNP || ptm.getType() == PTM.MODN && tagIndex == 0) {
                        boolean found = false;
                        for (ModificationMatch modificationMatch : tagSequence.getModificationsAt(1)) {
                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            goodTerminalPTms = false;
                            break;
                        }
                    } else if (ptm.getType() == PTM.MODNPAA || ptm.getType() == PTM.MODNAA && tagIndex == 0) {
                        if (ptm.getPattern().firstIndex(tagSequence, matchingType, massTolerance) != 0) {
                            goodTerminalPTms = false;
                            break;
                        }
                        boolean found = false;
                        for (ModificationMatch modificationMatch : tagSequence.getModificationsAt(1)) {
                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            goodTerminalPTms = false;
                            break;
                        }
                    }
                }
                if (!goodTerminalPTms) {
                    return result;
                }
            }
            // if the component is at the C-term check its c-term modifications
            if (componentIndex == content.size() - 1) {
                boolean goodTerminalPTms = true;
                for (String modificationName : fixedModifications) {
                    PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                    if (ptm.getType() == PTM.MODCP || ptm.getType() == PTM.MODC && endTagIndex == sequenceLastIndex) {
                        boolean found = false;
                        for (ModificationMatch modificationMatch : tagSequence.getModificationsAt(tagSequence.length())) {
                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            goodTerminalPTms = false;
                            break;
                        }
                    } else if (ptm.getType() == PTM.MODCPAA || ptm.getType() == PTM.MODCAA && endTagIndex == sequenceLastIndex) {
                        if (ptm.getPattern().firstIndex(tagSequence, matchingType, massTolerance) != 0) {
                            goodTerminalPTms = false;
                            break;
                        }
                        boolean found = false;
                        for (ModificationMatch modificationMatch : tagSequence.getModificationsAt(tagSequence.length())) {
                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            goodTerminalPTms = false;
                            break;
                        }
                    }
                }
                if (!goodTerminalPTms) {
                    return result;
                }
            }
        } else {
            throw new UnsupportedOperationException("Tag mapping not supported for tag component " + componentAtIndex.getClass() + ".");
        }
        // Check tag components to the C-term
        ArrayList<AminoAcidSequence> cTermPossibleSequences = new ArrayList<AminoAcidSequence>();
        ArrayList<Integer> cTermPossibleIndexes = new ArrayList<Integer>();
        cTermPossibleSequences.add(new AminoAcidSequence());
        cTermPossibleIndexes.add(endTagIndex);
        for (int i = componentIndex + 1; i < content.size(); i++) {
            TagComponent tagComponent = content.get(i);
            ArrayList<AminoAcidSequence> newSequences = new ArrayList<AminoAcidSequence>();
            ArrayList<Integer> newIndexes = new ArrayList<Integer>();
            Iterator<Integer> cTermPossibleIndexesIterator = cTermPossibleIndexes.iterator();
            if (tagComponent instanceof AminoAcidPattern) {
                for (AminoAcidSequence cTermSequence : cTermPossibleSequences) {
                    int aaIndex = cTermPossibleIndexesIterator.next();
                    AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) tagComponent;
                    int endIndex = aaIndex + aminoAcidPattern.length();
                    if (endIndex <= sequenceLastIndex) {
                        String subSequence = sequence.substring(aaIndex, endIndex);
                        if (aminoAcidPattern.matches(subSequence, matchingType, massTolerance)) {
                            AminoAcidSequence newSequence = new AminoAcidSequence(subSequence, aminoAcidPattern.getModificationMatches());
                            boolean goodTerminalPTms = true;
                            if (i == 0) {
                                for (String modificationName : fixedModifications) {
                                    PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                    if (ptm.getType() == PTM.MODCP || ptm.getType() == PTM.MODC && endIndex == sequenceLastIndex) {
                                        boolean found = false;
                                        for (ModificationMatch modificationMatch : newSequence.getModificationsAt(newSequence.length())) {
                                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                                found = true;
                                                break;
                                            }
                                        }
                                        if (!found) {
                                            goodTerminalPTms = false;
                                            break;
                                        }
                                    } else if (ptm.getType() == PTM.MODCPAA || ptm.getType() == PTM.MODCAA && endIndex == sequenceLastIndex) {
                                        if (ptm.getPattern().firstIndex(subSequence, matchingType, massTolerance) != 0) {
                                            goodTerminalPTms = false;
                                            break;
                                        }
                                        boolean found = false;
                                        for (ModificationMatch modificationMatch : newSequence.getModificationsAt(newSequence.length())) {
                                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                                found = true;
                                                break;
                                            }
                                        }
                                        if (!found) {
                                            goodTerminalPTms = false;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (goodTerminalPTms) {
                                cTermSequence.append(newSequence);
                                newIndexes.add(endIndex);
                                newSequences.add(cTermSequence);
                            }
                        }
                    }
                }
            } else if (tagComponent instanceof AminoAcidSequence) {
                for (AminoAcidSequence cTermSequence : cTermPossibleSequences) {
                    int aaIndex = cTermPossibleIndexesIterator.next();
                    AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) tagComponent;
                    int endIndex = aaIndex + aminoAcidSequence.length();
                    if (endIndex <= sequenceLastIndex) {
                        String subSequence = sequence.substring(aaIndex, endIndex);
                        if (aminoAcidSequence.matches(subSequence, matchingType, massTolerance)) {
                            AminoAcidSequence newSequence = new AminoAcidSequence(subSequence, aminoAcidSequence.getModificationMatches());
                            boolean goodTerminalPTms = true;
                            if (i == 0) {
                                for (String modificationName : fixedModifications) {
                                    PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                    if (ptm.getType() == PTM.MODCP || ptm.getType() == PTM.MODC && endIndex == sequenceLastIndex) {
                                        boolean found = false;
                                        for (ModificationMatch modificationMatch : newSequence.getModificationsAt(newSequence.length())) {
                                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                                found = true;
                                                break;
                                            }
                                        }
                                        if (!found) {
                                            goodTerminalPTms = false;
                                            break;
                                        }
                                    } else if (ptm.getType() == PTM.MODCPAA || ptm.getType() == PTM.MODCAA && endIndex == sequenceLastIndex) {
                                        if (ptm.getPattern().firstIndex(subSequence, matchingType, massTolerance) != 0) {
                                            goodTerminalPTms = false;
                                            break;
                                        }
                                        boolean found = false;
                                        for (ModificationMatch modificationMatch : newSequence.getModificationsAt(newSequence.length())) {
                                            if (modificationMatch.getTheoreticPtm().equals(modificationName)) {
                                                found = true;
                                                break;
                                            }
                                        }
                                        if (!found) {
                                            goodTerminalPTms = false;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (goodTerminalPTms) {
                                cTermSequence.append(newSequence);
                                newIndexes.add(endIndex);
                                newSequences.add(cTermSequence);
                            }
                        }
                    }
                }
            } else if (tagComponent instanceof MassGap) {
                double massGap = tagComponent.getMass();
                for (AminoAcidSequence cTermSequence : cTermPossibleSequences) {
                    int aaIndex = cTermPossibleIndexesIterator.next();
                    int currentIndex = aaIndex;
                    ArrayList<Double> possibleSequencesMasses = new ArrayList<Double>();
                    ArrayList<AminoAcidSequence> possibleSequences = new ArrayList<AminoAcidSequence>();
                    ArrayList<AminoAcidSequence> validSequences = new ArrayList<AminoAcidSequence>();
                    while (++aaIndex <= sequenceLastIndex) {
                        char aa = sequence.charAt(aaIndex);
                        AminoAcid aminoAcid = AminoAcid.getAminoAcid(aa);
                        double fixedMass = 0;
                        ArrayList<ModificationMatch> fixedModificationMatches = new ArrayList<ModificationMatch>();
                        for (String modificationName : fixedModifications) {
                            PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                            AminoAcidPattern ptmPattern = ptm.getPattern();
                            if (ptm.getType() == PTM.MODAA && ptmPattern.isTargeted(aa, ptmPattern.getTarget(), matchingType, massTolerance)) {
                                fixedMass += ptm.getMass();
                                fixedModificationMatches.add(new ModificationMatch(modificationName, false, 1));
                            }
                        }
                        if (possibleSequencesMasses.isEmpty()) {
                            AminoAcidSequence newSequence = new AminoAcidSequence(aminoAcid.singleLetterCode);
                            int modIndex = newSequence.length();
                            if (reportFixedPtms) {
                                for (ModificationMatch modificationMatch : fixedModificationMatches) {
                                    modificationMatch.setModificationSite(modIndex);
                                    newSequence.addModificationMatch(modIndex, modificationMatch);
                                }
                            }
                            double noModMass = aminoAcid.monoisotopicMass + fixedMass;
                            possibleSequences.add(newSequence);
                            possibleSequencesMasses.add(noModMass);
                            for (String modificationName : variableModifications) {
                                PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                AminoAcidPattern ptmPattern = ptm.getPattern();
                                if (ptm.getType() == PTM.MODAA && ptmPattern.isTargeted(aa, ptmPattern.getTarget(), matchingType, massTolerance)) {
                                    newSequence = new AminoAcidSequence(aminoAcid.singleLetterCode);
                                    newSequence.addModificationMatch(modIndex, new ModificationMatch(modificationName, true, modIndex));
                                    double newMass = noModMass + ptm.getMass();
                                    possibleSequences.add(newSequence);
                                    possibleSequencesMasses.add(newMass);
                                }
                            }
                        } else {
                            ArrayList<Double> newPossiblePatternsMasses = new ArrayList<Double>();
                            ArrayList<AminoAcidSequence> newPossibleSequences = new ArrayList<AminoAcidSequence>();
                            Iterator<AminoAcidSequence> newPossibleSequencesMassesIterator = possibleSequences.iterator();
                            for (double mass : possibleSequencesMasses) {
                                AminoAcidSequence aminoAcidSequence = newPossibleSequencesMassesIterator.next();
                                AminoAcidSequence newSequence = new AminoAcidSequence(aminoAcidSequence);
                                double noModMass = aminoAcid.monoisotopicMass + fixedMass + mass;
                                newSequence.append(aminoAcid.singleLetterCode);
                                int modIndex = newSequence.length();
                                if (reportFixedPtms) {
                                    for (ModificationMatch modificationMatch : fixedModificationMatches) {
                                        modificationMatch.setModificationSite(modIndex);
                                        newSequence.addModificationMatch(modIndex, modificationMatch);
                                    }
                                }
                                newPossibleSequences.add(newSequence);
                                newPossiblePatternsMasses.add(noModMass);
                                for (String modificationName : variableModifications) {
                                    PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                    AminoAcidPattern ptmPattern = ptm.getPattern();
                                    if (ptm.getType() == PTM.MODAA && ptmPattern.isTargeted(aa, ptmPattern.getTarget(), matchingType, massTolerance)) {
                                        newSequence = new AminoAcidSequence(aminoAcidSequence);
                                        newSequence.append(aminoAcid.singleLetterCode);
                                        newSequence.addModificationMatch(modIndex, new ModificationMatch(modificationName, true, modIndex));
                                        double newMass = noModMass + ptm.getMass();
                                        newPossibleSequences.add(newSequence);
                                        newPossiblePatternsMasses.add(newMass);
                                    }
                                }
                            }
                            possibleSequences.clear();
                            possibleSequencesMasses.clear();
                            possibleSequences = newPossibleSequences;
                            possibleSequencesMasses = newPossiblePatternsMasses;
                        }
                        double fixedCTermModifications = 0;
                        ArrayList<ModificationMatch> cTermModifications = new ArrayList<ModificationMatch>();
                        for (String modificationName : fixedModifications) {
                            PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                            if (ptm.getType() == PTM.MODCP) {
                                fixedCTermModifications = ptm.getMass();
                                cTermModifications.add(new ModificationMatch(modificationName, false, 1));
                                break;
                            }
                        }
                        if (aaIndex == sequenceLastIndex) {
                            for (String modificationName : fixedModifications) {
                                PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                if (ptm.getType() == PTM.MODC) {
                                    fixedCTermModifications = ptm.getMass();
                                    cTermModifications.add(new ModificationMatch(modificationName, false, 1));
                                    break;
                                }
                            }
                        }
                        Iterator<AminoAcidSequence> possibleSequencesIterator = possibleSequences.iterator();
                        Iterator<Double> possibleSequencesMassesIterator = possibleSequencesMasses.iterator();
                        while (possibleSequencesIterator.hasNext()) {
                            AminoAcidSequence aminoAcidSequence = possibleSequencesIterator.next();
                            double mass = possibleSequencesMassesIterator.next();
                            int lastAminoAcidIndex = aminoAcidSequence.length() - 1;
                            for (String modificationName : fixedModifications) {
                                PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                AminoAcidPattern ptmPattern = ptm.getPattern();
                                if (ptm.getType() == PTM.MODCPAA && ptmPattern.isTargeted(aminoAcidSequence.charAt(lastAminoAcidIndex), ptmPattern.getTarget(), matchingType, massTolerance)) {
                                    fixedCTermModifications = ptm.getMass();
                                    cTermModifications.add(new ModificationMatch(modificationName, false, 1));
                                    break;
                                }
                            }
                            if (aaIndex == lastAminoAcidIndex) {
                                for (String modificationName : fixedModifications) {
                                    PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                    AminoAcidPattern ptmPattern = ptm.getPattern();
                                    if (ptm.getType() == PTM.MODCAA && ptmPattern.isTargeted(aminoAcidSequence.charAt(lastAminoAcidIndex), ptmPattern.getTarget(), matchingType, massTolerance)) {
                                        fixedCTermModifications = ptm.getMass();
                                        cTermModifications.add(new ModificationMatch(modificationName, false, 1));
                                        break;
                                    }
                                }
                            }
                            if (mass + fixedCTermModifications + minMod > massGap + massTolerance) {
                                possibleSequencesIterator.remove();
                                possibleSequencesMassesIterator.remove();
                            } else {
                                int modIndex = aminoAcidSequence.length();
                                if (Math.abs(mass + fixedCTermModifications - massGap) <= massTolerance) {
                                    validSequences.add(aminoAcidSequence);
                                    if (reportFixedPtms) {
                                        for (ModificationMatch modificationMatch : cTermModifications) {
                                            modificationMatch.setModificationSite(modIndex);
                                            aminoAcidSequence.addModificationMatch(modIndex, modificationMatch);
                                        }
                                    }
                                    possibleSequencesIterator.remove();
                                    possibleSequencesMassesIterator.remove();
                                } else {
                                    boolean found = false;
                                    if (aaIndex == lastAminoAcidIndex) {
                                        for (String modificationName : variableModifications) {
                                            PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                            AminoAcidPattern ptmPattern = ptm.getPattern();
                                            if (ptm.getType() == PTM.MODC || ptm.getType() == PTM.MODCAA && ptmPattern.isTargeted(aminoAcidSequence.charAt(lastAminoAcidIndex), ptmPattern.getTarget(), matchingType, massTolerance)) {
                                                if (Math.abs(mass + fixedCTermModifications + ptm.getMass() - massGap) <= massTolerance) {
                                                    aminoAcidSequence.addModificationMatch(lastAminoAcidIndex, new ModificationMatch(modificationName, true, modIndex));
                                                    validSequences.add(aminoAcidSequence);
                                                    if (reportFixedPtms) {
                                                        for (ModificationMatch modificationMatch : cTermModifications) {
                                                            modificationMatch.setModificationSite(modIndex);
                                                            aminoAcidSequence.addModificationMatch(modIndex, modificationMatch);
                                                        }
                                                    }
                                                    possibleSequencesIterator.remove();
                                                    possibleSequencesMassesIterator.remove();
                                                    found = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    if (!found) {
                                        for (String modificationName : variableModifications) {
                                            PTM ptm = PTMFactory.getInstance().getPTM(modificationName);
                                            AminoAcidPattern ptmPattern = ptm.getPattern();
                                            if (ptm.getType() == PTM.MODCP || ptm.getType() == PTM.MODCPAA && ptmPattern.isTargeted(aminoAcidSequence.charAt(lastAminoAcidIndex), ptmPattern.getTarget(), matchingType, massTolerance)) {
                                                if (Math.abs(mass + fixedCTermModifications + ptm.getMass() - massGap) <= massTolerance) {
                                                    aminoAcidSequence.addModificationMatch(lastAminoAcidIndex, new ModificationMatch(modificationName, true, modIndex));
                                                    if (reportFixedPtms) {
                                                        for (ModificationMatch modificationMatch : cTermModifications) {
                                                            modificationMatch.setModificationSite(modIndex);
                                                            aminoAcidSequence.addModificationMatch(modIndex, modificationMatch);
                                                        }
                                                    }
                                                    validSequences.add(aminoAcidSequence);
                                                    possibleSequencesIterator.remove();
                                                    possibleSequencesMassesIterator.remove();
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        if (possibleSequencesMasses.isEmpty()) {
                            break;
                        }
                    }
                    for (AminoAcidSequence validSequence : validSequences) {
                        AminoAcidSequence newSequence = new AminoAcidSequence(cTermSequence);
                        newSequence.append(validSequence);
                        int newIndex = currentIndex + validSequence.length();
                        newIndexes.add(newIndex);
                        newSequences.add(validSequence);
                    }
                }
            } else {
                throw new IllegalArgumentException("Tag component " + tagComponent.getClass() + " not implemented for sequence mating.");
            }
            if (newIndexes.isEmpty()) {
                return result;
            } else {
                cTermPossibleIndexes.clear();
                cTermPossibleSequences.clear();
                cTermPossibleIndexes = newIndexes;
                cTermPossibleSequences = newSequences;
            }
        }
        // create all possible peptide sequences by adding all possible N and C term to the seed sequence
        String seedSequence = sequence.substring(tagIndex, tagIndex + componentAtIndexLength);
        Iterator<Integer> nTermPossibleIndexesIterator = nTermPossibleIndexes.iterator();
        for (AminoAcidSequence nTermPattern : nTermPossibleSequences) {
            int nTermIndex = nTermPossibleIndexesIterator.next();

            StringBuilder nTermSequence = new StringBuilder(nTermPattern.length() + seedSequence.length());
            nTermSequence.append(nTermPattern.getSequence());
            for (int i = 0; i < nTermPattern.length(); i++) {
                nTermSequence.append(nTermPattern.charAt(i));
            }
            nTermSequence.append(seedSequence);
            for (AminoAcidSequence cTermPattern : cTermPossibleSequences) {
                StringBuilder peptideSequence = new StringBuilder(nTermPattern.length() + seedSequence.length() + cTermPattern.length());
                peptideSequence.append(nTermSequence);
                ArrayList<ModificationMatch> modificationMatches = new ArrayList<ModificationMatch>();
                HashMap<Integer, ArrayList<ModificationMatch>> nTermModifications = nTermPattern.getModificationMatches();
                if (nTermModifications != null) {
                    for (int i : nTermModifications.keySet()) {
                        for (ModificationMatch modificationMatch : nTermModifications.get(i)) {
                            modificationMatches.add(new ModificationMatch(modificationMatch.getTheoreticPtm(), modificationMatch.isVariable(), i));
                        }
                    }
                }
                if (modificationsAtIndex != null) {
                    for (int i : modificationsAtIndex.keySet()) {
                        for (ModificationMatch modificationMatch : modificationsAtIndex.get(i)) {
                            modificationMatches.add(new ModificationMatch(modificationMatch.getTheoreticPtm(), modificationMatch.isVariable(), nTermPattern.length() + i));
                        }
                    }
                }
                peptideSequence.append(cTermPattern.getSequence());
                HashMap<Integer, ArrayList<ModificationMatch>> cTermModifications = cTermPattern.getModificationMatches();
                if (cTermModifications != null) {
                    for (int i : cTermModifications.keySet()) {
                        for (ModificationMatch modificationMatch : cTermModifications.get(i)) {
                            modificationMatches.add(new ModificationMatch(modificationMatch.getTheoreticPtm(), modificationMatch.isVariable(), nTermPattern.length() + componentAtIndexLength + i));
                        }
                    }
                }
                Peptide peptide = new Peptide(peptideSequence.toString(), modificationMatches);
                ArrayList<Peptide> peptides = result.get(nTermIndex);
                if (peptides == null) {
                    peptides = new ArrayList<Peptide>();
                    result.put(nTermIndex, peptides);
                }
                peptides.add(peptide);
            }
        }
        return result;
    }

    /**
     * Returns the tag modifications as a string.
     *
     * @param tag the tag
     * @return the peptide modifications as a string
     */
    public static String getTagModificationsAsString(Tag tag) {

        HashMap<String, ArrayList<Integer>> modMap = new HashMap<String, ArrayList<Integer>>();
        int offset = 0;
        for (TagComponent tagComponent : tag.getContent()) {
            if (tagComponent instanceof MassGap) {
                offset++;
            } else if (tagComponent instanceof AminoAcidPattern) {
                AminoAcidPattern aminoAcidPattern = (AminoAcidPattern) tagComponent;
                for (int i = 1; i <= aminoAcidPattern.length(); i++) {
                    for (ModificationMatch modificationMatch : aminoAcidPattern.getModificationsAt(i)) {
                        if (modificationMatch.isVariable()) {
                            if (!modMap.containsKey(modificationMatch.getTheoreticPtm())) {
                                modMap.put(modificationMatch.getTheoreticPtm(), new ArrayList<Integer>());
                            }
                            modMap.get(modificationMatch.getTheoreticPtm()).add(i + offset);
                        }
                    }
                }
                offset += aminoAcidPattern.length();
            } else if (tagComponent instanceof AminoAcidSequence) {
                AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) tagComponent;
                for (int i = 1; i <= aminoAcidSequence.length(); i++) {
                    for (ModificationMatch modificationMatch : aminoAcidSequence.getModificationsAt(i)) {
                        if (modificationMatch.isVariable()) {
                            if (!modMap.containsKey(modificationMatch.getTheoreticPtm())) {
                                modMap.put(modificationMatch.getTheoreticPtm(), new ArrayList<Integer>());
                            }
                            modMap.get(modificationMatch.getTheoreticPtm()).add(i + offset);
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
        ArrayList<String> mods = new ArrayList<String>(modMap.keySet());
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
     * with mass >= water).
     *
     * @return whether the tag can be reversed
     */
    public boolean canReverse() {
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
