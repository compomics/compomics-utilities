package com.compomics.util.experiment.io.biology.protein.iterators;

import com.compomics.util.experiment.biology.aminoacids.AminoAcid;
import com.compomics.util.experiment.biology.proteins.Protein;
import com.compomics.util.experiment.io.biology.protein.ProteinIterator;
import com.compomics.util.experiment.io.biology.protein.Header;
import static com.compomics.util.io.IoUtil.ENCODING;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.Semaphore;
import org.apache.commons.io.input.CountingInputStream;

/**
 * Iterator for a FASTA file.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class FastaIterator implements ProteinIterator {

    /**
     * Mutex for the buffering of the FASTA file.
     */
    private final Semaphore bufferingMutex = new Semaphore(1);
    /**
     * Character forbidden in protein sequences, will be removed.
     */
    public static final char FORBIDDEN_CHARACTER = '*';
    /**
     * Buffered reader for the FASTA file.
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
     * The length of the file in bytes.
     */
    private final long fileLength;
    /**
     * A stream counting the bytes read.
     */
    private final CountingInputStream countingInputStream;

    /**
     * Constructor without sanity check.
     *
     * @param fastaFile the FASTA file
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
     * @param fastaFile the FASTA file
     * @param sanityCheck boolean indicating whether sanity check should be
     * conducted
     *
     * @throws FileNotFoundException exception thrown if the file could not be
     * found
     */
    public FastaIterator(File fastaFile, boolean sanityCheck) throws FileNotFoundException {

        try {

            InputStream fileStream = new FileInputStream(fastaFile);
            countingInputStream = new CountingInputStream(fileStream);
            Reader reader = new InputStreamReader(countingInputStream, ENCODING);
            br = new BufferedReader(reader);

            fileLength = fastaFile.length();
            this.sanityCheck = sanityCheck;

        } catch (IOException e) {

            throw new RuntimeException(e);

        }

    }

    /**
     * Returns the progress reading the file in percent.
     *
     * @return The progress reading the file in percent.
     */
    public double getProgressInPercent() {

        return 100.0 * ((double) countingInputStream.getByteCount()) / fileLength;

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

            if (line == null) {

                br.close();
                endOfFileReached = true;

            }

            bufferingMutex.release();

            String sequence = sequenceBuilder.toString();
            if (sanityCheck) {

                sequence = getCleanedSequence(sequence);

            }

            if (sequence.length() > 0) {

                if (header == null) {
                    throw new RuntimeException("No header information found in the fasta file.");
                }

                lastHeader = header;
                return new Protein(header.getAccessionOrRest(), sequence);

            } else if (header == null) {

                return null;

            } else {

                throw new IllegalArgumentException(
                        "No sequence found for protein accession "
                        + header.getAccessionOrRest()
                        + "."
                );

            }

        } catch (Exception e) {
            throw new RuntimeException("An error occurred in the FASTA file\n\n" + e);
        }
    }

    /**
     * Removes forbidden characters from the file, verifies that amino acids can
     * be parsed, makes all characters upper case.
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

            if (charAtI != FORBIDDEN_CHARACTER) {

                char upperCase = Character.toUpperCase(charAtI);
                AminoAcid.getAminoAcid(upperCase);
                cleanedSequence.append(upperCase);

            }
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
