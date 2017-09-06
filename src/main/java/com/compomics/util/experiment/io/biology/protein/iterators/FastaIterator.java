package com.compomics.util.experiment.io.biology.protein.iterators;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.proteins.Protein;
import com.compomics.util.experiment.io.biology.protein.ProteinIterator;
import com.compomics.util.experiment.io.biology.protein.Header;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.concurrent.Semaphore;

/**
 * Iterator for a fasta file.
 *
 * @author Marc Vaudel
 */
public class FastaIterator implements ProteinIterator {

    /**
     * Mutex for the buffering of the fasta file.
     */
    private final Semaphore bufferingMutex = new Semaphore(1);
    /**
     * Character forbidden in protein sequences, will be removed.
     */
    public static final char forbiddenCharacter = '*';
    /**
     * Buffered reader for the fasta file.
     */
    private final BufferedReader br;
    /**
     * Boolean indicating whether sanity check should be conducted on the
     * protein sequences. If true, the iterator will remove forbidden
     * characters, verify that the sequence is upper case, and that all amino
     * acids are recognized.
     */
    private final boolean sanityCheck;
    /**
     * Placeholder for the next header found during parsing.
     */
    private Header nextHeader = null;
    /**
     * The header corresponding to the last protein returned.
     */
    private Header lastHeader = null;
    /**
     * Boolean indicating whether the end of the file has been reached.
     */
    private boolean endOfFileReached = false;

    /**
     * Constructor without sanity check.
     *
     * @param fastaFile the fasta file
     *
     * @throws FileNotFoundException exception thrown if the file could not be
     * found
     */
    public FastaIterator(File fastaFile) throws FileNotFoundException {

        this(fastaFile, false);

    }

    /**
     * Constructor.
     *
     * @param fastaFile the fasta file
     * @param sanityCheck boolean indicating whether sanity check should be
     * conducted
     *
     * @throws FileNotFoundException exception thrown if the file could not be
     * found
     */
    public FastaIterator(File fastaFile, boolean sanityCheck) throws FileNotFoundException {

        br = new BufferedReader(new FileReader(fastaFile));
        this.sanityCheck = sanityCheck;

    }

    @Override
    public Protein getNextProtein() {

        try {

            bufferingMutex.acquire();

            if (endOfFileReached) {

                return null;

            }

            Header header = nextHeader;
            StringBuilder sequenceBuilder = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {

                line = line.trim();

                if (line.length() > 0) {
                    if (line.charAt(0) == '>') {

                        nextHeader = Header.parseFromFASTA(line);

                        if (header != null) {

                            break;

                        }

                        header = nextHeader;

                    } else {

                        sequenceBuilder.append(line);

                    }
                }
            }

            if (nextHeader == null) {

                br.close();
                endOfFileReached = true;

            }

            bufferingMutex.release();

            String sequence = sequenceBuilder.toString();

            if (sanityCheck) {

                sequence = getCleanedSequence(sequence);

            }

            if (sequence.length() > 0) {
                
                lastHeader = header;

                return new Protein(header.getAccession(), sequence);

            } else if (header == null) {

                return null;

            } else {

                throw new IllegalArgumentException("No sequence found for protein accession " + header.getAccession() + ".");

            }

        } catch (Exception e) {

            throw new RuntimeException(e);

        }
    }

    /**
     * Removes forbidden characters from the file, verifies that amino acids can be parsed, makes all characters upper case.
     * 
     * @param sequence the original sequence
     * 
     * @return the cleaned sequence
     */
    private String getCleanedSequence(String sequence) {

        char[] lineAsCharArray = sequence.toCharArray();
        StringBuilder cleanedSequence = new StringBuilder(sequence.length());

        for (int i = 0; i < lineAsCharArray.length; i++) {

            char charAtI = lineAsCharArray[i];

            if (charAtI == forbiddenCharacter) {

                continue;

            }

            char upperCase = Character.toUpperCase(charAtI);
            AminoAcid.getAminoAcid(upperCase);
            cleanedSequence.append(upperCase);

        }

        return cleanedSequence.toString();

    }

    /**
     * Returns the header corresponding to the last protein. 
     * 
     * @return the header corresponding to the last protein
     */
    public Header getLastHeader() {
        return lastHeader;
    }
    
    /**
     * Closes the iterator.
     */
    public void close() {
        
        try {
            
            br.close();
            
        } catch (Exception e) {
            
            e.printStackTrace();
            
        }
    }
    
}
