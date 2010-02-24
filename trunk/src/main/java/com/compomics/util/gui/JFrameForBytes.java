/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/*
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 17-feb-03
 * Time: 18:05:39
 */
package com.compomics.util.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.zip.*;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class implements a JFrame, made specifically for the
 * display of binary data.
 * This data can be zipped, as a button will allow the unzipping of
 * the data.
 *
 * @author Lennart Martens
 */
public class JFrameForBytes extends JFrame {

    /**
     * This byte[] is the data that will be shown.
     */
    private byte[] iData = null;

    private JTextArea txtData = null;
    private JButton btnUnzip = null;
    private JButton btnSave = null;

    /**
     * This constructor creates a JFrame for display of the
     * binary data (specified as well).
     *
     * @param   aTitle  String with the title for the Dialog.
     * @param   aData   byte[] with the data to visualize.
     */
    public JFrameForBytes(String aTitle, byte[] aData) {
        super(aTitle);
        this.iData = aData;
        if(iData == null) {
            iData = "<null>".getBytes();
        }
        this.constructScreen();
        this.pack();
    }

    /**
     * This method sets up and lays out the graphical components on the screen.
     */
    private void constructScreen() {
        txtData = new JTextArea(10, 40);
        txtData.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(txtData);
        try {
            txtData.setText(new String(iData, "UTF-8"));
            if(iData.length > 0) {
                txtData.setCaretPosition(1);
            }
        } catch(UnsupportedEncodingException usee) {
            txtData.setText("Data could not be converted to ASCII text...");
        }

        btnUnzip = new JButton("Unzip data");
        btnUnzip.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                unzipPressed();
            }
        });

        btnSave = new JButton("Save to file...");
        btnSave.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                savePressed();
            }
        });

        JPanel jpanButton = new JPanel();
        jpanButton.setLayout(new BoxLayout(jpanButton, BoxLayout.X_AXIS));
        jpanButton.add(Box.createHorizontalGlue());
        jpanButton.add(btnSave);
        jpanButton.add(Box.createRigidArea(new Dimension(5, btnUnzip.getHeight())));
        jpanButton.add(btnUnzip);
        jpanButton.add(Box.createRigidArea(new Dimension(5, btnUnzip.getHeight())));

        JPanel jpanMain = new JPanel(new BorderLayout());
        jpanMain.add(scroll, BorderLayout.CENTER);
        jpanMain.add(jpanButton, BorderLayout.SOUTH);

        this.getContentPane().add(jpanMain, BorderLayout.CENTER);
        this.addWindowListener(new WindowAdapter() {
            /**
             * Invoked when a window is in the process of being closed.
             * The close operation can be overridden at this point.
             */
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                e.getWindow().setVisible(false);
                e.getWindow().dispose();
            }
        });
    }

    /**
     * This method is called when the user presses the 'unzip'
     * button.
     */
    private void unzipPressed() {
        try {
            // Raw input stream.
            ByteArrayInputStream bais = new ByteArrayInputStream(iData);

            // Raw output stream.
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(baos);

            try {
                ZipInputStream zis = new ZipInputStream(bais);
                ZipEntry ze = null;
                boolean atLeastOnce = false;
                while((ze = zis.getNextEntry()) != null) {
                    atLeastOnce = true;
                    String name = ze.getName();
                    String spacer = "";
                    int length = name.length();
                    for(int i=0;i<length;i++) {
                        spacer += "-";
                    }
                    bos.write((spacer + "\n" + name + "\n" + spacer + "\n").getBytes("ASCII"));
                    int lData = -1;
                    while((lData = zis.read()) != -1) {
                        bos.write(lData);
                    }
                    bos.write("\n".getBytes("ASCII"));
                }
                zis.close();
                if(!atLeastOnce) {
                    throw new ZipException("Not a zip stream.");
                }
            } catch(ZipException ze) {
                // Could be a Gzip. Let's check.
                bais = new ByteArrayInputStream(iData);
                GZIPInputStream gi = new GZIPInputStream(bais);
                int lData = -1;
                while((lData = gi.read()) != -1) {
                    bos.write(lData);
                }
                gi.close();
            }
            bais.close();
            bos.flush();
            baos.flush();
            iData = baos.toByteArray();
            txtData.setText(new String(iData));
            if(txtData.getText().length() > 0) {
                txtData.setCaretPosition(1);
            }
            bos.close();
            baos.close();
        } catch(Exception e) {
            JOptionPane.showMessageDialog(this, new String[]{"Unable to unzip data!", "Is this data zipped?"}, "Error unzipping data", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * This method saves the (unzipped) data to disk.
     */
    private void savePressed() {
        String text = txtData.getText();
        try {
            FileDialog fd = new FileDialog(this, "Save contents of frame to disk...", FileDialog.SAVE);
            fd.setVisible(true);
            String select = fd.getFile();
            if(select == null) {
                return;
            } else {
                select = fd.getDirectory() + select;
                File output = new File(select);
                if(!output.exists()) {
                    output.createNewFile();
                }
                BufferedReader br = new BufferedReader(new StringReader(text));
                BufferedWriter bw = new BufferedWriter(new FileWriter(output));
                String line = null;
                while((line = br.readLine()) != null) {
                    bw.write(line + "\n");
                }
                bw.flush();
                bw.close();
                br.close();
                JOptionPane.showMessageDialog(this, "Output written to " + select + ".", "Output written!", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch(IOException ioe) {
            JOptionPane.showMessageDialog(this, "Unable to save data to file: " + ioe.getMessage(), "Unable to write data to file!", JOptionPane.ERROR_MESSAGE);
        }
    }
}
