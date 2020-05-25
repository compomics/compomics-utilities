package com.compomics.util.experiment.identification.utils;

import com.compomics.util.Util;
import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidPattern;
import com.compomics.util.experiment.biology.aminoacids.sequence.AminoAcidSequence;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationFactory;
import com.compomics.util.experiment.biology.modifications.ModificationType;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.identification.amino_acid_tags.TagComponent;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.parameters.identification.search.ModificationParameters;
import com.compomics.util.parameters.identification.search.SearchParameters;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class groups functions that can be used to work with modifications.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class ModificationUtils {

    /**
     * Empty array for no result.
     */
    public static final int[] EMPTY = new int[0];
    /**
     * Array for N-term.
     */
    public static final int[] ZERO = new int[]{0};
    /**
     * Map of arrays for C-term.
     */
    public static final HashMap<Integer, int[]> ARRAYS_MAP = new HashMap<>();

    /**
     * Empty default constructor.
     */
    public ModificationUtils() {
    }

    /**
     * Returns an array containing only the given index.
     *
     * @param index the index
     *
     * @return an array containing only the given index
     */
    public static int[] getArray(
            int index
    ) {

        int[] result = ARRAYS_MAP.get(index);

        if (result == null) {

            result = new int[1];
            result[0] = index;
            ARRAYS_MAP.put(index, result);

        }

        return result;
    }

    /**
     * Returns an array of the possible modification sites for the given
     * modification on the given peptide. N-term modifications are at index 0,
     * C-term at sequence length + 1, and amino acid at 1-based index on the
     * sequence.
     *
     * @param peptide the peptide
     * @param modification the modification
     * @param sequenceProvider a protein sequence provider
     * @param sequenceMatchingParameters the sequence matching parameters
     *
     * @return an array of the possible modification sites
     */
    public static int[] getPossibleModificationSites(
            Peptide peptide,
            Modification modification,
            SequenceProvider sequenceProvider,
            SequenceMatchingParameters sequenceMatchingParameters
    ) {

        String peptideSequence = peptide.getSequence();
        ModificationType modificationType = modification.getModificationType();

        if (modificationType == null) {

            throw new UnsupportedOperationException(
                    "Modification mapping not supported for modification of type "
                    + modificationType + ".");

        } else {

            switch (modificationType) {

                case modaa: {

                    AminoAcidPattern aminoAcidPattern = modification.getPattern();

                    if (aminoAcidPattern.length() == 1) {

                        return aminoAcidPattern.getIndexes(peptideSequence, sequenceMatchingParameters);

                    } else if (aminoAcidPattern.length() > 1) {

                        int minIndex = aminoAcidPattern.getMinIndex();
                        int maxIndex = aminoAcidPattern.getMaxIndex();
                        IntStream allPossibleIndexes = IntStream.empty();

                        for (Map.Entry<String, int[]> entry : peptide.getProteinMapping().entrySet()) {

                            String accession = entry.getKey();
                            String sequence = sequenceProvider.getSequence(accession);

                            for (int startIndex : entry.getValue()) {

                                StringBuilder extendedSequenceBuilder
                                        = new StringBuilder(peptideSequence.length()
                                                + aminoAcidPattern.length());

                                if (minIndex < 0) {
                                    String prefix = sequence.substring(Math.max(startIndex + minIndex, 0), startIndex);
                                    extendedSequenceBuilder.append(prefix);
                                }

                                extendedSequenceBuilder.append(peptideSequence);

                                if (maxIndex > 0) {

                                    String suffix = sequence.substring(
                                            startIndex + peptideSequence.length(),
                                            Math.min(
                                                    startIndex + peptideSequence.length() + maxIndex, 
                                                    sequence.length()
                                            )
                                    );
                                    extendedSequenceBuilder.append(suffix);
                                }

                                int[] sitesAtIndex = aminoAcidPattern.getIndexes(
                                        extendedSequenceBuilder.toString(), sequenceMatchingParameters);
                                allPossibleIndexes = IntStream.concat(allPossibleIndexes, Arrays.stream(sitesAtIndex));

                            }
                        }

                        allPossibleIndexes = allPossibleIndexes
                                .distinct()
                                .sorted();

                        if (minIndex < 0) {
                            allPossibleIndexes.map(site -> site - minIndex);
                        }

                        return allPossibleIndexes.toArray();

                    } else {

                        throw new IllegalArgumentException(
                                "No pattern set for modification "
                                + modification.getName() + ".");

                    }

                }

                case modnaa_peptide: {

                    AminoAcidPattern aminoAcidPattern = modification.getPattern();

                    if (aminoAcidPattern.length() == 1) {

                        return aminoAcidPattern.matches(
                                Character.toString(peptideSequence.charAt(0)),
                                sequenceMatchingParameters) ? ZERO : EMPTY;

                    } else if (aminoAcidPattern.length() > 1) {

                        int minIndex = aminoAcidPattern.getMinIndex();
                        int maxIndex = aminoAcidPattern.getMaxIndex();

                        if (minIndex == 0 && maxIndex < peptideSequence.length()) {
                            return aminoAcidPattern.matchesAt(
                                    peptideSequence, sequenceMatchingParameters, 0) ? ZERO : EMPTY;
                        }

                        for (Map.Entry<String, int[]> entry : peptide.getProteinMapping().entrySet()) {

                            String accession = entry.getKey();
                            String sequence = sequenceProvider.getSequence(accession);

                            for (int startIndex : entry.getValue()) {

                                int tempStart = startIndex + minIndex;
                                int tempEnd = startIndex + maxIndex + 1;

                                if (tempStart >= 0 && tempEnd <= sequence.length()) {

                                    String subSequence = sequence.substring(tempStart, tempEnd);

                                    if (aminoAcidPattern.matches(subSequence, sequenceMatchingParameters)) {
                                        return ZERO;
                                    
                                    }
                                }
                            }
                        }

                        return EMPTY;

                    } else {

                        throw new IllegalArgumentException(
                                "No pattern set for modification "
                                + modification.getName() + ".");

                    }

                }

                case modn_protein:

                    return PeptideUtils.isNterm(peptide, sequenceProvider) ? ZERO : EMPTY;

                case modn_peptide:

                    return new int[]{0};

                case modnaa_protein: {

                    String[] accessions = peptide.getProteinMapping().entrySet().stream()
                            .filter(entry -> Arrays.stream(entry.getValue()).anyMatch(index -> index == 0))
                            .map(entry -> entry.getKey())
                            .toArray(String[]::new);

                    if (accessions.length > 0) {

                        AminoAcidPattern aminoAcidPattern = modification.getPattern();

                        if (aminoAcidPattern.length() == 1) {

                            return aminoAcidPattern.matches(
                                    Character.toString(peptideSequence.charAt(0)),
                                    sequenceMatchingParameters) ? ZERO : EMPTY;

                        } else if (aminoAcidPattern.length() > 1) {

                            int maxIndex = aminoAcidPattern.getMaxIndex();

                            if (maxIndex < peptideSequence.length()) {
                                return aminoAcidPattern.matchesAt(
                                        peptideSequence, sequenceMatchingParameters, 0) ? ZERO : EMPTY;
                            }

                            for (String accession : accessions) {

                                String sequence = sequenceProvider.getSequence(accession);

                                if (maxIndex < sequence.length()) {

                                    String subSequence = sequence.substring(0, maxIndex + 1);

                                    if (aminoAcidPattern.matches(subSequence, sequenceMatchingParameters)) {
                                        return ZERO;
                                    
                                    }

                                    if (sequence.charAt(0) == 'M' && maxIndex + 1 < sequence.length()) {

                                        subSequence = sequence.substring(1, maxIndex + 2);

                                        if (aminoAcidPattern.matches(subSequence, sequenceMatchingParameters)) {
                                            return ZERO;
                                        
                                        }
                                    }
                                }
                            }
                        } else {

                            throw new IllegalArgumentException(
                                    "No pattern set for modification "
                                    + modification.getName() + ".");

                        }
                    }

                    return EMPTY;

                }

                case modc_peptide:

                    return getArray(peptideSequence.length() + 1);

                case modc_protein:

                    return PeptideUtils.isCterm(peptide, sequenceProvider) ? getArray(peptideSequence.length() + 1) : EMPTY;

                case modcaa_peptide: {

                    AminoAcidPattern aminoAcidPattern = modification.getPattern();

                    if (aminoAcidPattern.length() == 1) {

                        return aminoAcidPattern.matches(
                                Character.toString(peptideSequence.charAt(peptideSequence.length() - 1)),
                                sequenceMatchingParameters) ? getArray(peptideSequence.length() + 1) : EMPTY;

                    } else if (aminoAcidPattern.length() > 1) {

                        int minIndex = aminoAcidPattern.getMinIndex();
                        int maxIndex = aminoAcidPattern.getMaxIndex();
                        int tempStart = peptideSequence.length() + minIndex;

                        if (maxIndex == 0 && tempStart > 0) {
                            return aminoAcidPattern.matchesAt(
                                    peptideSequence, sequenceMatchingParameters,
                                    peptideSequence.length() - 1) ? getArray(peptideSequence.length() + 1) : EMPTY;
                        }

                        for (Map.Entry<String, int[]> entry : peptide.getProteinMapping().entrySet()) {

                            String accession = entry.getKey();
                            String sequence = sequenceProvider.getSequence(accession);

                            for (int startIndex : entry.getValue()) {

                                int tempStartProtein = startIndex + tempStart;
                                int tempEndProtein = startIndex + peptideSequence.length() + maxIndex + 1;

                                if (tempStartProtein >= 0 && tempEndProtein <= sequence.length()) {

                                    String subSequence = sequence.substring(tempStartProtein, tempEndProtein);

                                    if (aminoAcidPattern.matches(subSequence, sequenceMatchingParameters)) {
                                        return getArray(peptideSequence.length() + 1);
                                    
                                    }
                                }
                            }
                        }

                        return EMPTY;

                    } else {

                        throw new IllegalArgumentException(
                                "No pattern set for modification "
                                + modification.getName() + ".");

                    }
                }

                case modcaa_protein: {

                    String[] accessions = peptide.getProteinMapping().entrySet().stream()
                            .filter(entry -> Arrays.stream(entry.getValue())
                            .anyMatch(index -> index + peptideSequence.length()
                            == sequenceProvider.getSequence(entry.getKey()).length()))
                            .map(entry -> entry.getKey())
                            .toArray(String[]::new);

                    if (accessions.length > 0) {

                        AminoAcidPattern aminoAcidPattern = modification.getPattern();

                        if (aminoAcidPattern.length() == 1) {

                            return aminoAcidPattern.matches(
                                    Character.toString(peptideSequence.charAt(peptideSequence.length() - 1)),
                                    sequenceMatchingParameters) ? getArray(peptideSequence.length() + 1) : EMPTY;

                        } else if (aminoAcidPattern.length() > 1) {

                            int minIndex = aminoAcidPattern.getMinIndex();
                            int tempStart = peptideSequence.length() + minIndex;

                            if (tempStart > 0) {
                                return aminoAcidPattern.matchesAt(
                                        peptideSequence, sequenceMatchingParameters,
                                        peptideSequence.length() - 1) ? getArray(peptideSequence.length() + 1) : EMPTY;
                            }

                            for (String accession : accessions) {

                                String sequence = sequenceProvider.getSequence(accession);
                                int tempStartProtein = sequence.length() - aminoAcidPattern.length() - 1;

                                if (tempStartProtein >= 0) {

                                    String subSequence = sequence.substring(tempStartProtein, sequence.length());

                                    if (aminoAcidPattern.matches(subSequence, sequenceMatchingParameters)) {

                                        return getArray(peptideSequence.length() + 1);

                                    }
                                }
                            }

                        } else {

                            throw new IllegalArgumentException(
                                    "No pattern set for modification "
                                    + modification.getName() + ".");

                        }
                    }

                    return EMPTY;

                }

                default:
                    throw new UnsupportedOperationException(
                            "Modification mapping not supported for modification of type "
                            + modificationType + ".");
            }
        }
    }

    /**
     * Returns an array of the possible modification sites for the given
     * modification on the given peptide. N-term modifications are at index 0,
     * C-term at sequence length + 1, and amino acid at 1-based index on the
     * sequence. Protein modifications are not taken into account.
     *
     * @param aminoAcidSequence the amino acid sequence
     * @param nTerm boolean indicating whether the sequence is located at the
     * n-term
     * @param cTerm boolean indicating whether the sequence is located at the
     * c-term
     * @param modification the modification
     * @param sequenceMatchingParameters the sequence matching parameters
     *
     * @return an array of the possible modification sites
     */
    public static int[] getPossibleModificationSites(
            AminoAcidSequence aminoAcidSequence,
            boolean nTerm,
            boolean cTerm,
            Modification modification,
            SequenceMatchingParameters sequenceMatchingParameters
    ) {

        String sequence = aminoAcidSequence.getSequence();
        ModificationType modificationType = modification.getModificationType();

        if (null == modificationType) {

            throw new UnsupportedOperationException(
                    "Modification mapping not supported for modification of type "
                    + modificationType + ".");

        } else {

            switch (modificationType) {

                case modaa: {

                    AminoAcidPattern aminoAcidPattern = modification.getPattern();
                    return aminoAcidPattern.getIndexes(sequence, sequenceMatchingParameters);

                }

                case modnaa_peptide: {

                    if (!nTerm) {
                        return EMPTY;
                    }

                    AminoAcidPattern aminoAcidPattern = modification.getPattern();
                    return aminoAcidPattern.matchesAt(sequence, sequenceMatchingParameters, 0) ? ZERO : EMPTY;

                }

                case modn_protein:

                    return EMPTY;

                case modn_peptide:

                    return nTerm ? ZERO : EMPTY;

                case modnaa_protein:

                    return EMPTY;

                case modc_peptide:

                    return cTerm ? getArray(sequence.length() + 1) : EMPTY;

                case modc_protein:

                    return EMPTY;

                case modcaa_peptide: {

                    if (!cTerm) {
                        return EMPTY;
                    }

                    AminoAcidPattern aminoAcidPattern = modification.getPattern();
                    return aminoAcidPattern.matchesAt(sequence, sequenceMatchingParameters,
                            sequence.length() - 1) ? getArray(sequence.length() + 1) : EMPTY;

                }

                case modcaa_protein:

                    return EMPTY;

                default:
                    throw new UnsupportedOperationException(
                            "Modification mapping not supported for modification of type "
                            + modificationType + ".");
            }
        }
    }

    /**
     * Returns the modified sequence as a tagged string with potential
     * modification sites color coded or with PTM tags, e.g, &lt;mox&gt;. /!\
     * This method will work only if the PTM found in the peptide are in the
     * PTMFactory. Modifications should be provided indexed by site as follows:
     * N-term modifications are at index 0, C-term at sequence length + 1, and
     * amino acid at 1-based index on the sequence.
     *
     * @param modificationProfile the modification profile of the search
     * @param sequence the amino acid sequence to annotate
     * @param confidentModificationSites the confidently localized variable
     * modification sites in a map: aa number &gt; list of modifications (1 is
     * the first AA) (can be null)
     * @param representativeAmbiguousModificationSites the representative site
     * of the ambiguously localized variable modifications in a map: aa number
     * &gt; list of modifications (1 is the first AA) (can be null)
     * @param secondaryAmbiguousModificationSites the secondary sites of the
     * ambiguously localized variable modifications in a map: aa number &gt;
     * list of modifications (1 is the first AA) (can be null)
     * @param fixedModificationSites the fixed modification sites in a map: aa
     * number &gt; list of modifications (1 is the first AA) (can be null)
     * @param useHtmlColorCoding if true, color coded HTML is used, otherwise
     * PTM tags, e.g, &lt;mox&gt;, are used
     * @param useShortName if true the short names are used in the tags
     *
     * @return the tagged modified sequence as a string
     */
    public static String getTaggedModifiedSequence(
            ModificationParameters modificationProfile,
            String sequence,
            String[] confidentModificationSites,
            String[] representativeAmbiguousModificationSites,
            String[] secondaryAmbiguousModificationSites,
            String[] fixedModificationSites,
            boolean useHtmlColorCoding,
            boolean useShortName
    ) {

        if (confidentModificationSites == null) {
            confidentModificationSites = new String[sequence.length() + 2];
        }

        if (representativeAmbiguousModificationSites == null) {
            representativeAmbiguousModificationSites = new String[sequence.length() + 2];
        }

        if (secondaryAmbiguousModificationSites == null) {
            secondaryAmbiguousModificationSites = new String[sequence.length() + 2];
        }

        if (fixedModificationSites == null) {
            fixedModificationSites = new String[sequence.length() + 2];
        }

        StringBuilder modifiedSequence = new StringBuilder(sequence.length());

        for (int aa = 1; aa <= sequence.length(); aa++) {

            int aaIndex = aa - 1;
            char aminoAcid = sequence.charAt(aaIndex);

            appendTaggedResidue(
                    modifiedSequence,
                    aminoAcid,
                    confidentModificationSites[aa],
                    representativeAmbiguousModificationSites[aa],
                    secondaryAmbiguousModificationSites[aa],
                    fixedModificationSites[aa],
                    modificationProfile,
                    useHtmlColorCoding,
                    useShortName
            );

        }

        return modifiedSequence.toString();
    }

    /**
     * Returns the single residue as a tagged string (HTML color or PTM tag).
     * Modified sites are color coded according to three levels: 1- black
     * foreground, colored background 2- colored foreground, white background 3-
     * colored foreground
     *
     * @param stringBuilder the string builder
     * @param residue the residue to tag
     * @param confidentModification the confident ptm at site
     * @param representativeAmbiguousModification the representative ptm at site
     * @param secondaryAmbiguousModification the secondary ptm at site
     * @param fixedModification the fixed ptm at site
     * @param modificationProfile the modification profile
     * @param useHtmlColorCoding if true, color coded HTML is used, otherwise
     * PTM tags, e.g, &lt;mox&gt;, are used
     * @param useShortName if true the short names are used in the tags
     */
    public static void appendTaggedResidue(
            StringBuilder stringBuilder,
            char residue,
            String confidentModification,
            String representativeAmbiguousModification,
            String secondaryAmbiguousModification,
            String fixedModification,
            ModificationParameters modificationProfile,
            boolean useHtmlColorCoding,
            boolean useShortName
    ) {

        if (confidentModification != null) {

            appendTaggedResidue(
                    stringBuilder,
                    residue,
                    confidentModification,
                    modificationProfile,
                    1,
                    useHtmlColorCoding,
                    useShortName
            );

        } else if (representativeAmbiguousModification != null) {

            appendTaggedResidue(
                    stringBuilder,
                    residue,
                    representativeAmbiguousModification,
                    modificationProfile,
                    2,
                    useHtmlColorCoding,
                    useShortName
            );

        } else if (secondaryAmbiguousModification != null) {

            appendTaggedResidue(
                    stringBuilder,
                    residue,
                    secondaryAmbiguousModification,
                    modificationProfile,
                    3,
                    useHtmlColorCoding,
                    useShortName
            );

        } else if (fixedModification != null) {

            appendTaggedResidue(
                    stringBuilder,
                    residue,
                    fixedModification,
                    modificationProfile,
                    1,
                    useHtmlColorCoding,
                    useShortName
            );

        } else {

            stringBuilder.append(residue);

        }
    }

    /**
     * Appends the single residue as a tagged string (HTML color or PTM tag).
     * Modified sites are color coded according to three levels: 1- black
     * foreground, colored background 2- colored foreground, white background 3-
     * colored foreground
     *
     * @param stringBuilder the string builder
     * @param residue the residue to tag
     * @param modificationName the name of the PTM
     * @param modificationProfile the modification profile
     * @param localizationConfidenceLevel the localization confidence level
     * @param useHtmlColorCoding if true, color coded HTML is used, otherwise
     * PTM tags, e.g, &lt;mox&gt;, are used
     * @param useShortName if true the short names are used in the tags
     */
    public static void appendTaggedResidue(
            StringBuilder stringBuilder,
            char residue,
            String modificationName,
            ModificationParameters modificationProfile,
            int localizationConfidenceLevel,
            boolean useHtmlColorCoding,
            boolean useShortName
    ) {

        ModificationFactory modificationFactory = ModificationFactory.getInstance();
        Modification modification = modificationFactory.getModification(modificationName);

        if (!useHtmlColorCoding) {

            if (localizationConfidenceLevel == 1 || localizationConfidenceLevel == 2) {

                if (useShortName) {

                    stringBuilder.append(residue).append("<").append(modification.getShortName()).append(">");

                } else {

                    stringBuilder.append(residue).append("<").append(modificationName).append(">");

                }

            } else if (localizationConfidenceLevel == 3) {

                stringBuilder.append(residue);

            }

        } else {

            int modificationColor = modificationProfile.getColor(modificationName);

            switch (localizationConfidenceLevel) {
                case 1:
                    stringBuilder
                            .append("<span style=\"color:#")
                            .append(Util.color2Hex(Color.WHITE))
                            .append(";background:#")
                            .append(Util.color2Hex(modificationColor))
                            .append("\">")
                            .append(residue)
                            .append("</span>");
                    break;

                case 2:
                    stringBuilder
                            .append("<span style=\"color:#")
                            .append(Util.color2Hex(modificationColor))
                            .append(";background:#")
                            .append(Util.color2Hex(Color.WHITE))
                            .append("\">")
                            .append(residue)
                            .append("</span>");
                    break;

                case 3:
                    // taggedResidue.append("<span style=\"color:#").append(Util.color2Hex(modificationColor)).append("\">").append(residue).append("</span>");
                    // taggedResidue.append("<span style=\"color:#").append(Util.color2Hex(Color.BLACK)).append(";background:#").append(Util.color2Hex(Color.WHITE)).append("\">").append(residue).append("</span>");
                    stringBuilder.append(residue);
                    break;

                default:
                    throw new IllegalArgumentException(
                            "No formatting implemented for localization confidence level "
                            + localizationConfidenceLevel + ".");
            }
        }
    }

    /**
     * Returns the 1-based index on the peptide.
     *
     * @param index the modification index
     * @param sequenceLength the sequence length
     *
     * @return the 1-based index on the sequence
     */
    public static int getSite(
            int index, 
            int sequenceLength
    ) {

        if (index > 0 && index < sequenceLength + 1) {

            return index;

        } else if (index == 0) {

            return 1;

        } else {

            return index - 1;

        }
    }

    /**
     * Returns a set of the names of all modifications found on a peptide.
     *
     * @param peptide the peptide
     * @param modificationParameters the modification parameters
     * @param sequenceProvider the protein sequence provider
     * @param sequenceMatchingParameters the sequence matching parameters
     *
     * @return a set of the names of all modifications found on a peptide
     */
    public static HashSet<String> getAllModifications(
            Peptide peptide,
            ModificationParameters modificationParameters,
            SequenceProvider sequenceProvider,
            SequenceMatchingParameters sequenceMatchingParameters
    ) {

        HashSet<String> modNames = Arrays.stream(peptide.getVariableModifications())
                .map(
                        ModificationMatch::getModification
                )
                .collect(
                        Collectors.toCollection(HashSet::new)
                );

        String[] fixedModifications = peptide.getFixedModifications(
                modificationParameters, sequenceProvider, sequenceMatchingParameters);

        modNames.addAll(
                Arrays.stream(fixedModifications)
                .filter(
                        modName -> modName != null
                )
                .collect(
                        Collectors.toSet()
                )
        );

        return modNames;
    }

    /**
     * Returns a set of the names of all modifications found on a tag.
     *
     * @param tag the tag
     * @param modificationParameters the modification parameters
     * @param sequenceMatchingParameters the sequence matching parameters
     *
     * @return a set of the names of all modifications found on a tag
     */
    public static HashSet<String> getAllModifications(
            Tag tag,
            ModificationParameters modificationParameters,
            SequenceMatchingParameters sequenceMatchingParameters
    ) {

        HashSet<String> modNames = new HashSet<>(2);
        ArrayList<TagComponent> tagComponents = tag.getContent();

        for (int i = 0; i < tagComponents.size(); i++) {

            TagComponent tagComponent = tagComponents.get(i);

            if (tagComponent instanceof AminoAcidSequence) {

                AminoAcidSequence aminoAcidSequence = (AminoAcidSequence) tagComponent;

                modNames.addAll(
                        Arrays.stream(aminoAcidSequence.getVariableModifications())
                        .map(
                                ModificationMatch::getModification
                        )
                        .collect(
                                Collectors.toCollection(HashSet::new)
                        )
                );

                String[] fixedModifications
                        = aminoAcidSequence.getFixedModifications(
                                i == 0, i == tagComponents.size() - 1,
                                modificationParameters, sequenceMatchingParameters);
                modNames.addAll(Arrays.stream(fixedModifications)
                        .filter(modName -> modName != null)
                        .collect(Collectors.toSet()));

            }
        }

        return modNames;
    }

    /**
     * Returns the expected modifications for a given modification mass indexed
     * by site.
     *
     * @param modMass the modification mass
     * @param modificationParameters the modification parameters
     * @param peptide the peptide where to map the modification
     * @param massTolerance the mass tolerance to use
     * @param sequenceProvider a sequence provider
     * @param sequenceMatchingParameters the sequence matching parameters
     * @param searchParameters the search parameters
     *
     * @return the expected modifications for a given modification mass indexed
     * by site
     */
    public static HashMap<Integer, HashSet<String>> getExpectedModifications(
            double modMass,
            ModificationParameters modificationParameters,
            Peptide peptide,
            double massTolerance,
            SequenceProvider sequenceProvider,
            SequenceMatchingParameters sequenceMatchingParameters,
            SearchParameters searchParameters
    ) {

        HashMap<Integer, HashSet<String>> results = new HashMap<>(1);
        ModificationFactory modificationFactory = ModificationFactory.getInstance();

        for (String possibleModName : modificationFactory.getExpectedVariableModifications(searchParameters)) {

            Modification possibleModification = modificationFactory.getModification(possibleModName);

            if (Math.abs(modMass - possibleModification.getMass()) < massTolerance) {

                int[] possibleSites = getPossibleModificationSites(
                        peptide,
                        possibleModification,
                        sequenceProvider,
                        sequenceMatchingParameters
                );

                for (int site : possibleSites) {

                    HashSet<String> modifications = results.get(site);

                    if (modifications == null) {
                        modifications = new HashSet<>(1);
                        results.put(site, modifications);
                    }

                    modifications.add(possibleModName);
                }
            }
        }

        return results;
    }
}
