package com.compomics.scripts_marc;

import com.compomics.util.experiment.biology.ions.impl.ElementaryIon;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.io.flat.SimpleFileWriter;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.parameters.identification.search.ModificationParameters;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 *
 * @author Marc Vaudel
 */
public class TrypsinCleavage {

    public final static double minMz = 300;
    public final static double maxMz = 1100;

    /**
     * Main method.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        ModificationParameters modificationParameters = new ModificationParameters();

        try {

            File inputFolder = new File("C:\\Github\\nfr2019\\resources\\mody\\prediction");
            File outputFile = new File("C:\\Github\\nfr2019\\resources\\mody\\predicted_peptides.txt");

            try (SimpleFileWriter writer = new SimpleFileWriter(outputFile, false)) {

                writer.writeLine("protein", "sequence", "charge", "mz", "start", "stop", "p");

                for (File inputFile : inputFolder.listFiles()) {

                    String fileName = inputFile.getName();
                    String protein = fileName.substring(0, fileName.length() - 4);

                    try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {

                        String line;
                        while ((line = reader.readLine()) != null) {

                            String[] lineSplit = line.split(" ");

                            double p = Double.parseDouble(lineSplit[0].substring(0, lineSplit[0].length() - 1));
                            String sequence = lineSplit[1];
                            int start = Integer.parseInt(lineSplit[2].substring(1, lineSplit[2].length() - 1));
                            int stop = Integer.parseInt(lineSplit[3].substring(0, lineSplit[3].length() - 1));

                            Peptide peptide = new Peptide(sequence);
                            peptide.estimateTheoreticMass(modificationParameters, null, SequenceMatchingParameters.defaultStringMatching);

                            for (int charge = 2; charge <= 4; charge++) {

                                double mz = (peptide.getMass() + ElementaryIon.getProtonMassMultiple(charge)) / charge;

                                if (mz >= minMz && mz <= maxMz) {

                                    writer.writeLine(
                                            protein,
                                            sequence,
                                            Integer.toString(charge),
                                            Double.toString(mz),
                                            Integer.toString(start),
                                            Integer.toString(stop),
                                            Double.toString(p)
                                    );

                                    break;
                                    
                                }
                            }
                        }
                    }
                }
            }

        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
