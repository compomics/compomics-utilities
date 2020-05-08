package com.compomics.util.experiment.io.identification.writers;

import com.compomics.util.experiment.biology.enzymes.Enzyme;
import com.compomics.util.experiment.biology.ions.Ion;
import com.compomics.util.experiment.biology.ions.NeutralLoss;
import com.compomics.util.experiment.biology.ions.impl.ImmoniumIon;
import com.compomics.util.experiment.biology.ions.impl.PeptideFragmentIon;
import com.compomics.util.experiment.biology.ions.impl.PrecursorIon;
import com.compomics.util.experiment.biology.ions.impl.RelatedIon;
import com.compomics.util.experiment.biology.ions.impl.ReporterIon;
import com.compomics.util.experiment.biology.modifications.Modification;
import com.compomics.util.experiment.biology.modifications.ModificationProvider;
import com.compomics.util.experiment.biology.modifications.ModificationType;
import com.compomics.util.experiment.biology.proteins.Peptide;
import com.compomics.util.experiment.identification.Advocate;
import com.compomics.util.experiment.identification.matches.IonMatch;
import com.compomics.util.experiment.identification.matches.ModificationMatch;
import com.compomics.util.experiment.identification.spectrum_annotation.AnnotationParameters;
import com.compomics.util.experiment.identification.spectrum_annotation.SpecificAnnotationParameters;
import com.compomics.util.experiment.identification.spectrum_annotation.spectrum_annotators.PeptideSpectrumAnnotator;
import com.compomics.util.experiment.identification.spectrum_assumptions.PeptideAssumption;
import com.compomics.util.experiment.identification.utils.ModificationUtils;
import com.compomics.util.experiment.identification.utils.PeptideUtils;
import com.compomics.util.experiment.io.biology.protein.FastaSummary;
import com.compomics.util.experiment.io.biology.protein.ProteinDetailsProvider;
import com.compomics.util.experiment.io.biology.protein.SequenceProvider;
import com.compomics.util.experiment.io.identification.MzIdentMLVersion;
import com.compomics.util.experiment.mass_spectrometry.SpectrumProvider;
import com.compomics.util.experiment.mass_spectrometry.spectra.Spectrum;
import com.compomics.util.io.IoUtil;
import com.compomics.util.io.compression.SectionGzWriter.SectionGzWriter;
import com.compomics.util.parameters.identification.IdentificationParameters;
import com.compomics.util.parameters.identification.advanced.SequenceMatchingParameters;
import com.compomics.util.parameters.identification.search.DigestionParameters;
import com.compomics.util.parameters.identification.search.ModificationParameters;
import com.compomics.util.parameters.identification.search.SearchParameters;
import com.compomics.util.pride.CvTerm;
import com.compomics.util.threading.SimpleSemaphore;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 * Simple mzIdentML exporter for PSM-level results.
 *
 * @author Marc Vaudel
 */
public class SimpleMzIdentMLExporter implements Closeable {

    /**
     * Key for the head section.
     */
    public static final String HEAD_SECTION = "HEAD_SECTION";
    /**
     * Key for the peptide section.
     */
    public static final String PEPTIDE_SECTION = "PEPTIDE_SECTION";
    /**
     * Key for the peptide evidence section.
     */
    public static final String PEPTIDE_EVIDENCE_SECTION = "PEPTIDE_EVIDENCE_SECTION";
    /**
     * Key for the analysis section.
     */
    public static final String ANALYSIS_SECTION = "ANALYSIS_SECTION";
    /**
     * Key for the data section.
     */
    public static final String DATA_SECTION = "DATA_SECTION";
    /**
     * Key for the tail section.
     */
    public static final String TAIL_SECTION = "TAIL_SECTION";
    /**
     * The version of the mzIdentML format.
     */
    public static final MzIdentMLVersion MZIDENTML_VERSION = MzIdentMLVersion.v1_2;
    /**
     * The maximum number of neutral losses a fragment ion can have in order to
     * be annotated.
     */
    public static final int MAX_NEUTRAL_LOSSES = 2;
    /**
     * The number of amino acids to export before and after the peptide
     */
    public static final int N_AA = 1;
    /**
     * Writer for the mzIdentML file.
     */
    private final SectionGzWriter writer;
    /**
     * The spectrum file.
     */
    private final File spectrumFile;
    /**
     * The search engine result file.
     */
    private final File searchEngineFile;
    /**
     * The fasta file.
     */
    private final File fastaFile;
    /**
     * The search engines used.
     */
    private final HashMap<String, ArrayList<String>> searchEngines;
    /**
     * Integer keeping track of the number of tabs to include at the beginning
     * of each line.
     */
    private final ConcurrentHashMap<String, Integer> indentCounterMap = new ConcurrentHashMap<>(6);
    /**
     * The provider to use to get modification information.
     */
    private final ModificationProvider modificationProvider;
    /**
     * The protein sequence provider.
     */
    private final SequenceProvider sequenceProvider;
    /**
     * The spectrum provider.
     */
    private final SpectrumProvider spectrumProvider;
    /**
     * Summary information on the protein sequences file.
     */
    private final FastaSummary fastaSummary;
    /**
     * A protein details provider.
     */
    private final ProteinDetailsProvider proteinDetailsProvider;
    /**
     * The software name.
     */
    private final String softwareName;
    /**
     * The software version.
     */
    private final String softwareVersion;
    /**
     * The software URL.
     */
    private final String softwareUrl;
    /**
     * The peptide evidence IDs.
     */
    private final HashMap<String, String> pepEvidenceIds = new HashMap<>();
    /**
     * Spectrum title to index map.
     */
    private final HashMap<String, Integer> spectrumTitleToIndexMap = new HashMap<>();
    /**
     * Set of already encountered peptide keys.
     */
    private final HashSet<Long> peptideKeys = new HashSet<>();
    /**
     * Count for the number of psms written.
     */
    private int psmCount = 0;
    /**
     * The identification parameters.
     */
    private final IdentificationParameters identificationParameters;
    /**
     * Map of PTM indexes: PTM mass to index.
     */
    private final HashMap<Double, Integer> modIndexMap = new HashMap<>();
    /**
     * Semaphore for adding peptides.
     */
    private final SimpleSemaphore peptideSemaphore = new SimpleSemaphore(1);
    /**
     * Semaphore for adding spectra.
     */
    private final SimpleSemaphore spectrumSemaphore = new SimpleSemaphore(1);
    /**
     * Semaphore for adding spectrum matches.
     */
    private final SimpleSemaphore spectrumMatchSemaphore = new SimpleSemaphore(1);
    /**
     * Counter for the protein evidence items.
     */
    private int peptideEvidenceCounter = 0;
    /**
     * Semaphore for the file initialization.
     */
    private final SimpleSemaphore initSemaphore = new SimpleSemaphore(1);
    /**
     * Exception encountered during file initialization. Null if none.
     */
    private Exception initException = null;

    /**
     * Constructor.
     *
     * @param softwareName The name of the software used to write the mzIdentML
     * file.
     * @param softwareVersion The version of the software used to write the
     * mzIdentML file.
     * @param softwareUrl The URL of the software used to write the mzIdentML
     * file.
     * @param tempFolder The folder to use to write temporary files.
     * @param destinationFile The mzIdentML file to write.
     * @param spectrumFile The spectrum file.
     * @param searchEngineFile The search engine file used to identify the
     * spectra.
     * @param searchEngines Map of the search engine(s) and their version used
     * to identify the spectra.
     * @param fastaFile The fasta file containing the peptide/protein sequences.
     * @param identificationParameters The identification parameters used to
     * identify the spectra.
     * @param sequenceProvider A sequence provider for the given fasta file.
     * @param proteinDetailsProvider A protein details provider for the given
     * fasta file.
     * @param spectrumProvider A spectrum provider for the given spectrum file.
     * @param modificationProvider A modification provider.
     * @param fastaSummary A summary for the given fasta file.
     * @param contactFirstName Contact first name.
     * @param contactLastName Contact last name.
     * @param contactAddress Contact address.
     * @param contactEmail Contact email.
     * @param contactOrganizationName Contact organization name.
     * @param contactOrganizationAddress Contact organization address.
     * @param contactOrganizationEmail Contact organization email.
     *
     * @throws FileNotFoundException Exception thrown if a file is not found.
     * @throws IOException Exception thrown if an error occurred while reading
     * or writing a file.
     */
    public SimpleMzIdentMLExporter(
            String softwareName,
            String softwareVersion,
            String softwareUrl,
            File tempFolder,
            File destinationFile,
            File spectrumFile,
            File searchEngineFile,
            HashMap<String, ArrayList<String>> searchEngines,
            File fastaFile,
            IdentificationParameters identificationParameters,
            SequenceProvider sequenceProvider,
            ProteinDetailsProvider proteinDetailsProvider,
            SpectrumProvider spectrumProvider,
            ModificationProvider modificationProvider,
            FastaSummary fastaSummary,
            String contactFirstName,
            String contactLastName,
            String contactAddress,
            String contactEmail,
            String contactOrganizationName,
            String contactOrganizationAddress,
            String contactOrganizationEmail
    )
            throws FileNotFoundException, IOException {

        this.softwareName = softwareName;
        this.softwareVersion = softwareVersion;
        this.softwareUrl = softwareUrl;
        this.spectrumFile = spectrumFile;
        this.searchEngineFile = searchEngineFile;
        this.searchEngines = searchEngines;
        this.fastaFile = fastaFile;
        this.identificationParameters = identificationParameters;
        this.sequenceProvider = sequenceProvider;
        this.proteinDetailsProvider = proteinDetailsProvider;
        this.spectrumProvider = spectrumProvider;
        this.modificationProvider = modificationProvider;
        this.fastaSummary = fastaSummary;
        writer = new SectionGzWriter(destinationFile, tempFolder);

        initTabCounterMap();

        init(
                contactFirstName,
                contactLastName,
                contactAddress,
                contactEmail,
                contactOrganizationName,
                contactOrganizationAddress,
                contactOrganizationEmail
        );

    }

    /**
     * Initializes the writing of the file. Writes up to the peptide section.
     * Note: Protein details are written in a separate thread.
     *
     * @param contactFirstName Contact first name.
     * @param contactLastName Contact last name.
     * @param contactAddress Contact address.
     * @param contactEmail Contact email.
     * @param contactOrganizationName Contact organization name.
     * @param contactOrganizationAddress Contact organization address.
     * @param contactOrganizationEmail Contact organization email.
     */
    public void init(
            String contactFirstName,
            String contactLastName,
            String contactAddress,
            String contactEmail,
            String contactOrganizationName,
            String contactOrganizationAddress,
            String contactOrganizationEmail
    ) {

        initSemaphore.acquire();

        new Thread(HEAD_SECTION) {
            @Override
            public synchronized void run() {

                try {

                    // The mzIdentML start tag.
                    writeMzIdentMLStartTag();

                    // The cv list.
                    writeCvList();

                    // The AnalysisSoftwareList.
                    writeAnalysisSoftwareList();

                    // The Provider details.
                    writeProviderDetails();

                    // The AuditCollection details.
                    writeAuditCollection(
                            contactFirstName,
                            contactLastName,
                            contactAddress,
                            contactEmail,
                            contactOrganizationName,
                            contactOrganizationAddress,
                            contactOrganizationEmail
                    );

                    // The protein sequence collection.
                    writeProteinSequenceCollection();

                    // Section completed, write to main file and delete temp file.
                    writer.sectionCompleted(HEAD_SECTION);

                } catch (Exception e) {

                    initException = e;
                    throw (e);

                } finally {

                    initSemaphore.release();

                }
            }
        }.start();

        // Write the analyis collection.
        writeAnalysisCollection();

        // Write the analysis protocol-
        writeAnalysisProtocol();

        // Set up the data collection.
        setupDataCollection();

    }

    /**
     * Finalizes the writing of the mzIdentML file.
     */
    public void finalizeFile() {

        // Make sure that initialization is finished and did not encounter any exception.
        initSemaphore.acquire();

        if (initException != null) {

            throw new IllegalArgumentException(
                    "An error occurred while initializing the mzIdentML file.",
                    initException
            );

        }

        initSemaphore.release();

        // Section completed, write to main file and delete temp file.
        writer.sectionCompleted(PEPTIDE_SECTION);

        // The end of the peptide evidence section.
        finalizePeptideEvidenceSection();

        // Section completed, write to main file and delete temp file.
        writer.sectionCompleted(PEPTIDE_EVIDENCE_SECTION);

        // Section completed, write to main file and delete temp file.
        writer.sectionCompleted(ANALYSIS_SECTION);

        // The end of the data collection section.
        finalizeDataCollection();

        // The mzIdentML end tag.
        writeMzIdentMLEndTag();

        // Section completed, write to main file and delete temp file.
        writer.sectionCompleted(DATA_SECTION);

    }

    /**
     * Writes the mzIdentML start tag.
     */
    private void writeMzIdentMLStartTag() {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        writer.write(HEAD_SECTION, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        writer.newLine(HEAD_SECTION);

        writer.write(
                HEAD_SECTION,
                "<MzIdentML id=\"" + softwareName + " v" + softwareVersion + "\""
                + " xmlns:xsi=\"https://www.w3.org/2001/XMLSchema-instance\" "
                //+ " xsi:schemaLocation=\"http://psidev.info/psi/pi/mzIdentML/1.2 http://www.psidev.info/files/mzIdentML1.2.0.xsd\""
                + " xmlns=\"http://psidev.info/psi/pi/mzIdentML/1.2\""
                + " version=\"1.2.0\" "
                + "creationDate=\"" + dateFormat.format(new Date()) + "\">"
        );
        writer.newLine(HEAD_SECTION);

        increaseIndent(HEAD_SECTION);

    }

    /**
     * Writes the CV list.
     */
    private void writeCvList() {

        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "<cvList>");
        writer.newLine(HEAD_SECTION);
        increaseIndent(HEAD_SECTION);

        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "<cv id=\"PSI-MS\" ");
        writer.write(HEAD_SECTION, "uri=\"https://raw.githubusercontent.com/HUPO-PSI/psi-ms-CV/master/psi-ms.obo\" ");
        writer.write(HEAD_SECTION, "fullName=\"PSI-MS\"/>");
        writer.newLine(HEAD_SECTION);

        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "<cv id=\"UNIMOD\" ");
        writer.write(HEAD_SECTION, "uri=\"http://www.unimod.org/obo/unimod.obo\" ");
        writer.write(HEAD_SECTION, "fullName=\"UNIMOD\"/>");
        writer.newLine(HEAD_SECTION);

        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "<cv id=\"UO\" ");
        writer.write(HEAD_SECTION, "uri=\"https://raw.githubusercontent.com/bio-ontology-research-group/unit-ontology/master/unit.obo\" ");
        writer.write(HEAD_SECTION, "fullName=\"UNIT-ONTOLOGY\"/>");
        writer.newLine(HEAD_SECTION);

        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "<cv id=\"PRIDE\" ");
        writer.write(HEAD_SECTION, "uri=\"https://github.com/PRIDE-Utilities/pride-ontology/blob/master/pride_cv.obo\" ");
        writer.write(HEAD_SECTION, "fullName=\"PRIDE\"/>");
        writer.newLine(HEAD_SECTION);

        decreaseIndent(HEAD_SECTION);
        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "</cvList>");
        writer.newLine(HEAD_SECTION);

    }

    /**
     * Write the software list.
     */
    private void writeAnalysisSoftwareList() {

        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "<AnalysisSoftwareList>");
        writer.newLine(HEAD_SECTION);
        increaseIndent(HEAD_SECTION);

        // @TODO: also add SearchGUI and/or search engines used?
        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "<AnalysisSoftware name=\"");
        writer.write(HEAD_SECTION, softwareName);
        writer.write(HEAD_SECTION, "\" version=\"");
        writer.write(HEAD_SECTION, softwareVersion);
        writer.write(HEAD_SECTION, "\" id=\"ID_software\" uri=\"");
        writer.write(HEAD_SECTION, softwareUrl);
        writer.write(HEAD_SECTION, "\">");
        writer.newLine(HEAD_SECTION);
        increaseIndent(HEAD_SECTION);

        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "<ContactRole contact_ref=\"PS_DEV\">");
        writer.newLine(HEAD_SECTION);
        increaseIndent(HEAD_SECTION);

        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "<Role>");
        writer.newLine(HEAD_SECTION);
        increaseIndent(HEAD_SECTION);

        writeCvTerm(
                HEAD_SECTION,
                new CvTerm(
                        "PSI-MS",
                        "MS:1001267",
                        "software vendor",
                        null
                )
        );
        decreaseIndent(HEAD_SECTION);

        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "</Role>");
        writer.newLine(HEAD_SECTION);
        decreaseIndent(HEAD_SECTION);

        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "</ContactRole>");
        writer.newLine(HEAD_SECTION);

        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "<SoftwareName>");
        writer.newLine(HEAD_SECTION);
        increaseIndent(HEAD_SECTION);

        writeCvTerm(
                HEAD_SECTION,
                new CvTerm(
                        "PSI-MS",
                        "MS:1002458",
                        "PeptideShaker",
                        null
                )
        );
        decreaseIndent(HEAD_SECTION);

        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "</SoftwareName>");
        writer.newLine(HEAD_SECTION);

        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "<Customizations>No customisations</Customizations>");
        writer.newLine(HEAD_SECTION);

        decreaseIndent(HEAD_SECTION);
        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "</AnalysisSoftware>");
        writer.newLine(HEAD_SECTION);

        decreaseIndent(HEAD_SECTION);
        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "</AnalysisSoftwareList>");
        writer.newLine(HEAD_SECTION);

    }

    /**
     * Write the provider details.
     */
    private void writeProviderDetails() {

        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "<Provider id=\"PROVIDER\">");
        writer.newLine(HEAD_SECTION);
        increaseIndent(HEAD_SECTION);

        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "<ContactRole contact_ref=\"PROVIDER\">");
        writer.newLine(HEAD_SECTION);
        increaseIndent(HEAD_SECTION);

        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "<Role>");
        writer.newLine(HEAD_SECTION);
        increaseIndent(HEAD_SECTION);

        // @TODO: add user defined provider role?
        writeCvTerm(
                HEAD_SECTION,
                new CvTerm(
                        "PSI-MS",
                        "MS:1001271",
                        "researcher",
                        null
                )
        );
        decreaseIndent(HEAD_SECTION);

        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "</Role>");
        writer.newLine(HEAD_SECTION);

        decreaseIndent(HEAD_SECTION);
        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "</ContactRole>");
        writer.newLine(HEAD_SECTION);

        decreaseIndent(HEAD_SECTION);
        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "</Provider>");
        writer.newLine(HEAD_SECTION);

    }

    /**
     * Write the audit collection.
     */
    private void writeAuditCollection(
            String contactFirstName,
            String contactLastName,
            String contactAddress,
            String contactEmail,
            String contactOrganizationName,
            String contactOrganizationAddress,
            String contactOrganizationEmail
    ) {

        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "<AuditCollection>");
        writer.newLine(HEAD_SECTION);
        increaseIndent(HEAD_SECTION);

        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "<Person firstName=\"");
        writer.write(HEAD_SECTION, contactFirstName);
        writer.write(HEAD_SECTION, "\" lastName=\"");
        writer.write(HEAD_SECTION, contactLastName);
        writer.write(HEAD_SECTION, "\" id=\"PROVIDER\">");
        writer.newLine(HEAD_SECTION);
        increaseIndent(HEAD_SECTION);

        writeCvTerm(
                HEAD_SECTION,
                new CvTerm(
                        "PSI-MS",
                        "MS:1000587",
                        "contact address",
                        StringEscapeUtils.escapeHtml4(
                                contactAddress
                        )
                )
        );

        writeCvTerm(
                HEAD_SECTION,
                new CvTerm(
                        "PSI-MS",
                        "MS:1000589",
                        "contact email",
                        contactEmail
                )
        );

        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "<Affiliation organization_ref=\"ORG_DOC_OWNER\"/>");
        writer.newLine(HEAD_SECTION);
        decreaseIndent(HEAD_SECTION);

        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "</Person>");
        writer.newLine(HEAD_SECTION);

        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "<Organization name=\"");
        writer.write(HEAD_SECTION, StringEscapeUtils.escapeHtml4(contactOrganizationName));
        writer.write(HEAD_SECTION, "\" id=\"ORG_DOC_OWNER\">");
        writer.newLine(HEAD_SECTION);
        increaseIndent(HEAD_SECTION);

        writeCvTerm(
                HEAD_SECTION,
                new CvTerm(
                        "PSI-MS",
                        "MS:1000586",
                        "contact name",
                        StringEscapeUtils.escapeHtml4(contactOrganizationName)
                )
        );
        writeCvTerm(
                HEAD_SECTION,
                new CvTerm(
                        "PSI-MS",
                        "MS:1000587",
                        "contact address",
                        StringEscapeUtils.escapeHtml4(contactOrganizationAddress)
                )
        );

        writeCvTerm(
                HEAD_SECTION,
                new CvTerm(
                        "PSI-MS",
                        "MS:1000589",
                        "contact email",
                        contactOrganizationEmail
                )
        );
        decreaseIndent(HEAD_SECTION);

        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "</Organization>");
        writer.newLine(HEAD_SECTION);

        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "<Organization name=\"PeptideShaker developers\" id=\"PS_DEV\">");
        writer.newLine(HEAD_SECTION);
        increaseIndent(HEAD_SECTION);

        writeCvTerm(
                HEAD_SECTION,
                new CvTerm(
                        "PSI-MS",
                        "MS:1000586",
                        "contact name",
                        "PeptideShaker developers"
                )
        );
        writeCvTerm(
                HEAD_SECTION,
                new CvTerm(
                        "PSI-MS",
                        "MS:1000587",
                        "contact address",
                        "Proteomics Unit, Building for Basic Biology, University of Bergen, Jonas Liesvei 91, N-5009 Bergen, Norway"
                )
        );
        writeCvTerm(
                HEAD_SECTION,
                new CvTerm(
                        "PSI-MS",
                        "MS:1000588",
                        "contact URL",
                        "https://compomics.github.io/projects/peptide-shaker.html"
                )
        );
        writeCvTerm(
                HEAD_SECTION,
                new CvTerm(
                        "PSI-MS",
                        "MS:1000589",
                        "contact email",
                        "peptide-shaker@googlegroups.com"
                )
        );

        decreaseIndent(HEAD_SECTION);

        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "</Organization>");
        writer.newLine(HEAD_SECTION);

        decreaseIndent(HEAD_SECTION);
        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "</AuditCollection>");
        writer.newLine(HEAD_SECTION);

    }

    /**
     * Write the protein sequence collection.
     */
    private void writeProteinSequenceCollection() {

        writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
        writer.write(HEAD_SECTION, "<SequenceCollection>");
        writer.newLine(HEAD_SECTION);
        increaseIndent(HEAD_SECTION);

//        String dbType = Header.getDatabaseTypeAsString(Header.DatabaseType.Unknown); // @TODO: add database type as cv param? children of MS:1001013 (database name)
//        FastaIndex fastaIndex = sequenceFactory.getCurrentFastaIndex();
//        if (fastaIndex != null) {
//            dbType = Header.getDatabaseTypeAsString(fastaIndex.getDatabaseType());
//        }
//
        for (String accession : sequenceProvider.getAccessions()) { // @TODO: include only protein sequences with at least one PeptideEvidence element referring to it (note: this is a SHOULD rule in the mzid specifications)

            writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
            writer.write(HEAD_SECTION, "<DBSequence id=\"");
            writer.write(HEAD_SECTION, accession);
            writer.write(HEAD_SECTION, "\" ");
            writer.write(HEAD_SECTION, "accession=\"");
            writer.write(HEAD_SECTION, accession);
            writer.write(HEAD_SECTION, "\" searchDatabase_ref=\"SearchDB_1\" >");
            writer.newLine(HEAD_SECTION);
            increaseIndent(HEAD_SECTION);

            String sequence = sequenceProvider.getSequence(accession);
            writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
            writer.write(HEAD_SECTION, "<Seq>");
            writer.write(HEAD_SECTION, sequence);
            writer.write(HEAD_SECTION, "</Seq>");
            writer.newLine(HEAD_SECTION);

            String description = proteinDetailsProvider.getDescription(accession);
            writeCvTerm(
                    HEAD_SECTION,
                    new CvTerm(
                            "PSI-MS",
                            "MS:1001088",
                            "protein description",
                            StringEscapeUtils.escapeHtml4(description)
                    )
            );

            decreaseIndent(HEAD_SECTION);
            writer.write(HEAD_SECTION, getCurrentTabSpace(HEAD_SECTION));
            writer.write(HEAD_SECTION, "</DBSequence>");
            writer.newLine(HEAD_SECTION);

        }
    }

    /**
     * Adds a peptide-spectrum match to the file.
     *
     * @param spectrumFile The spectrum file.
     * @param spectrumTitle The spectrum title.
     * @param peptideAssumptions The peptide assumptions for this spectrum.
     * @param modificationLocalizationScores The modification localization
     * scores for the given peptide assumptions.
     * @param peptideSpectrumAnnotator The annotator to use for the spectra.
     */
    public void addSpectrum(
            String spectrumFile,
            String spectrumTitle,
            ArrayList<PeptideAssumption> peptideAssumptions,
            ArrayList<TreeMap<Double, HashMap<Integer, Double>>> modificationLocalizationScores,
            PeptideSpectrumAnnotator peptideSpectrumAnnotator
    ) {

        Integer spectrumIndex = spectrumTitleToIndexMap.get(spectrumTitle);

        if (spectrumIndex == null) {

            spectrumSemaphore.acquire();

            spectrumIndex = spectrumTitleToIndexMap.size();
            spectrumTitleToIndexMap.put(spectrumTitle, spectrumIndex);

            spectrumSemaphore.release();

        } else {

            throw new IllegalArgumentException("Multiple entries for the same spectrum.");

        }

        for (PeptideAssumption peptideAssumption : peptideAssumptions) {

            Peptide peptide = peptideAssumption.getPeptide();
            long peptideKey = peptide.getKey();

            if (!peptideKeys.contains(peptideKey)) {

                peptideSemaphore.acquire();

                if (!peptideKeys.contains(peptideKey)) {

                    peptideKeys.add(peptideKey);

                    writePeptide(peptide);

                }

                peptideSemaphore.release();

            }
        }

        spectrumMatchSemaphore.acquire();

        writeSpectrumIdentificationResult(
                spectrumFile,
                spectrumTitle,
                peptideAssumptions,
                modificationLocalizationScores,
                peptideSpectrumAnnotator
        );

        spectrumMatchSemaphore.release();

    }

    /**
     * Writes a peptide to the file.
     *
     * @param peptide The peptide to write.
     */
    private void writePeptide(
            Peptide peptide
    ) {

        String peptideSequence = peptide.getSequence();

        writer.write(PEPTIDE_SECTION, getCurrentTabSpace(PEPTIDE_SECTION));
        writer.write(PEPTIDE_SECTION, "<Peptide id=\"");
        writer.write(PEPTIDE_SECTION, Long.toString(peptide.getKey()));
        writer.write(PEPTIDE_SECTION, "\">");
        writer.newLine(PEPTIDE_SECTION);
        increaseIndent(PEPTIDE_SECTION);

        writer.write(PEPTIDE_SECTION, getCurrentTabSpace(PEPTIDE_SECTION));
        writer.write(PEPTIDE_SECTION, "<PeptideSequence>");
        writer.write(PEPTIDE_SECTION, peptideSequence);
        writer.write(PEPTIDE_SECTION, "</PeptideSequence>");
        writer.newLine(PEPTIDE_SECTION);

        String[] fixedModifications = peptide.getFixedModifications(
                identificationParameters.getSearchParameters().getModificationParameters(),
                sequenceProvider,
                identificationParameters.getModificationLocalizationParameters().getSequenceMatchingParameters()
        );

        for (int site = 0; site < fixedModifications.length; site++) {

            String modName = fixedModifications[site];

            if (modName != null) {

                Modification modification = modificationProvider.getModification(modName);

                int aa = Math.min(Math.max(site, 1), peptideSequence.length());

                writer.write(PEPTIDE_SECTION, getCurrentTabSpace(PEPTIDE_SECTION));
                writer.write(PEPTIDE_SECTION, "<Modification monoisotopicMassDelta=\"");
                writer.write(PEPTIDE_SECTION, Double.toString(modification.getRoundedMass()));
                writer.write(PEPTIDE_SECTION, "\" residues=\"");
                writer.write(PEPTIDE_SECTION, Character.toString(peptideSequence.charAt(aa - 1)));
                writer.write(PEPTIDE_SECTION, "\" location=\"");
                writer.write(PEPTIDE_SECTION, Integer.toString(site));
                writer.write(PEPTIDE_SECTION, "\" >");
                writer.newLine(PEPTIDE_SECTION);

                CvTerm ptmCvTerm = modification.getUnimodCvTerm();

                if (ptmCvTerm != null) {

                    increaseIndent(PEPTIDE_SECTION);
                    writeCvTerm(
                            PEPTIDE_SECTION,
                            ptmCvTerm,
                            false
                    );
                    decreaseIndent(PEPTIDE_SECTION);

                } else {

                    // try PSI-MOD instead
                    ptmCvTerm = modification.getPsiModCvTerm();

                    if (ptmCvTerm != null) {

                        increaseIndent(PEPTIDE_SECTION);
                        writeCvTerm(
                                PEPTIDE_SECTION,
                                ptmCvTerm,
                                false
                        );
                        decreaseIndent(PEPTIDE_SECTION);

                    }

                }

                writer.write(PEPTIDE_SECTION, getCurrentTabSpace(PEPTIDE_SECTION));
                writer.write(PEPTIDE_SECTION, "</Modification>");
                writer.newLine(PEPTIDE_SECTION);

            }
        }

        for (ModificationMatch modMatch : peptide.getVariableModifications()) {

            Modification modification = modificationProvider.getModification(modMatch.getModification());

            int site = modMatch.getSite();
            int aa = Math.min(Math.max(site, 1), peptideSequence.length());

            writer.write(PEPTIDE_SECTION, getCurrentTabSpace(PEPTIDE_SECTION));
            writer.write(PEPTIDE_SECTION, "<Modification monoisotopicMassDelta=\"");
            writer.write(PEPTIDE_SECTION, Double.toString(modification.getRoundedMass()));
            writer.write(PEPTIDE_SECTION, "\" residues=\"");
            writer.write(PEPTIDE_SECTION, Character.toString(peptideSequence.charAt(aa - 1)));
            writer.write(PEPTIDE_SECTION, "\" location=\"");
            writer.write(PEPTIDE_SECTION, Integer.toString(site));
            writer.write(PEPTIDE_SECTION, "\" >");
            writer.newLine(PEPTIDE_SECTION);

            CvTerm ptmCvTerm = modification.getUnimodCvTerm();

            if (ptmCvTerm != null) {

                increaseIndent(PEPTIDE_SECTION);
                writeCvTerm(
                        PEPTIDE_SECTION,
                        ptmCvTerm,
                        false
                );
                decreaseIndent(PEPTIDE_SECTION);

            } else {

                // try PSI-MOD instead
                ptmCvTerm = modification.getPsiModCvTerm();

                increaseIndent(PEPTIDE_SECTION);
                writeCvTerm(
                        PEPTIDE_SECTION,
                        ptmCvTerm,
                        false
                );
                decreaseIndent(PEPTIDE_SECTION);

            }

            writer.write(PEPTIDE_SECTION, getCurrentTabSpace(PEPTIDE_SECTION));
            writer.write(PEPTIDE_SECTION, "</Modification>");
            writer.newLine(PEPTIDE_SECTION);

        }

        decreaseIndent(PEPTIDE_SECTION);
        writer.write(PEPTIDE_SECTION, getCurrentTabSpace(PEPTIDE_SECTION));
        writer.write(PEPTIDE_SECTION, "</Peptide>");
        writer.newLine(PEPTIDE_SECTION);

        for (Map.Entry<String, int[]> entry : peptide.getProteinMapping().entrySet()) {

            String accession = entry.getKey();
            int[] indexes = entry.getValue();

            for (int index : indexes) {

                String aaBefore = PeptideUtils.getAaBefore(
                        peptide,
                        accession,
                        index,
                        N_AA,
                        sequenceProvider
                );

                if (aaBefore.length() == 0) {

                    aaBefore = "-";

                }

                String aaAfter = PeptideUtils.getAaAfter(
                        peptide,
                        accession,
                        index,
                        N_AA,
                        sequenceProvider
                );

                if (aaAfter.length() == 0) {

                    aaAfter = "-";

                }

                int peptideStart = index;
                int peptideEnd = index + peptide.getSequence().length();

                String pepEvidenceKey = getPeptideEvidenceKey(
                        accession,
                        peptideStart,
                        peptide.getKey()
                );

                StringBuilder pepEvidenceValueBuilder = new StringBuilder();
                pepEvidenceValueBuilder.append("PepEv_")
                        .append(++peptideEvidenceCounter);
                String pepEvidenceValue = pepEvidenceValueBuilder.toString();

                pepEvidenceIds.put(pepEvidenceKey, pepEvidenceValue);

                writer.write(PEPTIDE_EVIDENCE_SECTION, getCurrentTabSpace(PEPTIDE_EVIDENCE_SECTION));
                writer.write(PEPTIDE_EVIDENCE_SECTION, "<PeptideEvidence isDecoy=\"");
                writer.write(
                        PEPTIDE_EVIDENCE_SECTION,
                        Boolean.toString(
                                PeptideUtils.isDecoy(
                                        peptide,
                                        sequenceProvider
                                )
                        )
                );
                writer.write(PEPTIDE_EVIDENCE_SECTION, "\" pre=\"");
                writer.write(PEPTIDE_EVIDENCE_SECTION, aaBefore);
                writer.write(PEPTIDE_EVIDENCE_SECTION, "\" post=\"");
                writer.write(PEPTIDE_EVIDENCE_SECTION, aaAfter);
                writer.write(PEPTIDE_EVIDENCE_SECTION, "\" start=\"");
                writer.write(PEPTIDE_EVIDENCE_SECTION, Integer.toString(peptideStart + 1));
                writer.write(PEPTIDE_EVIDENCE_SECTION, "\" end=\"");
                writer.write(PEPTIDE_EVIDENCE_SECTION, Integer.toString(peptideEnd + 1));
                writer.write(PEPTIDE_EVIDENCE_SECTION, "\" peptide_ref=\"");
                writer.write(PEPTIDE_EVIDENCE_SECTION, Long.toString(peptide.getKey()));
                writer.write(PEPTIDE_EVIDENCE_SECTION, "\" dBSequence_ref=\"");
                writer.write(PEPTIDE_EVIDENCE_SECTION, accession);
                writer.write(PEPTIDE_EVIDENCE_SECTION, "\" id=\"");
                writer.write(PEPTIDE_EVIDENCE_SECTION, pepEvidenceValue);
                writer.write(PEPTIDE_EVIDENCE_SECTION, "\" />");
                writer.newLine(PEPTIDE_EVIDENCE_SECTION);

            }
        }
    }

    /**
     * Returns the peptide evidence key as string for the given peptide
     * attributes.
     *
     * @param accession the protein accession
     * @param peptideStart the index of the peptide on the protein sequence
     * @param peptideKey the peptide match key
     *
     * @return the peptide evidence key as string for the given peptide
     * attributes
     */
    public static String getPeptideEvidenceKey(
            String accession,
            int peptideStart,
            long peptideKey
    ) {

        String peptideStartAsString = Integer.toString(peptideStart);
        String peptideKeyAsString = Long.toString(peptideKey);

        StringBuilder pepEvidenceKeybuilder = new StringBuilder(
                accession.length()
                + peptideStartAsString.length()
                + peptideKeyAsString.length() + 2
        );

        pepEvidenceKeybuilder
                .append(accession)
                .append('_')
                .append(peptideStartAsString)
                .append('_')
                .append(peptideKeyAsString);

        return pepEvidenceKeybuilder.toString();

    }

    /**
     * Finalizes the peptide evidence section.
     */
    private void finalizePeptideEvidenceSection() {

        decreaseIndent(PEPTIDE_EVIDENCE_SECTION);
        writer.write(PEPTIDE_EVIDENCE_SECTION, getCurrentTabSpace(PEPTIDE_EVIDENCE_SECTION));
        writer.write(PEPTIDE_EVIDENCE_SECTION, "</SequenceCollection>");
        writer.newLine(PEPTIDE_EVIDENCE_SECTION);

    }

    /**
     * Write the analysis collection.
     */
    private void writeAnalysisCollection() {

        writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
        writer.write(ANALYSIS_SECTION, "<AnalysisCollection>");
        writer.newLine(ANALYSIS_SECTION);
        increaseIndent(ANALYSIS_SECTION);

        writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
        writer.write(ANALYSIS_SECTION, "<SpectrumIdentification spectrumIdentificationList_ref=\"SIL_1\" ");
        writer.write(ANALYSIS_SECTION, "spectrumIdentificationProtocol_ref=\"SearchProtocol_1\" id=\"SpecIdent_1\">");
        writer.newLine(ANALYSIS_SECTION);
        increaseIndent(ANALYSIS_SECTION);

        writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
        writer.write(ANALYSIS_SECTION, "<InputSpectra spectraData_ref=\"");
        writer.write(ANALYSIS_SECTION, IoUtil.getFileName(spectrumFile));
        writer.write(ANALYSIS_SECTION, "\"/>");
        writer.newLine(ANALYSIS_SECTION);

        writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
        writer.write(ANALYSIS_SECTION, "<SearchDatabaseRef searchDatabase_ref=\"SearchDB_1\"/>");
        writer.newLine(ANALYSIS_SECTION);

        decreaseIndent(ANALYSIS_SECTION);
        writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
        writer.write(ANALYSIS_SECTION, "</SpectrumIdentification>");
        writer.newLine(ANALYSIS_SECTION);

        // add protein detection
        // @TODO: add activityDate? example: activityDate="2011-03-25T13:33:51
        writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
        writer.write(ANALYSIS_SECTION, "<ProteinDetection proteinDetectionProtocol_ref=\"PeptideShaker_1\" ");
        writer.write(ANALYSIS_SECTION, "proteinDetectionList_ref=\"Protein_groups\" id=\"PD_1\">");
        writer.newLine(ANALYSIS_SECTION);

        increaseIndent(ANALYSIS_SECTION);
        writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
        writer.write(ANALYSIS_SECTION, "<InputSpectrumIdentifications spectrumIdentificationList_ref=\"SIL_1\"/>");
        writer.newLine(ANALYSIS_SECTION);

        decreaseIndent(ANALYSIS_SECTION);
        writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
        writer.write(ANALYSIS_SECTION, "</ProteinDetection>");
        writer.newLine(ANALYSIS_SECTION);

        decreaseIndent(ANALYSIS_SECTION);
        writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
        writer.write(ANALYSIS_SECTION, "</AnalysisCollection>");
        writer.newLine(ANALYSIS_SECTION);

    }

    /**
     * Write the analysis protocol.
     */
    private void writeAnalysisProtocol() {

        writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
        writer.write(ANALYSIS_SECTION, "<AnalysisProtocolCollection>");
        writer.newLine(ANALYSIS_SECTION);
        increaseIndent(ANALYSIS_SECTION);

        // add spectrum identification protocol
        writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
        writer.write(ANALYSIS_SECTION, "<SpectrumIdentificationProtocol analysisSoftware_ref=\"ID_software\" id=\"SearchProtocol_1\">");
        writer.newLine(ANALYSIS_SECTION);
        increaseIndent(ANALYSIS_SECTION);

        // the search type
        writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
        writer.write(ANALYSIS_SECTION, "<SearchType>");
        writer.newLine(ANALYSIS_SECTION);

        increaseIndent(ANALYSIS_SECTION);
        writeCvTerm(
                ANALYSIS_SECTION,
                new CvTerm(
                        "PSI-MS",
                        "MS:1001083",
                        "ms-ms search",
                        null
                )
        );

        decreaseIndent(ANALYSIS_SECTION);
        writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
        writer.write(ANALYSIS_SECTION, "</SearchType>");
        writer.newLine(ANALYSIS_SECTION);

        // the search parameters
        SearchParameters searchParameters = identificationParameters.getSearchParameters();
        writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
        writer.write(ANALYSIS_SECTION, "<AdditionalSearchParams>");
        writer.newLine(ANALYSIS_SECTION);

        increaseIndent(ANALYSIS_SECTION);
        writeCvTerm(
                ANALYSIS_SECTION,
                new CvTerm(
                        "PSI-MS",
                        "MS:1001211",
                        "parent mass type mono",
                        null
                )
        );
        writeCvTerm(
                ANALYSIS_SECTION,
                new CvTerm(
                        "PSI-MS",
                        "MS:1001256",
                        "fragment mass type mono",
                        null
                )
        );
        writeCvTerm(
                ANALYSIS_SECTION,
                new CvTerm(
                        "PSI-MS",
                        "MS:1002491",
                        "modification localization scoring",
                        null
                )
        );

        // @TODO: list all search parameters from the search engines used?
        decreaseIndent(ANALYSIS_SECTION);
        writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
        writer.write(ANALYSIS_SECTION, "</AdditionalSearchParams>");
        writer.newLine(ANALYSIS_SECTION);

        // the modifications
        writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
        writer.write(ANALYSIS_SECTION, "<ModificationParams>");
        writer.newLine(ANALYSIS_SECTION);
        increaseIndent(ANALYSIS_SECTION);

        // create the ptm index map
        for (String modName : searchParameters.getModificationParameters().getAllModifications()) {

            Modification modification = modificationProvider.getModification(modName);
            Double modMass = modification.getMass();
            Integer index = modIndexMap.get(modMass);

            if (index == null) {

                modIndexMap.put(modMass, modIndexMap.size());

            }
        }

        // iterate and add the modifications
        for (String modName
                : searchParameters.getModificationParameters()
                        .getAllModifications()) {

            Modification modification = modificationProvider.getModification(modName);
            ModificationType modificationType = modification.getModificationType();
            double modMass = modification.getMass();

            String aminoAcidsAtTarget;

            if (modificationType == ModificationType.modaa
                    || modificationType == ModificationType.modcaa_peptide
                    || modificationType == ModificationType.modcaa_protein
                    || modificationType == ModificationType.modnaa_peptide
                    || modificationType == ModificationType.modnaa_protein) {

                StringBuilder sb = new StringBuilder();

                for (Character aa : modification.getPattern().getAminoAcidsAtTarget()) {

                    sb.append(aa);

                }

                aminoAcidsAtTarget = sb.toString();

            } else {

                aminoAcidsAtTarget = ".";

            }

            writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
            writer.write(ANALYSIS_SECTION, "<SearchModification residues=\"");
            writer.write(ANALYSIS_SECTION, aminoAcidsAtTarget);
            writer.write(ANALYSIS_SECTION, "\" massDelta=\"");
            writer.write(ANALYSIS_SECTION, Double.toString(modification.getRoundedMass()));
            writer.write(ANALYSIS_SECTION, "\" fixedMod= \"");
            writer.write(ANALYSIS_SECTION, Boolean.toString(searchParameters.getModificationParameters().getFixedModifications().contains(modName)));
            writer.write(ANALYSIS_SECTION, "\" >");
            writer.newLine(ANALYSIS_SECTION);
            increaseIndent(ANALYSIS_SECTION);

            // add modification specificity
            if (modificationType != ModificationType.modaa) {

                writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
                writer.write(ANALYSIS_SECTION, "<SpecificityRules>");
                writer.newLine(ANALYSIS_SECTION);
                increaseIndent(ANALYSIS_SECTION);

                switch (modificationType) {

                    case modn_protein:
                    case modnaa_protein:
                        writeCvTerm(
                                ANALYSIS_SECTION,
                                new CvTerm(
                                        "PSI-MS",
                                        "MS:1002057",
                                        "modification specificity protein N-term",
                                        null
                                )
                        );
                        break;

                    case modn_peptide:
                    case modnaa_peptide:
                        writeCvTerm(
                                ANALYSIS_SECTION,
                                new CvTerm(
                                        "PSI-MS",
                                        "MS:1001189",
                                        "modification specificity peptide N-term",
                                        null
                                )
                        );
                        break;

                    case modc_protein:
                    case modcaa_protein:
                        writeCvTerm(
                                ANALYSIS_SECTION,
                                new CvTerm(
                                        "PSI-MS",
                                        "MS:1002058",
                                        "modification specificity protein C-term",
                                        null
                                )
                        );
                        break;

                    case modc_peptide:
                    case modcaa_peptide:
                        writeCvTerm(
                                ANALYSIS_SECTION,
                                new CvTerm(
                                        "PSI-MS",
                                        "MS:1001190",
                                        "modification specificity peptide C-term",
                                        null
                                )
                        );
                        break;

                    default:
                        break;
                }

                decreaseIndent(ANALYSIS_SECTION);
                writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
                writer.write(ANALYSIS_SECTION, "</SpecificityRules>");
                writer.newLine(ANALYSIS_SECTION);

            }

            // add the modification cv term
            CvTerm ptmCvTerm = modification.getUnimodCvTerm();

            if (ptmCvTerm != null) {

                writeCvTerm(
                        ANALYSIS_SECTION,
                        ptmCvTerm
                );

            } else {

                // try PSI-MOD instead
                ptmCvTerm = modification.getPsiModCvTerm();

                if (ptmCvTerm != null) {

                    writeCvTerm(
                            ANALYSIS_SECTION,
                            ptmCvTerm
                    );

                } else {

                    writeCvTerm(
                            ANALYSIS_SECTION,
                            new CvTerm(
                                    "PSI-MS",
                                    "MS:1001460",
                                    "unknown modification",
                                    null
                            )
                    );
                }
            }

            // add modification type/index
            Integer modIndex = modIndexMap.get(modMass);

            if (modIndex == null) {

                throw new IllegalArgumentException("No index found for PTM " + modification.getName() + " of mass " + modMass + ".");

            }

            writeCvTerm(
                    ANALYSIS_SECTION,
                    new CvTerm(
                            "PSI-MS",
                            "MS:1002504",
                            "modification index",
                            modIndex.toString()
                    )
            );

            decreaseIndent(ANALYSIS_SECTION);
            writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
            writer.write(ANALYSIS_SECTION, "</SearchModification>");
            writer.newLine(ANALYSIS_SECTION);

        }

        decreaseIndent(ANALYSIS_SECTION);
        writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
        writer.write(ANALYSIS_SECTION, "</ModificationParams>");
        writer.newLine(ANALYSIS_SECTION);

        // Digestion
        DigestionParameters digestionPreferences = searchParameters.getDigestionParameters();

        if (digestionPreferences.getCleavageParameter()
                == DigestionParameters.CleavageParameter.unSpecific) {

            writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
            writer.write(ANALYSIS_SECTION, "<Enzymes independent=\"false\">");
            writer.newLine(ANALYSIS_SECTION);

            increaseIndent(ANALYSIS_SECTION);
            writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
            writer.write(ANALYSIS_SECTION, "<Enzyme name=\"unspecific cleavage\">");
            writer.newLine(ANALYSIS_SECTION);

            increaseIndent(ANALYSIS_SECTION);
            writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
            writer.write(ANALYSIS_SECTION, "<EnzymeName>");
            writer.newLine(ANALYSIS_SECTION);

            increaseIndent(ANALYSIS_SECTION);
            CvTerm enzymeCvTerm = new CvTerm(
                    "PSI-MS",
                    "MS:1001091",
                    "unspecific cleavage",
                    null
            );
            writeCvTerm(
                    ANALYSIS_SECTION,
                    enzymeCvTerm
            );

            decreaseIndent(ANALYSIS_SECTION);
            writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
            writer.write(ANALYSIS_SECTION, "</EnzymeName>");
            writer.newLine(ANALYSIS_SECTION);

            decreaseIndent(ANALYSIS_SECTION);
            writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
            writer.write(ANALYSIS_SECTION, "</Enzyme>");
            writer.newLine(ANALYSIS_SECTION);

        } else if (digestionPreferences.getCleavageParameter()
                == DigestionParameters.CleavageParameter.wholeProtein) {

            writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
            writer.write(ANALYSIS_SECTION, "<Enzymes independent=\"false\">");
            writer.newLine(ANALYSIS_SECTION);
            increaseIndent(ANALYSIS_SECTION);

            writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
            writer.write(ANALYSIS_SECTION, "<Enzyme name=\"NoEnzyme\">");
            writer.newLine(ANALYSIS_SECTION);
            increaseIndent(ANALYSIS_SECTION);

            writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
            writer.write(ANALYSIS_SECTION, "<EnzymeName>");
            writer.newLine(ANALYSIS_SECTION);
            increaseIndent(ANALYSIS_SECTION);

            CvTerm enzymeCvTerm = new CvTerm(
                    "PSI-MS",
                    "MS:1001955",
                    "NoEnzyme",
                    null
            );
            writeCvTerm(ANALYSIS_SECTION, enzymeCvTerm);

            decreaseIndent(ANALYSIS_SECTION);
            writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
            writer.write(ANALYSIS_SECTION, "</EnzymeName>");
            writer.newLine(ANALYSIS_SECTION);

            decreaseIndent(ANALYSIS_SECTION);
            writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
            writer.write(ANALYSIS_SECTION, "</Enzyme>");
            writer.newLine(ANALYSIS_SECTION);

        } else {

            ArrayList<Enzyme> enzymes = digestionPreferences.getEnzymes();

            writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
            writer.write(ANALYSIS_SECTION, "<Enzymes independent=\"");
            writer.write(ANALYSIS_SECTION, Boolean.toString(enzymes.size() > 1));
            writer.write(ANALYSIS_SECTION, "\">");
            writer.newLine(ANALYSIS_SECTION);
            increaseIndent(ANALYSIS_SECTION);

            for (Enzyme enzyme : enzymes) {

                String enzymeName = enzyme.getName();
                writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
                writer.write(ANALYSIS_SECTION, "<Enzyme missedCleavages=\"");
                writer.write(ANALYSIS_SECTION, Integer.toString(digestionPreferences.getnMissedCleavages(enzymeName)));
                writer.write(ANALYSIS_SECTION, "\" semiSpecific=\"");
                writer.write(ANALYSIS_SECTION, Boolean.toString(digestionPreferences.getSpecificity(enzymeName) == DigestionParameters.Specificity.semiSpecific));
                writer.write(ANALYSIS_SECTION, "\" ");
                //+ "cTermGain=\"OH\" " // Element formula gained at CTerm
                //+ "nTermGain=\"H\" " // Element formula gained at NTerm
                // @TODO: add <SiteRegexp><![CDATA[(?<=[KR])(?!P)]]></SiteRegexp>?
                writer.write(ANALYSIS_SECTION, "id=\"Enz1\" name=\"");
                writer.write(ANALYSIS_SECTION, enzyme.getName());
                writer.write(ANALYSIS_SECTION, "\">");
                writer.newLine(ANALYSIS_SECTION);
                increaseIndent(ANALYSIS_SECTION);

                writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
                writer.write(ANALYSIS_SECTION, "<EnzymeName>");
                writer.newLine(ANALYSIS_SECTION);

                increaseIndent(ANALYSIS_SECTION);
                CvTerm enzymeCvTerm = enzyme.getCvTerm();

                if (enzymeCvTerm != null) {

                    writeCvTerm(ANALYSIS_SECTION, enzymeCvTerm);

                } else {

                    writeUserParam(ANALYSIS_SECTION, enzyme.getName());

                }

                decreaseIndent(ANALYSIS_SECTION);
                writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
                writer.write(ANALYSIS_SECTION, "</EnzymeName>");
                writer.newLine(ANALYSIS_SECTION);

                decreaseIndent(ANALYSIS_SECTION);
                writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
                writer.write(ANALYSIS_SECTION, "</Enzyme>");
                writer.newLine(ANALYSIS_SECTION);

                decreaseIndent(ANALYSIS_SECTION);

            }
        }

        writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
        writer.write(ANALYSIS_SECTION, "</Enzymes>");
        writer.newLine(ANALYSIS_SECTION);

        // fragment tolerance
        writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
        writer.write(ANALYSIS_SECTION, "<FragmentTolerance>");
        writer.newLine(ANALYSIS_SECTION);
        increaseIndent(ANALYSIS_SECTION);

        String fragmentIonToleranceUnit;
        String unitAccession;

        switch (searchParameters.getFragmentAccuracyType()) {

            case DA:
                fragmentIonToleranceUnit = "dalton";
                unitAccession = "UO:0000221";
                break;

            case PPM:
                fragmentIonToleranceUnit = "parts per million";
                unitAccession = "UO:0000169";
                break;

            default:
                throw new UnsupportedOperationException("CV term not implemented for fragment accuracy in " + searchParameters.getFragmentAccuracyType() + ".");

        }
        writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
        writer.write(ANALYSIS_SECTION, "<cvParam accession=\"MS:1001412\" cvRef=\"PSI-MS\" unitCvRef=\"UO\" unitName=\"");
        writer.write(ANALYSIS_SECTION, fragmentIonToleranceUnit);

        writer.write(ANALYSIS_SECTION, "\" unitAccession=\"");
        writer.write(ANALYSIS_SECTION, unitAccession);

        writer.write(ANALYSIS_SECTION, "\" value=\"");
        writer.write(ANALYSIS_SECTION, Double.toString(searchParameters.getFragmentIonAccuracy()));
        writer.write(ANALYSIS_SECTION, "\" ");
        writer.write(ANALYSIS_SECTION, "name=\"search tolerance plus value\" />");
        writer.newLine(ANALYSIS_SECTION);

        writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
        writer.write(ANALYSIS_SECTION, "<cvParam accession=\"MS:1001413\" cvRef=\"PSI-MS\" unitCvRef=\"UO\" unitName=\"");
        writer.write(ANALYSIS_SECTION, fragmentIonToleranceUnit);

        writer.write(ANALYSIS_SECTION, "\" unitAccession=\"");
        writer.write(ANALYSIS_SECTION, unitAccession);

        writer.write(ANALYSIS_SECTION, "\" value=\"");
        writer.write(ANALYSIS_SECTION, Double.toString(searchParameters.getFragmentIonAccuracy()));
        writer.write(ANALYSIS_SECTION, "\" name=\"search tolerance minus value\" />");
        writer.newLine(ANALYSIS_SECTION);

        decreaseIndent(ANALYSIS_SECTION);
        writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
        writer.write(ANALYSIS_SECTION, "</FragmentTolerance>");
        writer.newLine(ANALYSIS_SECTION);

        // precursor tolerance
        writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
        writer.write(ANALYSIS_SECTION, "<ParentTolerance>");
        writer.newLine(ANALYSIS_SECTION);
        increaseIndent(ANALYSIS_SECTION);

        String precursorIonToleranceUnit;
        switch (searchParameters.getPrecursorAccuracyType()) {

            case DA:
                precursorIonToleranceUnit = "dalton";
                break;

            case PPM:
                precursorIonToleranceUnit = "parts per million";
                break;

            default:
                throw new UnsupportedOperationException("CV term not implemented for precursor accuracy in " + searchParameters.getFragmentAccuracyType() + ".");

        }
        writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
        writer.write(ANALYSIS_SECTION, "<cvParam accession=\"MS:1001412\" cvRef=\"PSI-MS\" unitCvRef=\"UO\" unitName=\"");
        writer.write(ANALYSIS_SECTION, precursorIonToleranceUnit);

        writer.write(ANALYSIS_SECTION, "\" unitAccession=\"UO:0000169\" value=\"");
        writer.write(ANALYSIS_SECTION, Double.toString(searchParameters.getPrecursorAccuracy()));
        writer.write(ANALYSIS_SECTION, "\" name=\"search tolerance plus value\" />");
        writer.newLine(ANALYSIS_SECTION);

        writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
        writer.write(ANALYSIS_SECTION, "<cvParam accession=\"MS:1001413\" cvRef=\"PSI-MS\" unitCvRef=\"UO\" unitName=\"");
        writer.write(ANALYSIS_SECTION, precursorIonToleranceUnit);

        writer.write(ANALYSIS_SECTION, "\" unitAccession=\"UO:0000169\" value=\"");
        writer.write(ANALYSIS_SECTION, Double.toString(searchParameters.getPrecursorAccuracy()));
        writer.write(ANALYSIS_SECTION, "\" name=\"search tolerance minus value\" />");

        decreaseIndent(ANALYSIS_SECTION);
        writer.newLine(ANALYSIS_SECTION);

        writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
        writer.write(ANALYSIS_SECTION, "</ParentTolerance>");
        writer.newLine(ANALYSIS_SECTION);

        // thresholds
        writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
        writer.write(ANALYSIS_SECTION, "<Threshold>");
        writer.newLine(ANALYSIS_SECTION);
        increaseIndent(ANALYSIS_SECTION);

        writeCvTerm(
                ANALYSIS_SECTION,
                new CvTerm(
                        "PSI-MS",
                        "MS:1001494",
                        "no threshold",
                        null
                )
        );

        decreaseIndent(ANALYSIS_SECTION);
        writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
        writer.write(ANALYSIS_SECTION, "</Threshold>");
        writer.newLine(ANALYSIS_SECTION);

        decreaseIndent(ANALYSIS_SECTION);
        writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
        writer.write(ANALYSIS_SECTION, "</SpectrumIdentificationProtocol>");
        writer.newLine(ANALYSIS_SECTION);

        decreaseIndent(ANALYSIS_SECTION);
        writer.write(ANALYSIS_SECTION, getCurrentTabSpace(ANALYSIS_SECTION));
        writer.write(ANALYSIS_SECTION, "</AnalysisProtocolCollection>");
        writer.newLine(ANALYSIS_SECTION);

    }

    /**
     * Sets up the data collection section.
     */
    private void setupDataCollection() {

        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "<DataCollection>");
        writer.newLine(DATA_SECTION);
        increaseIndent(DATA_SECTION);

        writeInputFileDetails();
        setupDataAnalysis();

    }

    /**
     * Sets up the data collection section.
     */
    private void finalizeDataCollection() {

        finalizeDataAnalysis();

        decreaseIndent(DATA_SECTION);
        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "</DataCollection>");
        writer.newLine(DATA_SECTION);

    }

    /**
     * Write the input file details.
     */
    private void writeInputFileDetails() {

        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "<Inputs>");
        writer.newLine(DATA_SECTION);
        increaseIndent(DATA_SECTION);

        int sourceFileCounter = 1;

        // add the search result files
        // @TODO: add MS:1000568 - MD5?
//            FileInputStream fis = new FileInputStream(new File("foo"));
//            String md5 = DigestUtils.md5Hex(fis);
//            fis.close();
        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "<SourceFile location=\"");
        writer.write(DATA_SECTION, searchEngineFile.toURI().toString());
        writer.write(DATA_SECTION, "\" id=\"SourceFile_");
        writer.write(DATA_SECTION, Integer.toString(sourceFileCounter++));
        writer.write(DATA_SECTION, "\">");
        writer.newLine(DATA_SECTION);
        increaseIndent(DATA_SECTION);

        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "<FileFormat>");
        writer.newLine(DATA_SECTION);
        increaseIndent(DATA_SECTION);

        for (String algorithmName : searchEngines.keySet()) {

            Advocate advocate = Advocate.getAdvocate(algorithmName);

            int advocateIndex = advocate.getIndex();

            if (advocateIndex == Advocate.mascot.getIndex()) {

                writeCvTerm(
                        DATA_SECTION,
                        new CvTerm(
                                "PSI-MS",
                                "MS:1001199",
                                "Mascot DAT format",
                                null
                        )
                );

            } else if (advocateIndex == Advocate.xtandem.getIndex()) {

                writeCvTerm(
                        DATA_SECTION,
                        new CvTerm(
                                "PSI-MS",
                                "MS:1001401",
                                "X!Tandem xml format",
                                null
                        )
                );

            } else if (advocateIndex == Advocate.omssa.getIndex()) {

                writeCvTerm(
                        DATA_SECTION,
                        new CvTerm(
                                "PSI-MS",
                                "MS:1001400",
                                "OMSSA xml format",
                                null
                        )
                );

            } else if (advocateIndex == Advocate.msgf.getIndex() || advocateIndex == Advocate.myriMatch.getIndex()) {

                writeCvTerm(
                        DATA_SECTION,
                        new CvTerm(
                                "PSI-MS",
                                "MS:1002073",
                                "mzIdentML format",
                                null
                        )
                );

            } else if (advocateIndex == Advocate.msAmanda.getIndex()) {

                writeCvTerm(
                        DATA_SECTION,
                        new CvTerm(
                                "PSI-MS",
                                "MS:1002459",
                                "MS Amanda csv format",
                                null
                        )
                );

            } else if (advocateIndex == Advocate.comet.getIndex()) {

                writeCvTerm(
                        DATA_SECTION,
                        new CvTerm(
                                "PSI-MS",
                                "MS:1001421",
                                "pepXML format",
                                null
                        )
                );

            } else if (advocateIndex == Advocate.tide.getIndex()) {

                writeCvTerm(
                        DATA_SECTION,
                        new CvTerm(
                                "PSI-MS",
                                "MS:1000914",
                                "tab delimited text format",
                                null
                        )
                );

            } else if (advocateIndex == Advocate.andromeda.getIndex()) {

                writeCvTerm(
                        DATA_SECTION,
                        new CvTerm(
                                "PSI-MS",
                                "MS:1002576",
                                "Andromeda result file",
                                null
                        )
                );

            } else {
                // no cv term available for the given advocate...
                // @TODO: should add for IdentiPy (pepxml only?) and Morpheus (mxid or pepxml?)
            }
        }

        // @TODO: add children of MS:1000561 - data file checksum type?
        decreaseIndent(DATA_SECTION);
        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "</FileFormat>");
        writer.newLine(DATA_SECTION);

        decreaseIndent(DATA_SECTION);
        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "</SourceFile>");
        writer.newLine(DATA_SECTION);

        // add the database
        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "<SearchDatabase numDatabaseSequences=\"");
        writer.write(DATA_SECTION, Integer.toString(fastaSummary.nSequences));
        writer.write(DATA_SECTION, "\" location=\"");
        writer.write(DATA_SECTION, fastaFile.toURI().toString());
        writer.write(DATA_SECTION, "\" id=\"SearchDB_1\">");
        writer.newLine(DATA_SECTION);
        increaseIndent(DATA_SECTION);

        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "<FileFormat>");
        writer.newLine(DATA_SECTION);
        increaseIndent(DATA_SECTION);

        writeCvTerm(
                DATA_SECTION,
                new CvTerm(
                        "PSI-MS",
                        "MS:1001348",
                        "FASTA format",
                        null
                )
        );
        decreaseIndent(DATA_SECTION);

        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "</FileFormat>");
        writer.newLine(DATA_SECTION);

        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "<DatabaseName>");
        writer.newLine(DATA_SECTION);
        increaseIndent(DATA_SECTION);

        writeUserParam(DATA_SECTION, fastaFile.getName()); // @TODO: add database type? children of MS:1001013 - database name??? for example: MS:1001104 (database UniProtKB/Swiss-Prot)

        decreaseIndent(DATA_SECTION);
        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "</DatabaseName>");
        writer.newLine(DATA_SECTION);

        writeCvTerm(
                DATA_SECTION,
                new CvTerm(
                        "PSI-MS",
                        "MS:1001073",
                        "database type amino acid",
                        null
                )
        );

        decreaseIndent(DATA_SECTION);
        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "</SearchDatabase>");
        writer.newLine(DATA_SECTION);

        // add the spectra location
        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "<SpectraData location=\"");
        writer.write(DATA_SECTION, spectrumFile.toURI().toString());
        writer.write(DATA_SECTION, "\" id=\"");
        writer.write(DATA_SECTION, IoUtil.getFileName(spectrumFile));
        writer.write(DATA_SECTION, "\" name=\"");
        writer.write(DATA_SECTION, spectrumFile.getName());
        writer.write(DATA_SECTION, "\">");
        writer.newLine(DATA_SECTION);
        increaseIndent(DATA_SECTION);

        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "<FileFormat>");
        writer.newLine(DATA_SECTION);
        increaseIndent(DATA_SECTION);

        writeCvTerm(
                DATA_SECTION,
                new CvTerm(
                        "PSI-MS",
                        "MS:1001062",
                        "Mascot MGF format",
                        null
                )
        );

        decreaseIndent(DATA_SECTION);
        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "</FileFormat>");
        writer.newLine(DATA_SECTION);

        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "<SpectrumIDFormat>");
        writer.newLine(DATA_SECTION);
        increaseIndent(DATA_SECTION);

        writeCvTerm(
                DATA_SECTION,
                new CvTerm(
                        "PSI-MS",
                        "MS:1000774",
                        "multiple peak list nativeID format",
                        null
                )
        );

        decreaseIndent(DATA_SECTION);
        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "</SpectrumIDFormat>");
        writer.newLine(DATA_SECTION);

        decreaseIndent(DATA_SECTION);
        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "</SpectraData>");
        writer.newLine(DATA_SECTION);

        decreaseIndent(DATA_SECTION);
        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "</Inputs>");
        writer.newLine(DATA_SECTION);

    }

    /**
     * Sets up the data analysis section.
     */
    private void setupDataAnalysis() {

        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "<AnalysisData>");
        writer.newLine(DATA_SECTION);
        increaseIndent(DATA_SECTION);

        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "<SpectrumIdentificationList id=\"SIL_1\">");
        writer.newLine(DATA_SECTION);
        increaseIndent(DATA_SECTION);

        writeFragmentationTable();

    }

    /**
     * Finalizes the data analysis section.
     */
    private void finalizeDataAnalysis() {

        //writeCvTerm(new CvTerm("PSI-MS", "MS:1002439", "final PSM list", null)); // @TODO: add children of MS:1001184 (search statistics)?
        decreaseIndent(DATA_SECTION);
        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "</SpectrumIdentificationList>");
        writer.newLine(DATA_SECTION);

        decreaseIndent(DATA_SECTION);
        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "</AnalysisData>");
        writer.newLine(DATA_SECTION);

    }

    /**
     * Write the fragmentation table.
     */
    private void writeFragmentationTable() {

        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "<FragmentationTable>");
        writer.newLine(DATA_SECTION);
        increaseIndent(DATA_SECTION);

        // mz
        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "<Measure id=\"Measure_MZ\">");
        writer.newLine(DATA_SECTION);
        increaseIndent(DATA_SECTION);

        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "<cvParam cvRef=\"PSI-MS\" accession=\"MS:1001225\" name=\"product ion m/z\" unitCvRef=\"PSI-MS\" unitAccession=\"MS:1000040\" unitName=\"m/z\" />");
        writer.newLine(DATA_SECTION);

        decreaseIndent(DATA_SECTION);
        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "</Measure>");
        writer.newLine(DATA_SECTION);

        // intensity
        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "<Measure id=\"Measure_Int\">");
        writer.newLine(DATA_SECTION);
        increaseIndent(DATA_SECTION);

        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "<cvParam cvRef=\"PSI-MS\" accession=\"MS:1001226\" name=\"product ion intensity\" unitCvRef=\"PSI-MS\" unitAccession=\"MS:1000131\" unitName=\"number of detector counts\"/>");
        writer.newLine(DATA_SECTION);

        decreaseIndent(DATA_SECTION);
        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "</Measure>");
        writer.newLine(DATA_SECTION);

        // mass error
        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "<Measure id=\"Measure_Error\">");
        writer.newLine(DATA_SECTION);
        increaseIndent(DATA_SECTION);

        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "<cvParam cvRef=\"PSI-MS\" accession=\"MS:1001227\" name=\"product ion m/z error\" unitCvRef=\"PSI-MS\" unitAccession=\"MS:1000040\" unitName=\"m/z\"/>");
        writer.newLine(DATA_SECTION);

        decreaseIndent(DATA_SECTION);
        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "</Measure>");
        writer.newLine(DATA_SECTION);

        decreaseIndent(DATA_SECTION);
        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "</FragmentationTable>");
        writer.newLine(DATA_SECTION);

    }

    /**
     * Writes a spectrum identification result.
     *
     * @param spectrumFile The name of the file containing the spectrum.
     * @param spectrumTitle The title of the spectrum.
     * @param peptideAssumptions The peptide assumptions for this spectrum.
     * @param modificationLocalizationScores The modification localization
     * scores for the different peptides.
     * @param peptideSpectrumAnnotator The annotator to use to annotate spectra.
     */
    private void writeSpectrumIdentificationResult(
            String spectrumFile,
            String spectrumTitle,
            ArrayList<PeptideAssumption> peptideAssumptions,
            ArrayList<TreeMap<Double, HashMap<Integer, Double>>> modificationLocalizationScores,
            PeptideSpectrumAnnotator peptideSpectrumAnnotator
    ) {

        int spectrumMatchIndex = ++psmCount;

        int spectrumIndex = spectrumTitleToIndexMap.get(spectrumTitle);

        String spectrumIdentificationResultItemKey = String.join("",
                "SIR_",
                Integer.toString(spectrumMatchIndex)
        );

        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "<SpectrumIdentificationResult spectraData_ref=\"");
        writer.write(DATA_SECTION, spectrumFile);
        writer.write(DATA_SECTION, "\" spectrumID=\"index=");
        writer.write(DATA_SECTION, Integer.toString(spectrumIndex));
        writer.write(DATA_SECTION, "\" id=\"");
        writer.write(DATA_SECTION, spectrumIdentificationResultItemKey);
        writer.write(DATA_SECTION, "\">");
        writer.newLine(DATA_SECTION);
        increaseIndent(DATA_SECTION);

        for (int i = 0; i < peptideAssumptions.size(); i++) {

            String spectrumIdentificationItemKey = String.join("",
                    "SII_",
                    Integer.toString(spectrumMatchIndex),
                    "_",
                    Integer.toString(i)
            );

            writeSpectrumIdentificationItem(
                    spectrumFile,
                    spectrumTitle,
                    spectrumIdentificationItemKey,
                    peptideAssumptions.get(i),
                    modificationLocalizationScores.get(i),
                    peptideSpectrumAnnotator
            );

        }

        // add the spectrum title
        writeCvTerm(
                DATA_SECTION,
                new CvTerm(
                        "PSI-MS",
                        "MS:1000796",
                        "spectrum title",
                        spectrumTitle
                )
        );

        // add the precursor retention time
        double precursorRt = spectrumProvider.getPrecursorRt(spectrumFile, spectrumTitle);

        if (!Double.isNaN(precursorRt)) {

            writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
            writer.write(DATA_SECTION, "<cvParam cvRef=\"PSI-MS\" accession=\"MS:1000894\" name=\"retention time\" value=\"");
            writer.write(DATA_SECTION, Double.toString(precursorRt));
            writer.write(DATA_SECTION, "\" unitCvRef=\"UO\" unitAccession=\"UO:0000010\" unitName=\"second\"/>");
            writer.newLine(DATA_SECTION);

        }

        decreaseIndent(DATA_SECTION);
        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "</SpectrumIdentificationResult>");
        writer.newLine(DATA_SECTION);

    }

    /**
     * Write a spectrum identification item.
     *
     * @param spectrumFile The name of the file of the spectrum.
     * @param spectrumTitle The title of the spectrum.
     * @param peptideAssumption The peptide assumption.
     * @param modificationLocalizationScores The modification localization
     * scores of the peptide.
     * @param peptideSpectrumAnnotator The peptide spectrum annotator to use.
     */
    private void writeSpectrumIdentificationItem(
            String spectrumFile,
            String spectrumTitle,
            String spectrumIdentificationItemKey,
            PeptideAssumption peptideAssumption,
            TreeMap<Double, HashMap<Integer, Double>> modificationLocalizationScores,
            PeptideSpectrumAnnotator peptideSpectrumAnnotator
    ) {

        Peptide peptide = peptideAssumption.getPeptide();
        long peptideKey = peptide.getKey();

        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "<SpectrumIdentificationItem peptide_ref=\"");
        writer.write(DATA_SECTION, Long.toString(peptideKey));
        writer.write(DATA_SECTION, "\" calculatedMassToCharge=\"");
        writer.write(DATA_SECTION, Double.toString(peptideAssumption.getTheoreticMz()));
        writer.write(DATA_SECTION, "\" experimentalMassToCharge=\"");
        writer.write(DATA_SECTION, Double.toString(spectrumProvider.getPrecursorMz(spectrumFile, spectrumTitle)));
        writer.write(DATA_SECTION, "\" chargeState=\"");
        writer.write(DATA_SECTION, Integer.toString(peptideAssumption.getIdentificationCharge()));
        writer.write(DATA_SECTION, "\" id=\"");
        writer.write(DATA_SECTION, spectrumIdentificationItemKey);
        writer.write(DATA_SECTION, "\">");
        writer.newLine(DATA_SECTION);
        increaseIndent(DATA_SECTION);

        // add the peptide evidence references
        // get all the possible parent proteins
        TreeMap<String, int[]> proteinMapping = peptide.getProteinMapping();
        String peptideSequence = peptide.getSequence();

        // iterate all the possible protein parents for each peptide
        for (Map.Entry<String, int[]> entry : proteinMapping.entrySet()) {

            String accession = entry.getKey();
            int[] indexes = entry.getValue();

            for (int index : indexes) {

                String pepEvidenceKey = getPeptideEvidenceKey(
                        accession,
                        index,
                        peptideKey
                );
                String peptideEvidenceId = pepEvidenceIds.get(pepEvidenceKey);

                writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
                writer.write(DATA_SECTION, "<PeptideEvidenceRef peptideEvidence_ref=\"");
                writer.write(DATA_SECTION, peptideEvidenceId);
                writer.write(DATA_SECTION, "\"/>");
                writer.newLine(DATA_SECTION);

            }
        }

        // add the fragment ion annotation
        AnnotationParameters annotationParameters = identificationParameters.getAnnotationParameters();
        Spectrum spectrum = spectrumProvider.getSpectrum(spectrumFile, spectrumTitle);
        ModificationParameters modificationParameters = identificationParameters.getSearchParameters().getModificationParameters();
        SequenceMatchingParameters modificationSequenceMatchingParameters = identificationParameters.getModificationLocalizationParameters().getSequenceMatchingParameters();
        SpecificAnnotationParameters specificAnnotationParameters = annotationParameters.getSpecificAnnotationParameters(
                spectrumFile,
                spectrumTitle,
                peptideAssumption,
                modificationParameters,
                sequenceProvider,
                modificationSequenceMatchingParameters,
                peptideSpectrumAnnotator
        );
        IonMatch[] matches = peptideSpectrumAnnotator.getSpectrumAnnotation(
                annotationParameters,
                specificAnnotationParameters,
                spectrumFile,
                spectrumTitle,
                spectrum,
                peptideAssumption.getPeptide(),
                modificationParameters,
                sequenceProvider,
                modificationSequenceMatchingParameters
        );

        // organize the fragment ions by ion type
        HashMap<String, HashMap<Integer, ArrayList<IonMatch>>> allFragmentIons = new HashMap<>();

        for (IonMatch ionMatch : matches) {

            if (ionMatch.ion.getType() == Ion.IonType.PEPTIDE_FRAGMENT_ION
                    || ionMatch.ion.getType() == Ion.IonType.IMMONIUM_ION
                    || ionMatch.ion.getType() == Ion.IonType.PRECURSOR_ION
                    || ionMatch.ion.getType() == Ion.IonType.REPORTER_ION
                    || ionMatch.ion.getType() == Ion.IonType.RELATED_ION) { // @TODO: what about tag fragment ion?

                CvTerm fragmentIonCvTerm = ionMatch.ion.getPsiMsCvTerm();
                Integer charge = ionMatch.charge;

                // check if there is less than the maximum number of allowed neutral losses
                boolean neutralLossesTestPassed = true;

                if (ionMatch.ion.hasNeutralLosses()) {

                    neutralLossesTestPassed = ionMatch.ion.getNeutralLosses().length <= MAX_NEUTRAL_LOSSES;

                }

                // only include ions with cv terms
                if (fragmentIonCvTerm != null && neutralLossesTestPassed) {

                    String fragmentIonName = ionMatch.ion.getName();

                    if (!allFragmentIons.containsKey(fragmentIonName)) {

                        allFragmentIons.put(fragmentIonName, new HashMap<>(1));

                    }

                    if (!allFragmentIons.get(fragmentIonName).containsKey(charge)) {

                        allFragmentIons.get(fragmentIonName).put(charge, new ArrayList<>(1));

                    }

                    allFragmentIons.get(fragmentIonName).get(charge).add(ionMatch);

                }
            }
        }

        if (!allFragmentIons.isEmpty()) {

            writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
            writer.write(DATA_SECTION, "<Fragmentation>");
            writer.newLine(DATA_SECTION);
            increaseIndent(DATA_SECTION);

            // add the fragment ions
            Iterator<String> fragmentTypeIterator = allFragmentIons.keySet().iterator();

            while (fragmentTypeIterator.hasNext()) {

                String fragmentType = fragmentTypeIterator.next();
                Iterator<Integer> chargeTypeIterator = allFragmentIons.get(fragmentType).keySet().iterator();

                while (chargeTypeIterator.hasNext()) {

                    Integer fragmentCharge = chargeTypeIterator.next();
                    ArrayList<IonMatch> ionMatches = allFragmentIons.get(fragmentType).get(fragmentCharge);
                    Ion currentIon = ionMatches.get(0).ion;
                    CvTerm fragmentIonCvTerm = currentIon.getPsiMsCvTerm();

                    StringBuilder indexes = new StringBuilder();
                    StringBuilder mzValues = new StringBuilder();
                    StringBuilder intensityValues = new StringBuilder();
                    StringBuilder errorValues = new StringBuilder();

                    // get the fragment ion details
                    for (IonMatch ionMatch : ionMatches) {

                        if (ionMatch.ion instanceof PeptideFragmentIon) {

                            indexes.append(((PeptideFragmentIon) ionMatch.ion).getNumber())
                                    .append(' ');

                        } else if (ionMatch.ion instanceof ImmoniumIon) {

                            // get the indexes of the corresponding residues
                            char residue = ((ImmoniumIon) ionMatch.ion).aa;
                            char[] peptideAsArray = peptideSequence.toCharArray();

                            for (int i = 0; i < peptideAsArray.length; i++) {

                                if (peptideAsArray[i] == residue) {

                                    indexes.append((i + 1))
                                            .append(' ');

                                }
                            }

                        } else if (ionMatch.ion instanceof ReporterIon
                                || ionMatch.ion instanceof RelatedIon // @TODO: request cv terms for related ions?
                                || ionMatch.ion instanceof PrecursorIon) {

                            indexes.append('0');

                        }

                        mzValues.append(ionMatch.peakMz)
                                .append(' ');
                        intensityValues.append(ionMatch.peakIntensity)
                                .append(' ');
                        errorValues.append(ionMatch.getAbsoluteError())
                                .append(' ');

                    }

                    // add the supported fragment ions
                    if (fragmentIonCvTerm != null) {

                        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
                        writer.write(DATA_SECTION, "<IonType charge=\"");
                        writer.write(DATA_SECTION, Integer.toString(fragmentCharge));
                        writer.write(DATA_SECTION, "\" index=\"");
                        writer.write(DATA_SECTION, indexes.toString().trim());
                        writer.write(DATA_SECTION, "\">");
                        writer.newLine(DATA_SECTION);
                        increaseIndent(DATA_SECTION);

                        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
                        writer.write(DATA_SECTION, "<FragmentArray measure_ref=\"Measure_MZ\" values=\"");
                        writer.write(DATA_SECTION, mzValues.toString().trim());
                        writer.write(DATA_SECTION, "\"/>");
                        writer.newLine(DATA_SECTION);

                        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
                        writer.write(DATA_SECTION, "<FragmentArray measure_ref=\"Measure_Int\" values=\"");
                        writer.write(DATA_SECTION, intensityValues.toString().trim());
                        writer.write(DATA_SECTION, "\"/>");
                        writer.newLine(DATA_SECTION);

                        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
                        writer.write(DATA_SECTION, "<FragmentArray measure_ref=\"Measure_Error\" values=\"");
                        writer.write(DATA_SECTION, errorValues.toString().trim());
                        writer.write(DATA_SECTION, "\"/>");
                        writer.newLine(DATA_SECTION);

                        // add the cv term for the fragment ion type
                        writeCvTerm(DATA_SECTION, fragmentIonCvTerm);

                        // add the cv term for the neutral losses
                        if (currentIon.getNeutralLosses() != null) {

                            int neutralLossesCount = currentIon.getNeutralLosses().length;

                            if (neutralLossesCount > MAX_NEUTRAL_LOSSES) {

                                throw new IllegalArgumentException("A maximum of " + MAX_NEUTRAL_LOSSES + " neutral losses is supported.");

                            } else {

                                for (NeutralLoss tempNeutralLoss : currentIon.getNeutralLosses()) {

                                    writeCvTerm(DATA_SECTION, tempNeutralLoss.getPsiMsCvTerm());

                                }
                            }
                        }

                        decreaseIndent(DATA_SECTION);
                        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
                        writer.write(DATA_SECTION, "</IonType>");
                        writer.newLine(DATA_SECTION);

                    }
                }
            }

            decreaseIndent(DATA_SECTION);
            writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
            writer.write(DATA_SECTION, "</Fragmentation>");
            writer.newLine(DATA_SECTION);

        }

        // Write modification localization scores.
        for (Entry<Double, HashMap<Integer, Double>> entry1 : modificationLocalizationScores.entrySet()) {

            double modMass = entry1.getKey();

            for (Entry<Integer, Double> entry2 : entry1.getValue().entrySet()) {

                int site = entry2.getKey();

                String modName = getModificationName(
                        modMass,
                        peptide,
                        site
                );

                Integer modIndex = modIndexMap.get(modMass);

                if (modIndex == null) {

                    throw new IllegalArgumentException("No index found for modification " + modName + " of mass " + modMass + ".");

                }

                double score = entry2.getValue();

                StringBuilder sb = new StringBuilder();
                sb.append(modIndex)
                        .append(':')
                        .append(score)
                        .append(':')
                        .append(site)
                        .append(':')
                        .append("true"); //@TODO: mandatory?

                writeCvTerm(
                        DATA_SECTION,
                        new CvTerm(
                                "PSI-MS",
                                "MS:1001969",
                                "phosphoRS score",
                                sb.toString()
                        )
                );

            }
        }

        // add the search engine score
        int advocate = peptideAssumption.getAdvocate();
        double score = peptideAssumption.getRawScore();

        if (advocate == Advocate.xtandem.getIndex()) {

            writeCvTerm(
                    DATA_SECTION,
                    new CvTerm(
                            "PSI-MS",
                            "MS:1001330",
                            "X!Tandem:expect",
                            Double.toString(score)
                    )
            );

        } else if (advocate == Advocate.comet.getIndex()) {

            writeCvTerm(
                    DATA_SECTION,
                    new CvTerm(
                            "PSI-MS",
                            "MS:1002257",
                            "Comet:expectation value",
                            Double.toString(score)
                    )
            );

        } else if (advocate == Advocate.myriMatch.getIndex()) {

            writeCvTerm(
                    DATA_SECTION,
                    new CvTerm(
                            "PSI-MS",
                            "MS:1001589",
                            "MyriMatch:MVH",
                            Double.toString(score)
                    )
            );

        } else if (advocate == Advocate.msgf.getIndex()) {

            writeCvTerm(
                    DATA_SECTION,
                    new CvTerm(
                            "PSI-MS",
                            "MS:1002052",
                            "MS-GF:SpecEValue",
                            Double.toString(score)
                    )
            );

        } else if (advocate == Advocate.omssa.getIndex()) {

            writeCvTerm(
                    DATA_SECTION,
                    new CvTerm(
                            "PSI-MS",
                            "MS:1001328",
                            "OMSSA:evalue",
                            Double.toString(score)
                    )
            );

        } else if (advocate == Advocate.mascot.getIndex()) {

            writeCvTerm(
                    DATA_SECTION,
                    new CvTerm(
                            "PSI-MS",
                            "MS:1001172",
                            "Mascot:expectation value",
                            Double.toString(score)
                    )
            );

        } else {

            writeUserParam(
                    DATA_SECTION,
                    String.join("", Advocate.getAdvocate(advocate).getName(), " score"),
                    Double.toString(score)
            ); // @TODO: add Tide if Tide CV term is added

        }

        // add other cv and user params
        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "<cvParam cvRef=\"PSI-MS\" accession=\"MS:1001117\" name=\"theoretical mass\" value=\"");
        writer.write(DATA_SECTION, Double.toString(peptideAssumption.getTheoreticMass()));
        writer.write(DATA_SECTION, "\" unitCvRef=\"UO\" unitAccession=\"UO:0000221\" unitName=\"dalton\"/>");
        writer.newLine(DATA_SECTION);

        // add validation level information
        decreaseIndent(DATA_SECTION);

        writer.write(DATA_SECTION, getCurrentTabSpace(DATA_SECTION));
        writer.write(DATA_SECTION, "</SpectrumIdentificationItem>");
        writer.newLine(DATA_SECTION);

    }

    /**
     * Returns the name of the modification corresponding to he given
     * modification mass at the given site on the given peptide. Note:
     * modification masses are matched by exact mass as reported by the
     * modification localization scorer, no tolerance is used.
     *
     * @param modMass The modifification mass.
     * @param peptide The peptide.
     * @param modSite The modification site.
     *
     * @return The name of the modification.
     */
    public String getModificationName(
            double modMass,
            Peptide peptide,
            int modSite
    ) {

        String hitAtOtherSite = null;

        for (ModificationMatch modificationMatch : peptide.getVariableModifications()) {

            String modName = modificationMatch.getModification();

            Modification modification = modificationProvider.getModification(modName);

            if (modification.getMass() == modMass) {

                if (modificationMatch.getSite() == modSite) {

                    return modName;

                } else {

                    hitAtOtherSite = modName;

                }
            }
        }

        if (hitAtOtherSite != null) {

            return hitAtOtherSite;

        }

        for (String modName : identificationParameters.getSearchParameters().getModificationParameters().getAllNotFixedModifications()) {

            Modification modification = modificationProvider.getModification(modName);

            if (modification.getMass() == modMass) {

                int[] possibleSites = ModificationUtils.getPossibleModificationSites(
                        peptide,
                        modification,
                        sequenceProvider,
                        identificationParameters.getModificationLocalizationParameters().getSequenceMatchingParameters()
                );

                for (int site : possibleSites) {

                    if (site == modSite) {

                        return modName;

                    }
                }
            }
        }

        throw new IllegalArgumentException(
                "No modification found for mass " + modMass + " at site " + modSite + " on peptide " + peptide.getSequence() + "."
        );

    }

    /**
     * Writes the mzIdentML end tag.
     */
    private void writeMzIdentMLEndTag() {

        decreaseIndent(DATA_SECTION);
        writer.write(DATA_SECTION, "</MzIdentML>");

    }

    private void initTabCounterMap() {

        indentCounterMap.put(HEAD_SECTION, 0);
        indentCounterMap.put(PEPTIDE_SECTION, 1);
        indentCounterMap.put(PEPTIDE_EVIDENCE_SECTION, 2);
        indentCounterMap.put(ANALYSIS_SECTION, 1);

    }

    private void increaseIndent(
            String sectionName
    ) {

        indentCounterMap.put(sectionName, indentCounterMap.get(sectionName) + 1);

    }

    private void decreaseIndent(
            String sectionName
    ) {

        indentCounterMap.put(sectionName, indentCounterMap.get(sectionName) - 1);

    }

    /**
     * Convenience method returning the tabs at the beginning of each line
     * depending on the tabCounter for each section.
     *
     * @param sectionName The section name.
     *
     * @return The tabs in the beginning of each line as a string.
     */
    private String getCurrentTabSpace(
            String sectionName
    ) {

        int indentCounter = indentCounterMap.get(sectionName);

        switch (indentCounter) {
            case 0:
                return "";
            case 1:
                return "\t";
            case 2:
                return "\t\t";
            case 3:
                return "\t\t\t";
            case 4:
                return "\t\t\t\t";
            case 5:
                return "\t\t\t\t\t";
            case 6:
                return "\t\t\t\t\t\t";
            case 7:
                return "\t\t\t\t\t\t\t";
            case 8:
                return "\t\t\t\t\t\t\t\t";
            case 9:
                return "\t\t\t\t\t\t\t\t\t";
            case 10:
                return "\t\t\t\t\t\t\t\t\t\t";
            case 11:
                return "\t\t\t\t\t\t\t\t\t\t\t";
            case 12:
                return "\t\t\t\t\t\t\t\t\t\t\t\t";
            default:
                StringBuilder sb = new StringBuilder(indentCounter);
                for (int i = 0; i < indentCounter; i++) {
                    sb.append('\t');
                }
                return sb.toString();

        }
    }

    /**
     * Convenience method writing a CV term.
     *
     * @param sectionName The name of the section to write to.
     * @param cvTerm The CV term to write.
     */
    private void writeCvTerm(
            String sectionName,
            CvTerm cvTerm
    ) {

        writeCvTerm(sectionName, cvTerm, true);

    }

    /**
     * Convenience method writing a CV term.
     *
     * @param sectionName The name of the section to write to.
     * @param cvTerm The CV term to write.
     * @param writeValue If true the CV term value is written if not null.
     */
    private void writeCvTerm(
            String sectionName,
            CvTerm cvTerm,
            boolean writeValue
    ) {

        writer.write(sectionName, getCurrentTabSpace(sectionName));
        writer.write(sectionName, "<cvParam cvRef=\"");
        writer.write(sectionName, StringEscapeUtils.escapeHtml4(cvTerm.getOntology()));
        writer.write(sectionName, "\" accession=\"");
        writer.write(sectionName, cvTerm.getAccession());
        writer.write(sectionName, "\" name=\"");
        writer.write(sectionName, StringEscapeUtils.escapeHtml4(cvTerm.getName()));
        writer.write(sectionName, "\"");

        writeCvTermValue(sectionName, cvTerm, writeValue);

    }

    /**
     * Convenience method writing the value element of a CV term.
     *
     * @param sectionName The name of the section to write to.
     * @param cvTerm The CV term.
     * @param writeValue If true the CV term value is written if not null.
     */
    private void writeCvTermValue(
            String sectionName,
            CvTerm cvTerm,
            boolean writeValue
    ) {

        String value = cvTerm.getValue();

        if (writeValue && value != null) {

            writer.write(sectionName, " value=\"");
            writer.write(sectionName, StringEscapeUtils.escapeHtml4(value));
            writer.write(sectionName, "\"/>");

        } else {

            writer.write(sectionName, "/>");

        }

        writer.newLine(sectionName);

    }

    /**
     * Convenience method writing a user parameter.
     *
     * @param sectionName The name of the section to write to.
     * @param userParamAsString The user parameter as a String.
     */
    private void writeUserParam(
            String sectionName,
            String userParamAsString
    ) {

        writer.write(sectionName, getCurrentTabSpace(sectionName));
        writer.write(sectionName, "<userParam name=\"");
        writer.write(sectionName, StringEscapeUtils.escapeHtml4(userParamAsString));
        writer.write(sectionName, "\"/>");

        writer.newLine(sectionName);

    }

    /**
     * Convenience method writing a user parameter.
     *
     * @param sectionName The name of the section to write to.
     * @param name The name of the user parameter.
     * @param value The value of the user parameter.
     */
    private void writeUserParam(
            String sectionName,
            String name,
            String value
    ) {

        writer.write(sectionName, getCurrentTabSpace(sectionName));
        writer.write(sectionName, "<userParam name=\"");
        writer.write(sectionName, StringEscapeUtils.escapeHtml4(name));
        writer.write(sectionName, "\" value=\"");
        writer.write(sectionName, StringEscapeUtils.escapeHtml4(value));
        writer.write(sectionName, "\" />");
        writer.newLine(sectionName);

    }

    @Override
    public void close() {

        finalizeFile();

        writer.close();

    }

}
