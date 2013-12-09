package com.compomics.util.examples;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

/**
 * A window used to display help text in HTML format.
 * 
 * @author Harald Barsnes
 * 
 * Created November 2005
 */
public class HelpWindow extends javax.swing.JFrame {

    /**
     * Creates a new HelpWindow object with a Frame as a parent.
     *
     * @param parent
     * @param fileName the name of the help file
     */
    public HelpWindow(javax.swing.JFrame parent, URL fileName) {

        // only works for Java 1.6 and newer
        //this.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);
        initComponents();

        // sets the icon of the frame
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
                getResource("/icons/help.GIF")));

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

            textJEditorPane.setText(helpText);

        } catch (Exception e) {
            textJEditorPane.setText("The selected help file is not yet available.");
        }

        textJEditorPane.setCaretPosition(0);

        setSize(450, 500);
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /**
     * Creates a new HelpWindow-object with a JDialog as a parent.
     *
     * @param parent
     * @param fileName the name of the help file
     */
    public HelpWindow(javax.swing.JDialog parent, URL fileName) {

        // only works for Java 1.6 and newer
        //this.setModalExclusionType(ModalExclusionType.APPLICATION_EXCLUDE);

        initComponents();

        // sets the icon of the frame
        setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().
                getResource("/icons/help.GIF")));

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

            textJEditorPane.setText(helpText);

        } catch (Exception e) {
            textJEditorPane.setText("The selected help file is not yet available.");
        }

        textJEditorPane.setCaretPosition(0);

        setSize(450, 500);
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        closeJButton = new javax.swing.JButton();
        textEditorScrollPane = new javax.swing.JScrollPane();
        textJEditorPane = new javax.swing.JEditorPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Compomics-Utilites - Help");
        setAlwaysOnTop(true);

        closeJButton.setText("Close");
        closeJButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeJButtonActionPerformed(evt);
            }
        });

        textJEditorPane.setEditable(false);
        textJEditorPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        textJEditorPane.setContentType("text/html"); // NOI18N
        textJEditorPane.setMinimumSize(new java.awt.Dimension(10, 10));
        textJEditorPane.setPreferredSize(new java.awt.Dimension(10, 10));
        textJEditorPane.addHyperlinkListener(new javax.swing.event.HyperlinkListener() {
            public void hyperlinkUpdate(javax.swing.event.HyperlinkEvent evt) {
                textJEditorPaneHyperlinkUpdate(evt);
            }
        });
        textEditorScrollPane.setViewportView(textJEditorPane);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(textEditorScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
                    .add(closeJButton))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .add(textEditorScrollPane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(closeJButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Closes the dialog 
     *
     * @param evt
     */
    private void closeJButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeJButtonActionPerformed
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
    private javax.swing.JButton closeJButton;
    private javax.swing.JScrollPane textEditorScrollPane;
    private javax.swing.JEditorPane textJEditorPane;
    // End of variables declaration//GEN-END:variables
}
