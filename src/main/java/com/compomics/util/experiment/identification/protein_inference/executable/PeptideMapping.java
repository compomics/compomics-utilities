package com.compomics.util.experiment.identification.protein_inference.executable;

import com.compomics.util.experiment.biology.AminoAcidSequence;
import com.compomics.util.experiment.biology.MassGap;
import com.compomics.util.experiment.identification.amino_acid_tags.Tag;
import com.compomics.util.experiment.identification.amino_acid_tags.TagComponent;
import com.compomics.util.experiment.identification.identification_parameters.PtmSettings;
import com.compomics.util.experiment.identification.identification_parameters.SearchParameters;
import com.compomics.util.experiment.identification.protein_inference.PeptideMapper;
import com.compomics.util.experiment.identification.protein_inference.PeptideMapperType;
import com.compomics.util.experiment.identification.protein_inference.PeptideProteinMapping;
import com.compomics.util.experiment.identification.protein_inference.fm_index.FMIndex;
import com.compomics.util.experiment.identification.protein_sequences.SequenceFactory;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import com.compomics.util.preferences.IdentificationParameters;
import com.compomics.util.preferences.PeptideVariantsPreferences;
import com.compomics.util.preferences.SequenceMatchingPreferences;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Command line peptide mapping.
 *
 * @author Dominik Kopczynski
 */
public class PeptideMapping {

    /**
     * Main class.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if ((args.length > 0 && (args[0].equals("-h") || args[0].equals("--help"))) || args.length < 4 || (!args[0].equals("-p") && !args[0].equals("-t"))) {
            System.err.println("PeptideMapping: a tool to map peptides or sequence tags against a given proteome.");
            System.err.println("usage: PeptideMapping -[p|t] input-fasta input-peptide/tag-csv output-csv [utilities-parameter-file]");
            System.err.println();
            System.err.println("Options are:");
            System.err.println("\t-p\tpeptide mapping");
            System.err.println("\t-t\tsequence tag mapping");
            System.err.println("\t-h\tprint this info");
            System.err.println();
            System.err.println("Default parameters:");
            System.err.println("\tindexing method:\t\tfm-index");
            System.err.println("\tframentation tolerance [Da]:\t0.02");

            System.exit(-1);
        }

        PeptideMapperType peptideMapperType = PeptideMapperType.fm_index;
        System.err.println("Start reading FASTA file");
        WaitingHandlerCLIImpl waitingHandlerCLIImpl = new WaitingHandlerCLIImpl();
        File sequences = new File(args[1]);
        SequenceFactory sequenceFactory = SequenceFactory.getInstance();
        try {
            sequenceFactory.loadFastaFile(sequences, waitingHandlerCLIImpl);
        } catch (Exception e) {
            System.err.println("Error: cound not open FASTA file");
            System.exit(-1);
        }

        SearchParameters searchParameters = null;
        PeptideVariantsPreferences peptideVariantsPreferences = null;
        SequenceMatchingPreferences sequenceMatchingPreferences = null;
        if (args.length >= 5) {
            File parameterFile = new File(args[4]);
            IdentificationParameters identificationParameters = null;
            try {
                identificationParameters = IdentificationParameters.getIdentificationParameters(parameterFile);
            } catch (Exception e) {
                System.err.println("Error: cound not open / parse parameter file");
                System.exit(-1);
            }

            if (peptideMapperType != identificationParameters.getSequenceMatchingPreferences().getPeptideMapperType()) {
                peptideMapperType = identificationParameters.getSequenceMatchingPreferences().getPeptideMapperType();
                System.err.println("New mapping index: " + peptideMapperType.name);
            }
            peptideVariantsPreferences = PeptideVariantsPreferences.getNoVariantPreferences();
            sequenceMatchingPreferences = identificationParameters.getSequenceMatchingPreferences();
            searchParameters = identificationParameters.getSearchParameters();

        } else {
            peptideVariantsPreferences = PeptideVariantsPreferences.getNoVariantPreferences();
            searchParameters = new SearchParameters();
            searchParameters.setPtmSettings(new PtmSettings());
            searchParameters.setFragmentIonAccuracy(0.02);
            searchParameters.setFragmentAccuracyType(SearchParameters.MassAccuracyType.DA);
            sequenceMatchingPreferences = new SequenceMatchingPreferences();
            sequenceMatchingPreferences.setSequenceMatchingType(SequenceMatchingPreferences.MatchingType.indistiguishableAminoAcids);
            sequenceMatchingPreferences.setLimitX(0.25);
        }

        System.err.println("Start indexing proteome");
        long startTimeIndex = System.nanoTime();
        PeptideMapper peptideMapper = null;
        if (peptideMapperType == PeptideMapperType.fm_index) {
            peptideMapper = new FMIndex(waitingHandlerCLIImpl, true, peptideVariantsPreferences, searchParameters);
        } else {
            System.err.println("No other peptide mapping supported than FM index, please change settings in setting file.");
            System.exit(-1);
        }
        double diffTimeIndex = System.nanoTime() - startTimeIndex;
        System.err.println();
        if (peptideMapperType == PeptideMapperType.fm_index) {
            System.err.println("Indexing took " + (diffTimeIndex / 1e9) + " seconds and consumes " + (((float) ((FMIndex) peptideMapper).getAllocatedBytes()) / 1e6) + " MB");
        } else {
            System.err.println("Indexing took " + (diffTimeIndex / 1e9) + " seconds");
        }

        if (args[0].equals("-p")) {
            ArrayList<String> peptides = new ArrayList<>();
            try {
                String line = "";
                BufferedReader br = new BufferedReader(new FileReader(args[2]));
                while ((line = br.readLine()) != null) {
                    if (!Pattern.matches("[a-zA-Z]+", line)) {
                        System.err.println("Error: invalid character in line '" + line + "'");
                        System.exit(-1);
                    }
                    peptides.add(line.toUpperCase());
                }
                br.close();
            } catch (Exception e) {
                System.err.println("Error: cound not open input list");
                System.exit(-1);
            }
            waitingHandlerCLIImpl.setSecondaryProgressCounterIndeterminate(false);
            waitingHandlerCLIImpl.setMaxSecondaryProgressCounter(peptides.size());
            waitingHandlerCLIImpl.setSecondaryProgressCounter(0);
            ArrayList<PeptideProteinMapping> allPeptideProteinMappings = new ArrayList<>();

            // starting the mapping
            try {
                long startTimeMapping = System.nanoTime();
                for (int i = 0; i < peptides.size(); ++i) {
                    String peptide = peptides.get(i);
                    waitingHandlerCLIImpl.increaseSecondaryProgressCounter();
                    ArrayList<PeptideProteinMapping> peptideProteinMappings = peptideMapper.getProteinMapping(peptide, sequenceMatchingPreferences);
                    allPeptideProteinMappings.addAll(peptideProteinMappings);
                }
                long diffTimeMapping = System.nanoTime() - startTimeMapping;
                System.err.println();
                System.err.println("Mapping " + peptides.size() + " peptides took " + (diffTimeMapping / 1e9) + " seconds");
            } catch (Exception e) {
                System.err.println("Error: mapping went wrong for unknown reasons");
                System.exit(-1);
            }

            try {
                PrintWriter writer = new PrintWriter(args[3], "UTF-8");
                for (PeptideProteinMapping peptideProteinMapping : allPeptideProteinMappings) {
                    String peptide = peptideProteinMapping.getPeptideSequence();
                    String accession = peptideProteinMapping.getProteinAccession();
                    int startIndex = peptideProteinMapping.getIndex();
                    writer.println(peptide + "," + accession + "," + startIndex);
                }
                writer.close();
            } catch (Exception e) {
                System.err.println("Error: could not write into file '" + args[3] + "'");
                System.exit(-1);
            }
        } else {
            ArrayList<Tag> tags = new ArrayList<>();
            ArrayList<Integer> tagIndexes = new ArrayList<>();
            try {
                String line = "";
                BufferedReader br = new BufferedReader(new FileReader(args[2]));
                while ((line = br.readLine()) != null) {
                    Tag tag = new Tag();
                    for (String part : line.split(",")) {

                        if (Pattern.matches("[a-zA-Z]+", part)) {
                            tag.addAminoAcidSequence(new AminoAcidSequence(part));
                        } else {
                            try {
                                double mass = Double.parseDouble(part);
                                tag.addMassGap(mass);
                            } catch (NumberFormatException e) {
                                System.err.println("Error: line contains no valid tag: '" + line + "'");
                                System.exit(-1);
                            }
                        }
                    }
                    tags.add(tag);
                }
            } catch (Exception e) {
                System.err.println("Error: cound not open input list");
                System.exit(-1);
            }

            waitingHandlerCLIImpl.setSecondaryProgressCounterIndeterminate(false);
            waitingHandlerCLIImpl.setMaxSecondaryProgressCounter(tags.size());
            waitingHandlerCLIImpl.setSecondaryProgressCounter(0);
            ArrayList<PeptideProteinMapping> allPeptideProteinMappings = new ArrayList<>();

            // starting the mapping
            try {
                // setting up modifications lists, only relevant for protein tree
                ArrayList<String> variableModifications = searchParameters.getPtmSettings().getVariableModifications();
                ArrayList<String> fixedModifications = searchParameters.getPtmSettings().getFixedModifications();

                long startTimeMapping = System.nanoTime();
                for (int i = 0; i < tags.size(); ++i) {
                    waitingHandlerCLIImpl.increaseSecondaryProgressCounter();
                    ArrayList<PeptideProteinMapping> peptideProteinMappings = peptideMapper.getProteinMapping(tags.get(i), sequenceMatchingPreferences, searchParameters.getFragmentIonAccuracyInDaltons());
                    allPeptideProteinMappings.addAll(peptideProteinMappings);
                    for (int j = 0; j < peptideProteinMappings.size(); ++j) {
                        tagIndexes.add(i);
                    }
                }
                long diffTimeMapping = System.nanoTime() - startTimeMapping;
                System.err.println();
                System.err.println("Mapping " + tags.size() + " tags took " + (diffTimeMapping / 1e9) + " seconds");
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error: an unexpected error happened.");
                System.exit(-1);
            }

            try {
                PrintWriter writer = new PrintWriter(args[3], "UTF-8");
                for (int i = 0; i < allPeptideProteinMappings.size(); ++i) {
                    PeptideProteinMapping peptideProteinMapping = allPeptideProteinMappings.get(i);
                    String peptide = peptideProteinMapping.getPeptideSequence();
                    String accession = peptideProteinMapping.getProteinAccession();
                    int startIndex = peptideProteinMapping.getIndex();
                    for (TagComponent tagComponent : tags.get(tagIndexes.get(i)).getContent()) {
                        if (tagComponent instanceof MassGap) {
                            writer.print(tagComponent.getMass());
                        }
                        if (tagComponent instanceof AminoAcidSequence) {
                            writer.print(tagComponent.asSequence());
                        }
                        writer.print(",");
                    }
                    writer.println(peptide + "," + accession + "," + startIndex);
                }
                writer.close();
            } catch (Exception e) {
                System.err.println("Error: could not write into file '" + args[3] + "'");
                System.exit(-1);
            }
        }
    }
}
