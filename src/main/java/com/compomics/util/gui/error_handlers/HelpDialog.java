package com.compomics.util.gui.error_handlers;

import com.compomics.util.examples.BareBonesBrowserLaunch;
import java.awt.Frame;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;

/**
 * A dialog used to display help text in HTML format.
 *
 * @author Harald Barsnes
 */
public class HelpDialog extends javax.swing.JDialog {

    /**
     * The parent of the parent is a Frame.
     */
    private Frame frameParent = null;
    /**
     * The parent of the parent is a JDialog.
     */
    private JDialog dialogParent = null;

    /**
     * Creates a new HelpDialog with a JFrame as a parent.
     *
     * @param parent the parent frame
     * @param fileName the name of the help file
     * @param helpIcon the icon to use for the help pages
     * @param aboutIcon the icon to use for the about pages
     * @param title the title to use for the dialog
     */
    public HelpDialog(Frame parent, URL fileName, Image helpIcon, Image aboutIcon, String title) {
        this(parent, fileName, null, helpIcon, aboutIcon, title, 500, 300);
    }

    /**
     * Creates a new HelpDialog with a JFrame as a parent.
     *
     * @param parent the parent frame
     * @param fileName the name of the help file
     * @param helpIcon the icon to use for the help pages
     * @param aboutIcon the icon to use for the about pages
     * @param title the title to use for the dialog
     * @param windowWidth the window width
     * @param windowHeightReduction the window height reduction
     */
    public HelpDialog(Frame parent, URL fileName, Image helpIcon, Image aboutIcon, String title, int windowWidth, int windowHeightReduction) {
        this(parent, fileName, null, helpIcon, aboutIcon, title, windowWidth, windowHeightReduction);
    }

    /**
     * Creates a new HelpDialog with a JFrame as a parent.
     *
     * @param parent the parent frame
     * @param fileName the name of the help file
     * @param reference a reference in the HTML file to scroll to, can be null
     * @param helpIcon the icon to use for the help pages
     * @param aboutIcon the icon to use for the about pages
     * @param title the title to use for the dialog
     */
    public HelpDialog(Frame parent, URL fileName, String reference, Image helpIcon, Image aboutIcon, String title) {
        this(parent, fileName, reference, helpIcon, aboutIcon, title, 500, 300);
    }

    /**
     * Creates a new HelpDialog with a JFrame as a parent.
     *
     * @param parent the parent frame
     * @param fileName the name of the help file
     * @param reference a reference in the HTML file to scroll to, can be null
     * @param helpIcon the icon to use for the help pages
     * @param aboutIcon the icon to use for the about pages
     * @param title the title to use for the dialog
     * @param windowWidth the window width
     * @param windowHeightReduction the window height reduction
     */
    public HelpDialog(Frame parent, URL fileName, String reference, Image helpIcon, Image aboutIcon, String title, int windowWidth, int windowHeightReduction) {
        super(parent, true);
        frameParent = parent;

        initComponents();

        try {
            InputStream stream = fileName.openStream();
            InputStreamReader streamReader = new InputStreamReader(stream);
            BufferedReader b = new BufferedReader(streamReader);
            String s = b.readLine();
            String helpText = "";

            while (s != null) {
                helpText += s;
                s = b.readLine();
            }

            b.close();
            streamReader.close();
            stream.close();

            String imageTag = "<img src=\"";
            int index = 0;

            // replace any relative image tags by their static alternatives
            while (helpText.indexOf(imageTag, index) != -1) {
                int startIndex = helpText.indexOf(imageTag, index) + imageTag.length();
                String figureName = helpText.substring(startIndex, helpText.indexOf(".", startIndex));
                String figureType = helpText.substring(helpText.indexOf(".", startIndex) + 1, helpText.indexOf("\"", startIndex));
                helpText = helpText.replaceAll(imageTag + figureName + "." + figureType, imageTag + getClass().getResource("/helpFiles/" + figureName + "." + figureType));
                index = helpText.indexOf(imageTag, index) + 1;
            }

            textJEditorPane.setText(helpText);

            if (fileName.getPath().substring(fileName.getPath().lastIndexOf(File.separator) + File.separator.length()).startsWith("About")) {
                setTitle("About");
                setIconImage(aboutIcon);
            } else {
                setTitle(title);
                setIconImage(helpIcon);
            }
        } catch (Exception e) {

            e.printStackTrace();

            try {
                textJEditorPane.setPage(getClass().getResource("/helpfiles/DefaultHelpFile.html"));
            } catch (Exception ex) {
                textJEditorPane.setText("The selected help file is not yet available.");
            }
        }

        textJEditorPane.setCaretPosition(0);

        if (reference != null) {
            final String marker = reference;

            // invoke later to give time for components to update
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    textJEditorPane.scrollToReference(marker);
                }
            });
        }

        setSize(windowWidth, Math.max(parent.getHeight() - windowHeightReduction, 500));
        parent.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Creates a new HelpDialog object with a JDialog as a parent.
     *
     * @param parent the parent dialog
     * @param fileName the name of the help file
     * @param helpIcon the icon to use for the help pages
     * @param aboutIcon the icon to use for the about pages
     * @param title the title to use for the dialog
     */
    public HelpDialog(JDialog parent, URL fileName, Image helpIcon, Image aboutIcon, String title) {
        this(parent, fileName, null, helpIcon, aboutIcon, title, 500, 300);
    }

    /**
     * Creates a new HelpDialog object with a JDialog as a parent.
     *
     * @param parent the parent dialog
     * @param fileName the name of the help file
     * @param helpIcon the icon to use for the help pages
     * @param aboutIcon the icon to use for the about pages
     * @param title the title to use for the dialog
     * @param windowWidth the window width
     * @param windowHeightReduction the window height reduction
     */
    public HelpDialog(JDialog parent, URL fileName, Image helpIcon, Image aboutIcon, String title, int windowWidth, int windowHeightReduction) {
        this(parent, fileName, null, helpIcon, aboutIcon, title, windowWidth, windowHeightReduction);
    }

    /**
     * Creates a new HelpDialog with a JDialog as a parent.
     *
     * @param parent the parent dialog
     * @param fileName the name of the help file
     * @param reference a reference in the HTML file to scroll to, can be null
     * @param helpIcon the icon to use for the help pages
     * @param aboutIcon the icon to use for the about pages
     * @param title the title to use for the dialog
     */
    public HelpDialog(JDialog parent, URL fileName, String reference, Image helpIcon, Image aboutIcon, String title) {
        this(parent, fileName, reference, helpIcon, aboutIcon, title, 500, 300);
    }

    /**
     * Creates a new HelpDialog with a JDialog as a parent.
     *
     * @param parent the parent dialog
     * @param fileName the name of the help file
     * @param reference a reference in the HTML file to scroll to, can be null
     * @param helpIcon the icon to use for the help pages
     * @param aboutIcon the icon to use for the about pages
     * @param title the title to use for the dialog
     * @param windowWidth the window width
     * @param windowHeightReduction the window height reduction
     */
    public HelpDialog(JDialog parent, URL fileName, String reference, Image helpIcon, Image aboutIcon, String title, int windowWidth, int windowHeightReduction) {
        super(parent, true);

        dialogParent = parent;

        initComponents();

        try {
            InputStream stream = fileName.openStream();
            InputStreamReader streamReader = new InputStreamReader(stream);
            BufferedReader b = new BufferedReader(streamReader);
            String s = b.readLine();
            String helpText = "";

            while (s != null) {
                helpText += s;
                s = b.readLine();
            }

            b.close();
            streamReader.close();
            stream.close();

            String imageTag = "<img src=\"";
            int index = 0;

            // replace any relative image tags by their static alternatives
            while (helpText.indexOf(imageTag, index) != -1) {
                int startIndex = helpText.indexOf(imageTag, index) + imageTag.length();
                String figureName = helpText.substring(startIndex, helpText.indexOf(".", startIndex));
                String figureType = helpText.substring(helpText.indexOf(".", startIndex) + 1, helpText.indexOf("\"", startIndex));
                helpText = helpText.replaceAll(imageTag + figureName + "." + figureType, imageTag + getClass().getResource("/helpFiles/" + figureName + "." + figureType));
                index = helpText.indexOf(imageTag, index) + 1;
            }

            textJEditorPane.setText(helpText);

            if (fileName.getPath().substring(fileName.getPath().lastIndexOf(File.separator) + File.separator.length()).startsWith("About")) {
                setTitle("About");
                setIconImage(aboutIcon);
            } else {
                setTitle(title);
                setIconImage(helpIcon);
            }
        } catch (Exception e) {

            try {
                textJEditorPane.setPage(getClass().getResource("/helpfiles/DefaultHelpFile.html"));
            } catch (Exception ex) {
                textJEditorPane.setText("The selected help file is not yet available.");
            }
        }

        textJEditorPane.setCaretPosition(0);

        if (reference != null) {
            final String marker = reference;

            // invoke later to give time for components to update
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    textJEditorPane.scrollToReference(marker);
                }
            });
        }

        setSize(windowWidth, Math.max(parent.getParent().getHeight() - windowHeightReduction, 500));
        parent.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
        setLocationRelativeTo(parent);
        setVisible(true);
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
        closeJButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        textJEditorPane = new javax.swing.JEditorPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Help");

        backgroundPanel.setBackground(new java.awt.Color(230, 230, 230));

        closeJButton.setText("Close");
        closeJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeJButtonActionPerformed(evt);
            }
        });

        textJEditorPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        textJEditorPane.setContentType("text/html"); // NOI18N
        textJEditorPane.setEditable(false);
        textJEditorPane.setMinimumSize(new java.awt.Dimension(10, 10));
        textJEditorPane.setPreferredSize(new java.awt.Dimension(10, 10));
        textJEditorPane.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
            public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
                textJEditorPaneHyperlinkUpdate(evt);
            }
        });
        jScrollPane1.setViewportView(textJEditorPane);

        javax.swing.GroupLayout backgroundPanelLayout = new javax.swing.GroupLayout(backgroundPanel);
        backgroundPanel.setLayout(backgroundPanelLayout);
        backgroundPanelLayout.setHorizontalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(closeJButton, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 380, Short.MAX_VALUE))
                .addContainerGap())
        );
        backgroundPanelLayout.setVerticalGroup(
            backgroundPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, backgroundPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(closeJButton)
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
     * Closes the dialog
     *
     * @param evt
     */
    private void closeJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeJButtonActionPerformed
        if (frameParent != null) {
            frameParent.setModalExclusionType(ModalExclusionType.NO_EXCLUDE);
        } else {
            dialogParent.setModalExclusionType(ModalExclusionType.NO_EXCLUDE);
        }
        this.setVisible(false);
        this.dispose();
    }//GEN-LAST:event_closeJButtonActionPerformed

    /**
     * Makes the links active.
     *
     * @param evt
     */
    private void textJEditorPaneHyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {//GEN-FIRST:event_textJEditorPaneHyperlinkUpdate
        if (evt.getEventType().toString().equalsIgnoreCase(
                javax.swing.event.HyperlinkEvent.EventType.ENTERED.toString())) {
            setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        } else if (evt.getEventType().toString().equalsIgnoreCase(
                javax.swing.event.HyperlinkEvent.EventType.EXITED.toString())) {
            setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        } else if (evt.getEventType().toString().equalsIgnoreCase(
                javax.swing.event.HyperlinkEvent.EventType.ACTIVATED.toString())) {
            if (evt.getDescription().startsWith("#")) {
                textJEditorPane.scrollToReference(evt.getDescription());
            } else {
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
                BareBonesBrowserLaunch.openURL(evt.getDescription());
                this.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
            }
        }
    }//GEN-LAST:event_textJEditorPaneHyperlinkUpdate
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel backgroundPanel;
    private javax.swing.JButton closeJButton;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JEditorPane textJEditorPane;
    // End of variables declaration//GEN-END:variables
}
