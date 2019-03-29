package com.compomics.util.experiment.io.biology.protein;

import com.compomics.util.db.object.DbObject;
import com.compomics.util.experiment.io.biology.protein.iterators.HeaderIterator;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.IOException;

/**
 * The parameters used to parse a FASTA file.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public class FastaParameters extends DbObject {

    /**
     * The version UID for serialization/deserialization compatibility.
     */
    //static final long serialVersionUID = -7453836034514062470L;
    /**
     * The decoy flags used to infer the FASTA parameters.
     */
    public static final String[] DECOY_FLAGS = {"decoy", "random", "reversed", "rev"};
    /**
     * The decoy separators used to infer the FASTA parameters.
     */
    public static final char[] DECOY_SEPARATORS = {'-', '.', '_'};
    /**
     * Boolean indicating whether the FASTA file should be processed as
     * target-decoy or only target.
     */
    private boolean targetDecoy = true;
    /**
     * The flag for decoy proteins in the accession.
     */
    private String decoyFlag = "-REVERSED";
    /**
     * Boolean indicating whether the decoy flag is a suffix or a prefix.
     */
    private boolean decoySuffix = true;
    /**
     * The tag added after adding decoy sequences to a FASTA file.
     */
    private String targetDecoyFileNameTag = "_concatenated_target_decoy";

    /**
     * Empty default constructor.
     */
    public FastaParameters() {
    }

    /**
     * Returns a boolean indicating whether the FASTA file should be processed
     * as target-decoy or only target.
     *
     * @return a boolean indicating whether the FASTA file should be processed
     * as target-decoy or only target
     */
    public boolean isTargetDecoy() {
        readDBMode();
        return targetDecoy;
    }

    /**
     * Sets whether the FASTA file should be processed as target-decoy or only
     * target.
     *
     * @param targetDecoy whether the FASTA file should be processed as
     * target-decoy or only target
     */
    public void setTargetDecoy(boolean targetDecoy) {
        writeDBMode();
        this.targetDecoy = targetDecoy;
    }

    /**
     * Returns the decoy flag.
     *
     * @return the decoy flag
     */
    public String getDecoyFlag() {
        readDBMode();
        return decoyFlag;
    }

    /**
     * Sets the decoy flag.
     *
     * @param decoyFlag the decoy flag
     */
    public void setDecoyFlag(String decoyFlag) {
        writeDBMode();
        this.decoyFlag = decoyFlag;
    }

    /**
     * Returns a boolean indicating whether the decoy flag is a suffix or a
     * prefix.
     *
     * @return a boolean indicating whether the decoy flag is a suffix or a
     * prefix
     */
    public boolean isDecoySuffix() {
        readDBMode();
        return decoySuffix;
    }

    /**
     * Sets whether the decoy flag is a suffix or a prefix.
     *
     * @param decoySuffix whether the decoy flag is a suffix or a prefix
     */
    public void setDecoySuffix(boolean decoySuffix) {
        writeDBMode();
        this.decoySuffix = decoySuffix;
    }

    /**
     * Returns the target-decoy file name suffix.
     *
     * @return the targetDecoyFileNameSuffix
     */
    public String getTargetDecoyFileNameSuffix() {
        readDBMode();
        return targetDecoyFileNameTag;
    }

    /**
     * Set the target-decoy file name suffix.
     *
     * @param targetDecoyFileNameSuffix the targetDecoyFileNameSuffix to set
     */
    public void setTargetDecoyFileNameSuffix(String targetDecoyFileNameSuffix) {
        writeDBMode();
        this.targetDecoyFileNameTag = targetDecoyFileNameSuffix;
    }

    /**
     * Returns a boolean indicating whether the parsing parameters are the same
     * as the given parameters.
     *
     * @param fastaParameters the other parameters
     *
     * @return a boolean indicating whether the parsing parameters are the same
     * as the given parameters
     */
    public boolean isSameAs(FastaParameters fastaParameters) {

        readDBMode();

        if (targetDecoy != fastaParameters.isTargetDecoy()) {

            return false;

        }

        if (decoyFlag != null && fastaParameters.getDecoyFlag() == null
                || decoyFlag == null && fastaParameters.getDecoyFlag() != null
                || decoyFlag != null && fastaParameters.getDecoyFlag() != null && !decoyFlag.equals(fastaParameters.getDecoyFlag())) {

            return false;

        }

        if (decoySuffix != fastaParameters.isDecoySuffix()) {

            return false;

        }

        if (targetDecoyFileNameTag != null && fastaParameters.getTargetDecoyFileNameSuffix() == null
                || targetDecoyFileNameTag == null && fastaParameters.getTargetDecoyFileNameSuffix() != null
                || targetDecoyFileNameTag != null && fastaParameters.getTargetDecoyFileNameSuffix() != null && !targetDecoyFileNameTag.equals(fastaParameters.getTargetDecoyFileNameSuffix())) {

            return false;

        }

        return true;
    }

    /**
     * Infers the parameters used to parse the file.
     *
     * @param fastaFilePath path to a FASTA file
     *
     * @return returns FASTA parameters inferred from the file
     *
     * @throws IOException exception thrown if an error occurred while iterating
     * the file
     */
    public static FastaParameters inferParameters(String fastaFilePath) throws IOException {

        return inferParameters(fastaFilePath, null);

    }

    /**
     * Infers the parameters used to parse the file.
     *
     * @param fastaFilePath path to a FASTA file
     * @param waitingHandler a handler to allow canceling the import
     *
     * @return returns FASTA parameters inferred from the file
     *
     * @throws IOException exception thrown if an error occurred while iterating
     * the file
     */
    public static FastaParameters inferParameters(String fastaFilePath, WaitingHandler waitingHandler) throws IOException {

        FastaParameters fastaParameters = new FastaParameters();
        File fastaFile = new File(fastaFilePath);

        HeaderIterator headerIterator = new HeaderIterator(fastaFile);
        String fastaHeader;
        int i = 0, offset = 0, offSetIncrease = 100;

        while ((fastaHeader = headerIterator.getNextHeader()) != null) {

            if (i > offset && i < offset + 10) {

                Header header = Header.parseFromFASTA(fastaHeader);
                String accession = header.getAccessionOrRest();
                String accessionLowerCase = accession.toLowerCase();

                for (String decoyFlagLowerCase : DECOY_FLAGS) {

                    if (accession.length() > decoyFlagLowerCase.length()) {

                        String subString = accessionLowerCase.substring(0, decoyFlagLowerCase.length());

                        if (subString.equals(decoyFlagLowerCase)) {

                            String decoyFlag = accession.substring(0, decoyFlagLowerCase.length());

                            for (char sep : DECOY_SEPARATORS) {

                                if (accession.charAt(decoyFlagLowerCase.length()) == sep) {

                                    decoyFlag += sep;

                                    fastaParameters.setDecoySuffix(false);
                                    fastaParameters.setDecoyFlag(decoyFlag);

                                    headerIterator.close();

                                    return fastaParameters;

                                }

                            }

                            fastaParameters.setDecoySuffix(false);
                            fastaParameters.setDecoyFlag(decoyFlag);

                            headerIterator.close();

                            return fastaParameters;

                        }

                        int startIndex = accession.length() - decoyFlagLowerCase.length();
                        subString = accessionLowerCase.substring(startIndex);

                        if (subString.equals(decoyFlagLowerCase)) {

                            String decoyFlag = accession.substring(startIndex);

                            for (char sep : DECOY_SEPARATORS) {

                                if (accession.charAt(startIndex - 1) == sep) {

                                    decoyFlag = sep + decoyFlag;

                                    fastaParameters.setDecoySuffix(true);
                                    fastaParameters.setDecoyFlag(decoyFlag);

                                    headerIterator.close();

                                    return fastaParameters;

                                }

                            }

                            fastaParameters.setDecoySuffix(true);
                            fastaParameters.setDecoyFlag(decoyFlag);

                            headerIterator.close();

                            return fastaParameters;

                        }
                    }
                }

            } else if (i == offset + 10) {

                if (i > 10 * offSetIncrease) {

                    offSetIncrease *= 10;

                }

                offset += offSetIncrease;

            }

            i++;

            if (waitingHandler != null && waitingHandler.isRunCanceled()) {

                return null;

            }
        }

        return fastaParameters;
    }

    public boolean equals(FastaParameters fastaParameters) {

        if (!decoyFlag.equals(fastaParameters.getDecoyFlag())) {
            return false;
        }

        if (decoySuffix != fastaParameters.isDecoySuffix()) {
            return false;
        }

        if (!targetDecoyFileNameTag.equals(fastaParameters.getTargetDecoyFileNameSuffix())) {
            return false;
        }

        return true;

    }

    /**
     * Returns a short description of the parameters.
     *
     * @return a short description of the parameters
     */
    public String getShortDescription() {
        readDBMode();

        String newLine = System.getProperty("line.separator");
        StringBuilder output = new StringBuilder();
        output.append("Decoy File Tag: ").append(decoySuffix).append(", ").append("Decoy Tag: ").append(decoyFlag).append(".").append(newLine);

        return output.toString();
    }
}
