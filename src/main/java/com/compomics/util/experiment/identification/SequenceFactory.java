
package com.compomics.util.experiment.identification;

import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.gui.dialogs.ProgressDialogX;
import com.compomics.util.protein.Header;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;

/**
 * factory retrieving the information of the loaded fasta file
 *
 * @author marc
 */
public class SequenceFactory {

    private static SequenceFactory instance = null;
    private Header currentHeader = null;
    private Protein currentProtein = null;
    private FastaIndex fastaIndex;
    private RandomAccessFile currentFastaFile;
    /**
     * Recognized flags for a decoy protein
     */
    public static final String[] decoyFlags = {"REVERSED", "REV", "RND"};

    private SequenceFactory() {
    }

    public static SequenceFactory getInstance() {
        if (instance == null) {
            instance = new SequenceFactory();
        }
        return instance;
    }

    public Protein getProtein(String accession) throws IOException {
        if (currentProtein == null || !currentProtein.getAccession().equals(accession)) {
            long index = fastaIndex.getIndex(accession);
            currentFastaFile.seek(index);
            String line, sequence = "";
            while ((line = currentFastaFile.readLine()) != null) {
                line = line.trim();

                if (line.startsWith(">")) {
                    if (!sequence.equals("")) {
                        break;
                    }
                    currentHeader = Header.parseFromFASTA(line);
                } else {
                    sequence += line;
                }
            }
            currentProtein = new Protein(accession, currentHeader.getDatabaseType(), sequence, isDecoy(accession));
        }
        return currentProtein;
    }

    public Header getHeader(String accession) throws IOException {
        if (currentHeader == null || !currentHeader.getAccession().equals(accession)) {
            long index = fastaIndex.getIndex(accession);
            currentFastaFile.seek(index);
            currentHeader = Header.parseFromFASTA(currentFastaFile.readLine());
        }
        return currentHeader;
    }

    public void loadFastaFile(File fastaFile) throws FileNotFoundException, IOException, ClassNotFoundException {
        currentFastaFile = new RandomAccessFile(fastaFile, "r");
        fastaIndex = getFastaIndex(fastaFile);
    }

    public FastaIndex getFastaIndex(File fastaFile) throws FileNotFoundException, IOException, ClassNotFoundException {
        File indexFile = new File(fastaFile.getParent(), fastaFile.getName() + ".cui");
        if (indexFile.exists()) {
            return getIndex(indexFile);
        } else {
            FastaIndex tempFastaIndex = createFastaIndex(fastaFile);
            writeIndex(tempFastaIndex, fastaFile.getParentFile());
            return tempFastaIndex;
        }
    }

    public void writeIndex(FastaIndex fastaIndex, File directory) throws IOException {
        // Serialize the file index as compomics utilities index
        FileOutputStream fos = new FileOutputStream(new File(directory, fastaIndex.getFileName() + ".cui"));
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(fastaIndex);
        oos.close();
    }

    public FastaIndex getIndex(File fastaIndex) throws FileNotFoundException, IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(fastaIndex);
        ObjectInputStream in = new ObjectInputStream(fis);
        FastaIndex index = (FastaIndex) in.readObject();
        in.close();
        return index;
    }

    public void closeFile() throws IOException {
        currentFastaFile.close();
    }

    public static FastaIndex createFastaIndex(File fastaFile) throws FileNotFoundException, IOException {
        HashMap<String, Long> indexes = new HashMap<String, Long>();

        RandomAccessFile randomAccessFile = new RandomAccessFile(fastaFile, "r");
        String line, accession = "";
        boolean decoy = false;
        int nTarget = 0;
        Header fastaHeader;
        long index = randomAccessFile.getFilePointer();

        while ((line = randomAccessFile.readLine()) != null) {
            if (line.startsWith(">")) {
                fastaHeader = Header.parseFromFASTA(line);
                accession = fastaHeader.getAccession();
                indexes.put(accession, index);
                if (!isDecoy(accession)) {
                    nTarget++;
                } else if (!decoy) {
                    decoy = true;
                }
            } else {
                index = randomAccessFile.getFilePointer();
            }
        }

        randomAccessFile.close();

        return new FastaIndex(indexes, fastaFile.getName(), decoy, nTarget);
    }

    /**
     * Returns a boolean indicating whether a protein is decoy or not based on the protein accession. 
     * Recognized flags for decoy proteins are listed as a static field.
     * 
     * @param proteinAccession  The accession of the protein
     * @return a boolean indicating whether the protein is Decoy.
     */
    public static boolean isDecoy(String proteinAccession) {
        for (String flag : decoyFlags) {
            if (proteinAccession.contains(flag)) {
                return true;
            }
        }
        return false;
    }

    public boolean concatenatedTargetDecoy() {
        return fastaIndex.isDecoy();
    }

    public int getNTargetSequences() {
        return fastaIndex.getNTarget();
    }

    public void appendDecoySequences(File destinationFile) throws IOException {
        appendDecoySequences(destinationFile, null);
    }

    public void appendDecoySequences(File destinationFile, ProgressDialogX progressDialog) throws IOException {

        if (progressDialog != null) {
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(fastaIndex.getNTarget());
        }

        BufferedWriter proteinWriter = new BufferedWriter(new FileWriter(destinationFile));

        int counter = 1;
        Header decoyHeader;
        String decoySequence;

        for (String accession : fastaIndex.getIndexes().keySet()) {

            if (progressDialog != null) {
                progressDialog.setValue(counter++);
            }

            getProtein(accession);

            decoyHeader = Header.parseFromFASTA(currentHeader.toString());
            decoyHeader.setAccession(decoyHeader.getAccession() + "_" + decoyFlags[0]);
            decoyHeader.setDescription(decoyHeader.getDescription() + "-" + decoyFlags[0]);
            decoySequence = reverseSequence(currentProtein.getSequence());

            proteinWriter.write(currentHeader.toString() + "\n");
            proteinWriter.write(currentProtein.getSequence() + "\n");
            proteinWriter.write(decoyHeader.toString() + "\n");
            proteinWriter.write(decoySequence + "\n");

        }

        if (progressDialog != null) {
            progressDialog.setIndeterminate(true);
        }

        proteinWriter.close();
    }

    /**
     * Reverses a protein sequence.
     * 
     * @param sequence the protein sequence
     * @return the reversed protein sequence
     */
    private String reverseSequence(String sequence) {
        return new StringBuilder(sequence).reverse().toString();
    }
}
