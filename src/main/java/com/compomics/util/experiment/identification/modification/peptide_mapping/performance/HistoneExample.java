package com.compomics.util.experiment.identification.modification.peptide_mapping.performance;

import com.compomics.util.io.flat.SimpleFileWriter;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.TreeSet;

/**
 * Convenience class extracting the data needed to plot the modification
 * matching.
 *
 * @author Marc Vaudel
 */
public class HistoneExample {

    private final static String GROUND_TRUTH_SEQUENCE = "ARTKQTARKSTGGKAPRKQLATKAARKSAPATGGVKKPHRYRPGTVALRE";

    private final static SimpleFileWriter WRITER_OCCURRENCE = new SimpleFileWriter(new File("/home/marc/Github/papers/peptides-modifications-matching/histone/modification_occurrence.gz"), true);
    private final static SimpleFileWriter WRITER_WEIGHTS = new SimpleFileWriter(new File("/home/marc/Github/papers/peptides-modifications-matching/histone/modification_weight.gz"), true);

    private static boolean header_written = false;

    private static int n = 1;

    public static void exportHistoneData(
            String sequence,
            HashMap<Double, int[]> modificationToPossibleSiteMap,
            HashMap<Double, Integer> modificationOccurrenceMap,
            HashMap<Double, HashMap<Integer, Double>> modificationToSiteToScore,
            HashMap<Double, TreeSet<Integer>> mapping,
            double amandaScore
    ) {

        if (sequence.equals(GROUND_TRUTH_SEQUENCE)) {

            if (!header_written) {

                WRITER_OCCURRENCE.writeLine("psm", "modification", "occurrence", "amanda_score");
                WRITER_WEIGHTS.writeLine("psm", "modification", "site", "weight", "selected");

                header_written = true;

            }

            for (Entry<Double, Integer> entry : modificationOccurrenceMap.entrySet()) {

                WRITER_OCCURRENCE.writeLine(Integer.toString(n), Double.toString(entry.getKey()), Integer.toString(entry.getValue()), Double.toString(amandaScore));

            }

            for (Entry<Double, int[]> entry : modificationToPossibleSiteMap.entrySet()) {

                double modMass = entry.getKey();
                HashMap<Integer, Double> scores = modificationToSiteToScore.get(modMass);
                HashSet<Integer> mappedSites = new HashSet<>(mapping.get(modMass));

                for (int site : entry.getValue()) {

                    Double score = scores.get(site);

                    if (score == null) {

                        score = 0.0;

                    }

                    String mapped = mappedSites.contains(site) ? "1" : "0";

                    WRITER_WEIGHTS.writeLine(Integer.toString(n), Double.toString(modMass), Integer.toString(site), Double.toString(score), mapped);

                }
            }

            n++;

        }
    }

    public static void close() {

        WRITER_OCCURRENCE.close();
        WRITER_WEIGHTS.close();

    }

}
