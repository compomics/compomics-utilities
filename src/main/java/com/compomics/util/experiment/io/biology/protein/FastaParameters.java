package com.compomics.util.experiment.io.biology.protein;

import com.compomics.util.Util;
import com.compomics.util.experiment.io.biology.protein.iterators.HeaderIterator;
import com.compomics.util.waiting.WaitingHandler;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

/**
 * The parameters used to parse a FASTA file.
 *
 * @author Marc Vaudel
 */
public class FastaParameters implements Serializable {

    /**
     * The version UID for serialization/deserialization compatibility.
     */
    static final long serialVersionUID = -7453836034514062470L;
    /**
     * The decoy flags used to infer the FASTA parameters.
     */
    public static final String[] decoyFlags = {"decoy", "random", "reversed", "rev"};
    /**
     * The decoy separators used to infer the FASTA parameters.
     */
    public static final char[] separators = {'-', '.', '_'};
    /**
     * The name of the database.
     */
    private String name;
    /**
     * Description of the database.
     */
    private String description;
    /**
     * The version of the database.
     */
    private String version;
    /**
     * Indicates whether the database is a concatenated target/decoy.
     */
    private boolean targetDecoy;
    /**
     * The flag for decoy proteins in the accession.
     */
    private String decoyFlag;
    /**
     * Boolean indicating whether the decoy flag is a suffix or a prefix.
     */
    private boolean decoySuffix;

    /**
     * Returns the decoy flag.
     *
     * @return the decoy flag
     */
    public String getDecoyFlag() {
        return decoyFlag;
    }

    /**
     * Sets the decoy flag.
     *
     * @param decoyFlag the decoy flag
     */
    public void setDecoyFlag(String decoyFlag) {
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
        return decoySuffix;
    }

    /**
     * Sets whether the decoy flag is a suffix or a prefix.
     *
     * @param decoySuffix whether the decoy flag is a suffix or a prefix
     */
    public void setDecoySuffix(boolean decoySuffix) {
        this.decoySuffix = decoySuffix;
    }

    /**
     * Returns the name of the database.
     *
     * @return the name for the database
     */
    public String getName() {
        return name;
    }

    /**
     * Sets a new name for the database.
     *
     * @param name a new name for the database
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the database version.
     *
     * @return the database version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the database version.
     *
     * @param version the database version
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Returns the description for this database.
     *
     * @return the description for this database
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description for this database.
     *
     * @param description the description for this database
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns a boolean indicating whether the database is concatenated target
     * decoy.
     *
     * @return a boolean indicating whether the database is concatenated target
     * decoy
     */
    public boolean isTargetDecoy() {
        return targetDecoy;
    }

    /**
     * Sets whether the database is concatenated target decoy.
     *
     * @param targetDecoy a boolean indicating whether the database is
     * concatenated target decoy
     */
    public void setTargetDecoy(boolean targetDecoy) {
        this.targetDecoy = targetDecoy;
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

        if (name != null && fastaParameters.getName() == null
                || name == null && fastaParameters.getName() != null
                || name != null && fastaParameters.getName() != null && !name.equals(fastaParameters.getName())) {

            return false;

        }
        if (description != null && fastaParameters.getDescription() == null
                || description == null && fastaParameters.getDescription() != null
                || description != null && fastaParameters.getDescription() != null && !description.equals(fastaParameters.getDescription())) {

            return false;

        }
        if (version != null && fastaParameters.getVersion() == null
                || version == null && fastaParameters.getVersion() != null
                || version != null && fastaParameters.getVersion() != null && !version.equals(fastaParameters.getVersion())) {

            return false;

        }
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

        return true;
    }

    /**
     * Sets default name and version from the given FASTA file.
     *
     * @param fastaFile the FASTA file
     */
    public void setDefaultAttributes(File fastaFile) {

        setName(Util.removeExtension(fastaFile.getName()));
        setDescription(fastaFile.getAbsolutePath());
        setVersion((new Date(fastaFile.lastModified())).toString());

    }

    /**
     * Infers the parameters used to parse the file.
     *
     * @param fastaFile a FASTA file
     *
     * @return returns FASTA parameters inferred from the file
     *
     * @throws IOException exception thrown if an error occurred while iterating
     * the file
     */
    public static FastaParameters inferParameters(File fastaFile) throws IOException {

        return inferParameters(fastaFile, null);

    }

    /**
     * Infers the parameters used to parse the file.
     *
     * @param fastaFile a FASTA file
     * @param waitingHandler a handler to allow canceling the import
     *
     * @return returns FASTA parameters inferred from the file
     *
     * @throws IOException exception thrown if an error occurred while iterating
     * the file
     */
    public static FastaParameters inferParameters(File fastaFile, WaitingHandler waitingHandler) throws IOException {

        FastaParameters fastaParameters = new FastaParameters();

        fastaParameters.setDefaultAttributes(fastaFile);

        HeaderIterator headerIterator = new HeaderIterator(fastaFile);
        String fastaHeader;
        int i = 0, offset = 0, offSetIncrease = 100;

        while ((fastaHeader = headerIterator.getNextHeader()) != null) {

            if (i > offset && i < offset + 10) {

                Header header = Header.parseFromFASTA(fastaHeader);
                String accession = header.getAccessionOrRest();
                String accessionLowerCase = accession.toLowerCase();

                for (String decoyFlagLowerCase : decoyFlags) {

                    if (accession.length() > decoyFlagLowerCase.length()) {

                        String subString = accessionLowerCase.substring(0, decoyFlagLowerCase.length());

                        if (subString.equals(decoyFlagLowerCase)) {

                            String decoyFlag = accession.substring(0, decoyFlagLowerCase.length());

                            for (char sep : separators) {

                                if (accession.charAt(decoyFlagLowerCase.length()) == sep) {

                                    decoyFlag += sep;

                                    fastaParameters.setTargetDecoy(true);
                                    fastaParameters.setDecoySuffix(false);
                                    fastaParameters.setDecoyFlag(decoyFlag);

                                    headerIterator.close();

                                    return fastaParameters;

                                }

                            }

                            fastaParameters.setTargetDecoy(true);
                            fastaParameters.setDecoySuffix(false);
                            fastaParameters.setDecoyFlag(decoyFlag);

                            headerIterator.close();

                            return fastaParameters;

                        }

                        int startIndex = accession.length() - decoyFlagLowerCase.length();
                        subString = accessionLowerCase.substring(startIndex);

                        if (subString.equals(decoyFlagLowerCase)) {

                            String decoyFlag = accession.substring(startIndex);

                            for (char sep : separators) {

                                if (accession.charAt(startIndex - 1) == sep) {

                                    decoyFlag = sep + decoyFlag;

                                    fastaParameters.setTargetDecoy(true);
                                    fastaParameters.setDecoySuffix(true);
                                    fastaParameters.setDecoyFlag(decoyFlag);

                                    headerIterator.close();

                                    return fastaParameters;

                                }

                            }

                            fastaParameters.setTargetDecoy(true);
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

        fastaParameters.setTargetDecoy(false);

        return fastaParameters;
    }

    public boolean equals(FastaParameters fastaParameters) {

        if (!name.equals(fastaParameters.getName())) {
            return false;
        }

        if (!description.equals(fastaParameters.getDescription())) {
            return false;
        }

        if (!version.equals(fastaParameters.getVersion())) {
            return false;
        }

        if (targetDecoy != fastaParameters.isTargetDecoy()) {
            return false;
        }

        if (!decoyFlag.equals(fastaParameters.getDecoyFlag())) {
            return false;
        }

        if (decoySuffix != fastaParameters.isDecoySuffix()) {
            return false;
        }

        return true;

    }
}
