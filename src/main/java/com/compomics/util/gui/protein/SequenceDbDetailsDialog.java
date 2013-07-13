package com.compomics.util.gui.protein;

import com.compomics.util.Util;
import com.compomics.util.examples.BareBonesBrowserLaunch;
import com.compomics.util.experiment.biology.Protein;
import com.compomics.util.experiment.identification.FastaIndex;
import com.compomics.util.experiment.identification.SequenceFactory;
import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import com.compomics.util.preferences.UtilitiesUserPreferences;
import com.compomics.util.protein.Header;
import java.awt.Frame;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SpinnerListModel;
import javax.swing.filechooser.FileFilter;

/**
 * This dialog displays information about a sequence database.
 *
 * @author Marc Vaudel
 */
public class SequenceDbDetailsDialog extends javax.swing.JDialog {

    /**
     * A simple progress dialog.
     */
    private static ProgressDialogX progressDialog;
    /**
     * The sequence factory.
     */
    private SequenceFactory sequenceFactory = SequenceFactory.getInstance();
    /**
     * The last selected folder.
     */
    private String lastSelectedFolder = null;
    /**
     * boolean indicating whether the db can be changed.
     */
    private boolean dbEditable = true;
    /**
     * The icon to display when waiting.
     */
    private Image waitingImage;
    /**
     * The normal icon.
     */
    private Image normalImange;
    /**
     * The parent frame.
     */
    private Frame parentFrame;
    /**
     * The utilities user preferences.
     */
    private UtilitiesUserPreferences utilitiesUserPreferences = null;

    /**
     * Creates a new SequenceDbDetailsDialog.
     *
     * @param parent
     * @param lastSelectedFolder
     * @param dbEditable
     * @param waitingImage
     * @param normalImange
     */
    public SequenceDbDetailsDialog(Frame parent, String lastSelectedFolder, boolean dbEditable, Image normalImange, Image waitingImage) {
        super(parent, true);
        initComponents();
        this.parentFrame = parent;
        this.lastSelectedFolder = lastSelectedFolder;
        this.dbEditable = dbEditable;
        this.waitingImage = waitingImage;
        this.normalImange = normalImange;
        loadUserPreferences();
        setUpGUI();
        setLocationRelativeTo(parent);
    }

    /**
     * Set up the GUI.
     */
    private void setUpGUI() {
        FastaIndex fastaIndex = sequenceFactory.getCurrentFastaIndex();
        if (fastaIndex != null) {
            fileTxt.setText(sequenceFactory.getCurrentFastaFile().getAbsolutePath());
            dbNameTxt.setText(fastaIndex.getName());
            typeCmb.setSelectedItem(Header.getDatabaseTypeAsString(fastaIndex.getDatabaseType()));
            versionTxt.setText(fastaIndex.getVersion());
            lastModifiedTxt.setText(new Date(fastaIndex.getLastModified()).toString());
            String nSequences = fastaIndex.getNSequences() + " sequences";
            if (fastaIndex.isConcatenatedTargetDecoy()) {
                nSequences += " (" + fastaIndex.getNTarget() + " target)";
            }
            sizeTxt.setText(nSequences);
            if (fastaIndex.isConcatenatedTargetDecoy()) {
                decoyFlagTxt.setEnabled(true);
                decoyFlagTxt.setText(fastaIndex.getDecoyTag());
            } else {
                decoyFlagTxt.setText("");
                decoyFlagTxt.setEnabled(false);
            }
            decoyButton.setEnabled(!sequenceFactory.concatenatedTargetDecoy() && dbEditable);
            browseButton.setEnabled(dbEditable);
            decoyFlagTxt.setEditable(dbEditable);
            if (!sequenceFactory.getAccessions().isEmpty()) {
                accessionsSpinner.setEnabled(true);
                accessionsSpinner.setModel(new SpinnerListModel(sequenceFactory.getAccessions()));
                accessionsSpinner.setValue(sequenceFactory.getAccessions().get(0));
                updateSequence();
            } else {
                accessionsSpinner.setEnabled(false);
            }
        } else {
            typeCmb.setSelectedItem(Header.getDatabaseTypeAsString(Header.DatabaseType.Unknown));
        }
    }

    /**
     * Updates the displayed sequence.
     */
    private void updateSequence() {
        String accession = accessionsSpinner.getValue().toString();
        try {
            Protein protein = sequenceFactory.getProtein(accession);
            proteinTxt.setText(sequenceFactory.getHeader(accession).toString() + System.getProperty("line.separator") + protein.getSequence());
            proteinTxt.setCaretPosition(0);
            String decoyFlag = decoyFlagTxt.getText().trim();
            if (!decoyFlag.equals("")) {
                if (SequenceFactory.isDecoy(accession, decoyFlag)) {
                    targetDecoyTxt.setText("(Decoy)");
                } else {
                    targetDecoyTxt.setText("(Target)");
                }
            } else {
                targetDecoyTxt.setText("");
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occured while looking for protein " + accession + ".", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Returns the last selected folder.
     *
     * @return the last selected folder
     */
    public String getLastSelectedFolder() {
        return lastSelectedFolder;
    }

    /**
     * Allows the user to select a db and loads its information.
     *
     * @param userCanDispose if true, the dialog is closed if the user cancels
     * the selection
     * @return true if the selection was not canceled by the user or an error
     * occurred
     */
    public boolean selectDB(boolean userCanDispose) {

        if (sequenceFactory.getFileName() == null || !userCanDispose) {

            File startLocation = new File(lastSelectedFolder);
            if (utilitiesUserPreferences.getDbFolder() != null && utilitiesUserPreferences.getDbFolder().exists()) {
                startLocation = utilitiesUserPreferences.getDbFolder();
            }

            JFileChooser fc = new JFileChooser(startLocation);
            FileFilter filter = new FileFilter() {
                @Override
                public boolean accept(File myFile) {
                    return myFile.getName().toLowerCase().endsWith("fasta")
                            || myFile.isDirectory();
                }

                @Override
                public String getDescription() {
                    return "Supported formats: FASTA (.fasta)";
                }
            };

            fc.setFileFilter(filter);
            int result = fc.showOpenDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                lastSelectedFolder = file.getParent();
                utilitiesUserPreferences.setDbFolder(file.getParentFile());


                if (file.getName().indexOf(" ") != -1) {
                    file = renameFastaFileName(file);
                    if (file == null) {
                        return false;
                    }
                }

                try {
                    sequenceFactory.clearFactory();
                    loadFastaFile(file);
                    return true;
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this, "An error occurred while clearing the sequence factory.",
                            "Import error", JOptionPane.WARNING_MESSAGE);
                    e.printStackTrace();
                }
            } else if (userCanDispose) {
                dispose();
            }

            return false;
        } else {
            return true;
        }
    }

    /**
     * Loads the FASTA file in the factory and updates the GUI.
     *
     * @param file the FASTA file
     */
    private void loadFastaFile(File file) {

        final File finalFile = file;

        progressDialog = new ProgressDialogX(this, parentFrame,
                normalImange,
                waitingImage,
                true);
        progressDialog.setPrimaryProgressCounterIndeterminate(true);
        progressDialog.setTitle("Loading Database. Please Wait...");

        new Thread(new Runnable() {
            public void run() {
                try {
                    progressDialog.setVisible(true);
                } catch (IndexOutOfBoundsException e) {
                    // ignore
                }
            }
        }, "ProgressDialog").start();

        new Thread("importThread") {
            public void run() {

                try {
                    progressDialog.setTitle("Importing Database. Please Wait...");
                    progressDialog.setPrimaryProgressCounterIndeterminate(false);
                    sequenceFactory.loadFastaFile(finalFile, progressDialog);
                } catch (IOException e) {
                    progressDialog.setRunFinished();
                    JOptionPane.showMessageDialog(SequenceDbDetailsDialog.this,
                            new String[]{"FASTA Import Error.", "File " + finalFile.getAbsolutePath() + " not found."},
                            "FASTA Import Error", JOptionPane.WARNING_MESSAGE);
                    e.printStackTrace();
                    return;
                } catch (ClassNotFoundException e) {
                    progressDialog.setRunFinished();
                    JOptionPane.showMessageDialog(SequenceDbDetailsDialog.this,
                            new String[]{"FASTA Import Error.", "File index of " + finalFile.getName() + " could not be imported. Please contact the developers."},
                            "FASTA Import Error", JOptionPane.WARNING_MESSAGE);
                    e.printStackTrace();
                    return;
                } catch (StringIndexOutOfBoundsException e) {
                    progressDialog.setRunFinished();
                    JOptionPane.showMessageDialog(SequenceDbDetailsDialog.this,
                            e.getMessage(),
                            "FASTA Import Error", JOptionPane.WARNING_MESSAGE);
                    e.printStackTrace();
                    return;
                } catch (IllegalArgumentException e) {
                    progressDialog.setRunFinished();
                    JOptionPane.showMessageDialog(SequenceDbDetailsDialog.this,
                            e.getMessage(),
                            "FASTA Import Error", JOptionPane.WARNING_MESSAGE);
                    e.printStackTrace();
                    return;
                }

                if (!progressDialog.isRunCanceled() && !sequenceFactory.concatenatedTargetDecoy()) {
                    int value = JOptionPane.showConfirmDialog(SequenceDbDetailsDialog.this,
                            "The selected FASTA file does not seem to contain decoy sequences.\n"
                            + "Add decoys?", "Add Decoy Sequences?", JOptionPane.YES_NO_OPTION);

                    if (value == JOptionPane.NO_OPTION) {
                        decoyFlagTxt.setEnabled(false);
                    } else if (value == JOptionPane.YES_OPTION) {
                        generateTargetDecoyDatabase(finalFile, progressDialog);
                    }
                }
                if (!progressDialog.isRunCanceled()) {
                    setUpGUI();
                }
                progressDialog.setRunFinished();
            }
        }.start();
    }

    /**
     * Appends decoy sequences to the given target database file.
     *
     * @param targetFile the target database file
     * @param progressDialog the progress dialog 
     */
    public void generateTargetDecoyDatabase(File targetFile, ProgressDialogX progressDialog) {

        String fastaInput = targetFile.getAbsolutePath();
        String newFasta = fastaInput.substring(0, fastaInput.lastIndexOf("."));
        newFasta += SequenceFactory.getTargetDecoyFileNameTag();

        try {
            File newFile = new File(newFasta);
            progressDialog.setTitle("Appending Decoy Sequences. Please Wait...");
            sequenceFactory.appendDecoySequences(newFile, progressDialog);
            sequenceFactory.clearFactory();
            progressDialog.setTitle("Getting Database Details. Please Wait...");
            sequenceFactory.loadFastaFile(newFile, progressDialog);
        } catch (OutOfMemoryError error) {
            Runtime.getRuntime().gc();
            JOptionPane.showMessageDialog(SequenceDbDetailsDialog.this,
                    "SearchGUI used up all the available memory and had to be stopped.\n"
                    + "Memory boundaries are set in the Edit menu (Edit > Java Options).",
                    "Out Of Memory Error",
                    JOptionPane.ERROR_MESSAGE);
            System.out.println("Ran out of memory!");
            error.printStackTrace();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(SequenceDbDetailsDialog.this,
                    new String[]{"FASTA Import Error.", "File " + fastaInput + " not found."},
                    "FASTA Import Error", JOptionPane.WARNING_MESSAGE);
            e.printStackTrace();
        }
    }

    /**
     * Copies the content of the FASTA file to a new file and replaces any white
     * space in the file name with '_' instead. Returns the new file, null if an
     * error occurred.
     *
     * @param file the FASTA file to rename
     * @return the renamed FASTA file
     */
    public File renameFastaFileName(File file) {
        String tempName = file.getName();
        tempName = tempName.replaceAll(" ", "_");

        File renamedFile = new File(file.getParentFile().getAbsolutePath() + File.separator + tempName);

        boolean success = false;

        try {
            success = renamedFile.createNewFile();
            if (success) {
                Util.copyFile(file, renamedFile);
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "An error occurred while renaming the file.",
                    "Please Rename File", JOptionPane.WARNING_MESSAGE);
            e.printStackTrace();
            success = false;
        }

        if (success) {
            JOptionPane.showMessageDialog(this, "Your FASTA file name contained white space and has been renamed to:\n"
                    + file.getParentFile().getAbsolutePath() + File.separator + tempName, "Renamed File", JOptionPane.WARNING_MESSAGE);
            return renamedFile;
        }
        return null;
    }

    /**
     * Saves the changes in the index file.
     *
     * @return true if saving was successful
     */
    private boolean saveChanges() {

        boolean change = false;
        FastaIndex fastaIndex = sequenceFactory.getCurrentFastaIndex();

        String name = dbNameTxt.getText().trim();
        if (!name.equals(fastaIndex.getName())) {
            fastaIndex.setName(name);
            change = true;
        }

        String version = versionTxt.getText().trim();
        if (!version.equals(fastaIndex.getVersion())) {
            fastaIndex.setVersion(version);
            change = true;
        }

        String decoyFlag = decoyFlagTxt.getText().trim();
        if (!decoyFlag.equals(fastaIndex.getDecoyTag())) {
            fastaIndex.setDecoyTag(decoyFlag);
            change = true;
        }

        if (change) {
            try {
                sequenceFactory.saveIndex();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "An error occurred while attempting to save the database index file.", "Renamed File", JOptionPane.WARNING_MESSAGE);
                return false;
            }
        }
        return true;

    }

    /**
     * Loads the user preferences.
     */
    public void loadUserPreferences() {
        try {
            utilitiesUserPreferences = UtilitiesUserPreferences.loadUserPreferences();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        backgroundPanel = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        databaseInformationPanel = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        dbNameTxt = new javax.swing.JTextField();
        typeCmb = new javax.swing.JComboBox();
        typeLabel = new javax.swing.JLabel();
        fileTxt = new javax.swing.JTextField();
        decoyFlagTxt = new javax.swing.JTextField();
        decoyTagLabel = new javax.swing.JLabel();
        versionLabel = new javax.swing.JLabel();
        versionTxt = new javax.swing.JTextField();
        lastModifiedLabel = new javax.swing.JLabel();
        lastModifiedTxt = new javax.swing.JTextField();
        sizeLabel = new javax.swing.JLabel();
        sizeTxt = new javax.swing.JTextField();
        decoyButton = new javax.swing.JButton();
        browseButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        previewPanel = new javax.swing.JPanel();
        proteinYxtScrollPane = new javax.swing.JScrollPane();
        proteinTxt = new javax.swing.JTextArea();
        proteinLabel = new javax.swing.JLabel();
        accessionsSpinner = new javax.swing.JSpinner();
        targetDecoyTxt = new javax.swing.JLabel();
        databaseHelpSettingsJLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Database");

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        databaseInformationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Database Details"));
        databaseInformationPanel.setOpaque(false);

        nameLabel.setText("Name");

        dbNameTxt.setEditable(false);

        typeCmb.setModel(new DefaultComboBoxModel(Header.getDatabaseTypesAsString()));
        typeCmb.setPreferredSize(new java.awt.Dimension(200, 22));

        typeLabel.setText("Type");

        fileTxt.setEditable(false);
        fileTxt.setHorizontalAlignment(javax.swing.JTextField.CENTER);

        decoyFlagTxt.setEditable(false);

        decoyTagLabel.setText("Decoy Tag");

        versionLabel.setText("Version");

        versionTxt.setEditable(false);

        lastModifiedLabel.setText("Modified");

        lastModifiedTxt.setEditable(false);

        sizeLabel.setText("Size");

        sizeTxt.setEditable(false);

        decoyButton.setText("Decoy");
        decoyButton.setPreferredSize(new java.awt.Dimension(75, 25));
        decoyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                decoyButtonActionPerformed(evt);
            }
        });

        browseButton.setText("Browse");
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("File");

        javax.swing.GroupLayout databaseInformationPanelLayout = new javax.swing.GroupLayout(databaseInformationPanel);
        databaseInformationPanel.setLayout(databaseInformationPanelLayout);
        databaseInformationPanelLayout.setHorizontalGroup(
            databaseInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(databaseInformationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(databaseInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(databaseInformationPanelLayout.createSequentialGroup()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(fileTxt)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(browseButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(decoyButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(databaseInformationPanelLayout.createSequentialGroup()
                        .addGroup(databaseInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, databaseInformationPanelLayout.createSequentialGroup()
                                .addComponent(versionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(versionTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, databaseInformationPanelLayout.createSequentialGroup()
                                .addComponent(decoyTagLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(decoyFlagTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, databaseInformationPanelLayout.createSequentialGroup()
                                .addComponent(nameLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dbNameTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)))
                        .addGap(35, 35, 35)
                        .addGroup(databaseInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(sizeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(typeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(lastModifiedLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(databaseInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(typeCmb, 0, 280, Short.MAX_VALUE)
                            .addComponent(lastModifiedTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE)
                            .addComponent(sizeTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 280, Short.MAX_VALUE))))
                .addContainerGap())
        );

        databaseInformationPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {decoyTagLabel, jLabel1, lastModifiedLabel, nameLabel, sizeLabel, typeLabel, versionLabel});

        databaseInformationPanelLayout.setVerticalGroup(
            databaseInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(databaseInformationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(databaseInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fileTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(decoyButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(databaseInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(dbNameTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(typeLabel)
                    .addComponent(typeCmb, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(databaseInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(versionTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(versionLabel)
                    .addComponent(lastModifiedLabel)
                    .addComponent(lastModifiedTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(databaseInformationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(decoyTagLabel)
                    .addComponent(decoyFlagTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sizeLabel)
                    .addComponent(sizeTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        previewPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Preview"));
        previewPanel.setOpaque(false);

        proteinTxt.setEditable(false);
        proteinTxt.setColumns(20);
        proteinTxt.setLineWrap(true);
        proteinTxt.setRows(5);
        proteinTxt.setWrapStyleWord(true);
        proteinYxtScrollPane.setViewportView(proteinTxt);

        proteinLabel.setText("Protein");

        accessionsSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                accessionsSpinnerStateChanged(evt);
            }
        });

        targetDecoyTxt.setText("(target/decoy)");

        javax.swing.GroupLayout previewPanelLayout = new javax.swing.GroupLayout(previewPanel);
        previewPanel.setLayout(previewPanelLayout);
        previewPanelLayout.setHorizontalGroup(
            previewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(previewPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(previewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(proteinYxtScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 753, Short.MAX_VALUE)
                    .addGroup(previewPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(proteinLabel)
                        .addGap(18, 18, 18)
                        .addComponent(accessionsSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(targetDecoyTxt)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        previewPanelLayout.setVerticalGroup(
            previewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(previewPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(proteinYxtScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 125, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(previewPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(proteinLabel)
                    .addComponent(accessionsSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(targetDecoyTxt))
                .addContainerGap())
        );

        databaseHelpSettingsJLabel.setForeground(new java.awt.Color(0, 0, 255));
        databaseHelpSettingsJLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        databaseHelpSettingsJLabel.setText("<html><u><i>Database?</i></u></html>");
        databaseHelpSettingsJLabel.setToolTipText("Open Database Help");
        databaseHelpSettingsJLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                databaseHelpSettingsJLabelMouseClicked(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                databaseHelpSettingsJLabelMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                databaseHelpSettingsJLabelMouseExited(evt);
            }
        });

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(backgroundPanelLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(databaseHelpSettingsJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton, javax.swing.GroupLayout.PREFERRED_SIZE, 75, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(databaseInformationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(previewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        backgroundPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {cancelButton, okButton});

        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(databaseInformationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(previewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cancelButton)
                    .addComponent(okButton)
                    .addComponent(databaseHelpSettingsJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Saves changes and closes the dialog
     *
     * @param evt
     */
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        if (saveChanges()) {
            if (utilitiesUserPreferences != null) {
                UtilitiesUserPreferences.saveUserPreferences(utilitiesUserPreferences);
            }
            dispose();
        }
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Close the dialog.
     *
     * @param evt
     */
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * Open a file chooser to select a FASTA file.
     *
     * @param evt
     */
    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed
        selectDB(false);
    }//GEN-LAST:event_browseButtonActionPerformed

    /**
     * Add decoys.
     *
     * @param evt
     */
    private void decoyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_decoyButtonActionPerformed

        progressDialog = new ProgressDialogX(this, parentFrame,
                normalImange,
                waitingImage,
                true);
        progressDialog.setPrimaryProgressCounterIndeterminate(true);
        progressDialog.setTitle("Creating Decoy. Please Wait...");

        new Thread(new Runnable() {
            public void run() {
                try {
                    progressDialog.setVisible(true);
                } catch (IndexOutOfBoundsException e) {
                    // ignore
                }
            }
        }, "ProgressDialog").start();

        new Thread("DecoyThread") {
            public void run() {
                generateTargetDecoyDatabase(sequenceFactory.getCurrentFastaFile(), progressDialog);
                progressDialog.setRunFinished();
            }
        }.start();

    }//GEN-LAST:event_decoyButtonActionPerformed

    /**
     * Update the sequence.
     *
     * @param evt
     */
    private void accessionsSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_accessionsSpinnerStateChanged
        updateSequence();
    }//GEN-LAST:event_accessionsSpinnerStateChanged

    /**
     * Open the database help page.
     *
     * @param evt
     */
    private void databaseHelpSettingsJLabelMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_databaseHelpSettingsJLabelMouseClicked
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        BareBonesBrowserLaunch.openURL("http://code.google.com/p/searchgui/wiki/DatabaseHelp");
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_databaseHelpSettingsJLabelMouseClicked

    /**
     * Change the cursor to a hand cursor.
     *
     * @param evt
     */
    private void databaseHelpSettingsJLabelMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_databaseHelpSettingsJLabelMouseEntered
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
    }//GEN-LAST:event_databaseHelpSettingsJLabelMouseEntered

    /**
     * Change cursor back to the default cursor.
     *
     * @param evt
     */
    private void databaseHelpSettingsJLabelMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_databaseHelpSettingsJLabelMouseExited
        this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
    }//GEN-LAST:event_databaseHelpSettingsJLabelMouseExited
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSpinner accessionsSpinner;
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton browseButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel databaseHelpSettingsJLabel;
    private javax.swing.JPanel databaseInformationPanel;
    private javax.swing.JTextField dbNameTxt;
    private javax.swing.JButton decoyButton;
    private javax.swing.JTextField decoyFlagTxt;
    private javax.swing.JLabel decoyTagLabel;
    private javax.swing.JTextField fileTxt;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel lastModifiedLabel;
    private javax.swing.JTextField lastModifiedTxt;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JButton okButton;
    private javax.swing.JPanel previewPanel;
    private javax.swing.JLabel proteinLabel;
    private javax.swing.JTextArea proteinTxt;
    private javax.swing.JScrollPane proteinYxtScrollPane;
    private javax.swing.JLabel sizeLabel;
    private javax.swing.JTextField sizeTxt;
    private javax.swing.JLabel targetDecoyTxt;
    private javax.swing.JComboBox typeCmb;
    private javax.swing.JLabel typeLabel;
    private javax.swing.JLabel versionLabel;
    private javax.swing.JTextField versionTxt;
    // End of variables declaration//GEN-END:variables
}
