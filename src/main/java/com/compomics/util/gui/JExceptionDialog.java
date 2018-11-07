/*
 * Copyright (C) Lennart Martens
 * 
 * Contact: lennart.martens AT UGent.be (' AT ' to be replaced with '@')
 */

/**
 * Created by IntelliJ IDEA.
 * User: Lennart
 * Date: 18-jun-2003
 * Time: 7:25:15
 */
package com.compomics.util.gui;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.StringWriter;
import java.io.PrintWriter;

/*
 * CVS information:
 *
 * $Revision: 1.3 $
 * $Date: 2007/07/06 09:41:53 $
 */

/**
 * This class implements a JDialog for the specific purpose of showing unrecoverable
 * errors or exceptions.
 *
 * @author Lennart Martens
 */
public class JExceptionDialog extends JDialog {

    /**
     * Empty default constructor
     */
    public JExceptionDialog() {
    }

    // Class specific log4j logger for JExceptionDialog instances.
    Logger logger = Logger.getLogger(JExceptionDialog.class);

    private String[] iMessages = null;
    private Throwable iThrowable = null;

    /**
     * This constructor takes all required parameters for the construction, initialization
     * and execution of a JExceptionDialog. Calling this constructor results ultimately in a
     * 'System.exit(1);' call.
     *
     * @param aOwner    Frame with the owner for this JExceptionDialog.
     * @param aTitle    String with the title for the JExceptionDialog.
     * @param aMessages String[] with the messages to be displayed. One line will be given to each String element.
     * @param aThrowable    Throwable that represents the unrecoverable error or exception.
     */
    public JExceptionDialog(Frame aOwner, String aTitle, String[] aMessages, Throwable aThrowable) {
        super(aOwner, aTitle, true);
        iMessages = aMessages;
        iThrowable = aThrowable;
        this.constructGUI();
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((int)(d.getWidth()/2.5), (int)(d.getHeight()/2.5));
        this.pack();
        this.setVisible(true);
        System.exit(1);
    }

    /**
     * This method will build the GUI for the JexcptionDialog.
     */
    private void constructGUI() {
        JPanel jpanTop = new JPanel();
        jpanTop.setLayout(new BoxLayout(jpanTop, BoxLayout.X_AXIS));

        JPanel jpanTopLeft = new JPanel();
        jpanTopLeft.setLayout(new BoxLayout(jpanTopLeft, BoxLayout.Y_AXIS));
        jpanTopLeft.add(new JLabel(new ImageIcon(this.getClass().getClassLoader().getResource("yinyang.gif"))));
        jpanTopLeft.add(Box.createVerticalStrut(15));
        jpanTopLeft.setMaximumSize(jpanTopLeft.getPreferredSize());

        JPanel jpanTopRight = new JPanel();
        jpanTopRight.setLayout(new BoxLayout(jpanTopRight, BoxLayout.Y_AXIS));
        jpanTopRight.add(Box.createVerticalGlue());
        for (int i = 0; i < iMessages.length; i++) {
            String lMessage = iMessages[i];
            jpanTopRight.add(new JLabel(lMessage));
        }
        Component[] comps = jpanTopRight.getComponents();
        Font tempFont = new Font(jpanTop.getFont().getName(), Font.PLAIN, jpanTop.getFont().getSize());
        for (int i = 0; i < comps.length; i++) {
            Component comp = comps[i];
            comp.setFont(tempFont);
        }
        jpanTop.add(jpanTopLeft);
        jpanTop.add(Box.createHorizontalStrut(15));
        jpanTop.add(jpanTopRight);
        jpanTop.add(Box.createHorizontalGlue());

        JPanel jpanCenter = new JPanel();
        jpanCenter.setLayout(new BoxLayout(jpanCenter, BoxLayout.Y_AXIS));
        jpanCenter.setBorder(BorderFactory.createEtchedBorder());
        tempFont = new Font(jpanCenter.getFont().getName(), Font.ITALIC, jpanCenter.getFont().getSize());
        JPanel temp1 = new JPanel();
        JLabel lbl1 = new JLabel("Serious error.");
        lbl1.setFont(tempFont);
        temp1.add(lbl1);
        temp1.setLayout(new BoxLayout(temp1, BoxLayout.X_AXIS));
        JPanel temp2 = new JPanel();
        JLabel lbl2 = new JLabel("       All shortcuts have disappeared       ");
        lbl2.setFont(tempFont);
        temp2.add(lbl2);
        temp2.setLayout(new BoxLayout(temp2, BoxLayout.X_AXIS));
        JPanel temp3 = new JPanel();
        temp3.setLayout(new BoxLayout(temp3, BoxLayout.X_AXIS));
        JLabel lbl3 = new JLabel("Screen. Mind. Both are blank.");
        lbl3.setFont(tempFont);
        temp3.add(lbl3);
        jpanCenter.add(Box.createVerticalGlue());
        jpanCenter.add(temp1);
        jpanCenter.add(new JLabel("\n"));
        jpanCenter.add(temp2);
        jpanCenter.add(new JLabel("\n"));
        jpanCenter.add(temp3);
        jpanCenter.add(new JLabel("\n"));
        jpanCenter.add(Box.createVerticalGlue());
        JPanel jpanCenterHolder = new JPanel();
        jpanCenterHolder.setLayout(new BoxLayout(jpanCenterHolder, BoxLayout.X_AXIS));
        jpanCenterHolder.add(Box.createHorizontalGlue());
        jpanCenterHolder.add(jpanCenter);
        jpanCenterHolder.add(Box.createHorizontalGlue());

        JPanel jpanBottom = this.getButtonPanel();

        this.getContentPane().add(jpanTop, BorderLayout.NORTH);
        this.getContentPane().add(jpanCenterHolder, BorderLayout.CENTER);
        this.getContentPane().add(jpanBottom, BorderLayout.SOUTH);
    }

    /**
     * This method generates the buttonpanel for the JExceptionDialog.
     *
     * @return  JPanel  with the buttons.
     */
    private JPanel getButtonPanel() {
        // The JPanel that holds the real buttonpanel, and the stacktrace if it is to be shown.
        final JPanel jpanOuterButton = new JPanel();
        jpanOuterButton.setLayout(new BoxLayout(jpanOuterButton, BoxLayout.Y_AXIS));

        // The actual button panel.
        JPanel jpanButtons = new JPanel();
        jpanButtons.setLayout(new BoxLayout(jpanButtons, BoxLayout.X_AXIS));
        jpanButtons.add(Box.createHorizontalGlue());

        JButton btnStackTrace = new JButton("Show stack trace");
        btnStackTrace.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                iThrowable.printStackTrace(pw);
                pw.flush();
                sw.flush();
                JTextArea txtTrace = new JTextArea(sw.toString());
                jpanOuterButton.add(Box.createRigidArea(new Dimension(txtTrace.getWidth(), 5)));
                jpanOuterButton.add(txtTrace);
                pw.close();
            }
        });
        jpanButtons.add(btnStackTrace);
        jpanOuterButton.add(jpanButtons);

        return jpanOuterButton;
    }

    /**
     * This main method is for testing purposes only.
     *
     * @param args  String[] with the start-up arguments.
     */
    public static void main(String[] args) {
        Exception e = new IllegalArgumentException("Nowhere to run!!");
        JExceptionDialog jed = new JExceptionDialog(new JFrame(), "Test of this dialog.", new String[]{"Error occurred.", "\n", e.getMessage(), "\n"}, e);
    }
}
