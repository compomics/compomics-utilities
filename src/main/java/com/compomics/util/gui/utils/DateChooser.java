/**
 * Created by IntelliJ IDEA.
 * User: martlenn
 * Date: 28-Jul-2009
 * Time: 16:23:59
 */
package com.compomics.util.gui.utils;
import org.apache.log4j.Logger;
/*
 * CVS information:
 *
 * $Revision: 1.1 $
 * $Date: 2009/07/28 15:25:52 $
 */
import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.*;
import javax.swing.border.BevelBorder;

/**
 * This class provides a simple date chooser.
 *
 * @author Lennart Martens
 * @version $Id: DateChooser.java,v 1.1 2009/07/28 15:25:52 lennart Exp $
 */
public class DateChooser extends JDialog {

    // Class specific log4j logger for DateChooser instances.
    static Logger logger = Logger.getLogger(DateChooser.class);

    private Calendar iSelectedCalendar = null;

    private Calendar iShownCalendar = null;

    private int iSelectedDayIndex = -1;

    private static final String iCoreTitle = "Date chooser";

    private JLabel[] lblDayNames = new JLabel[] {new JLabel("Sun"), new JLabel("Mon"), new JLabel("Tue"), new JLabel("Wed"), new JLabel("Thur"), new JLabel("Fri"), new JLabel("Sat")};
    private JLabel[] lblDays = new JLabel[42];
    private Integer[] iDays = new Integer[42];
    private JLabel lblCurrentMonthYear = new JLabel("");

    private final static SimpleDateFormat iSDFCurrentMonthyear = new SimpleDateFormat("MMMM yyyy");

    /**
     * Creates a new DataChooser object.
     *
     * @param aParent the JFrame parent
     */
    public DateChooser(JFrame aParent) {
        this(aParent, null);
    }

    /**
     * This constructor takes the parent JFRame for this dialog,
     * as well as a reference parameter for the selected date.
     *
     * @param aParent
     * @param aSelectedCalendar
     */
    public DateChooser(JFrame aParent, Calendar aSelectedCalendar) {
        super(aParent, iCoreTitle);
        iSelectedCalendar = aSelectedCalendar;
        if(iSelectedCalendar == null) {
            iShownCalendar = Calendar.getInstance();
        } else {
            iShownCalendar = (Calendar)iSelectedCalendar.clone();
        }
        this.setModal(true);
        createScreen();
        initDates();
    }

    /**
     * This method returns a Calendar set at the selected date,
     * or 'null' if no date was selected.
     *
     * @return Calendar set at the selected date.
     */
    public Calendar getSelectedDate() {
        Calendar result = null;
        if(iSelectedCalendar != null) {
            result = iSelectedCalendar;
        }
        return result;
    }

    /**
     * Sets up the basic GUI components.
     */
    private void createScreen() {
        JPanel jpanDate = new JPanel(new GridLayout(7, 7));
        // The day names.
        for (int i = 0; i < lblDayNames.length; i++) {
            lblDayNames[i].setHorizontalAlignment(JLabel.RIGHT);
            lblDayNames[i].setFont(lblDayNames[i].getFont().deriveFont(Font.BOLD));
            setColour(i, lblDayNames[i]);
            jpanDate.add(lblDayNames[i]);
        }
        jpanDate.setMinimumSize(new Dimension(jpanDate.getPreferredSize().width, jpanDate.getPreferredSize().height));
        // The day numbers.
        int dayCounter = 0;
        for (int i=0;i< lblDays.length;i++) {
            final int counter = i;
            lblDays[i] = new JLabel("", JLabel.RIGHT);
            lblDays[i].addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    dateClicked(counter);
                }
                public void mouseEntered(MouseEvent me) {
                    mouseFocus(counter, true);
                }
                public void mouseExited(MouseEvent me) {
                    mouseFocus(counter, false);
                }
            });
            setColour(dayCounter, lblDays[i]);
            jpanDate.add(lblDays[i]);
            // Increment daycounter, but don't let it go above 6.
            dayCounter++;
            if(dayCounter > 6) {
                dayCounter = 0;
            }
        }

        JPanel jpanDateHolder = new JPanel();
        jpanDateHolder.setLayout(new BoxLayout(jpanDateHolder, BoxLayout.X_AXIS));
        jpanDateHolder.add(jpanDate);
        jpanDateHolder.add(Box.createHorizontalStrut(20));

        JPanel jpanScrollDate = new JPanel();
        jpanScrollDate.setLayout(new BoxLayout(jpanScrollDate, BoxLayout.X_AXIS));

        JLabel lblPrevious = new JLabel("<< Previous");
        lblPrevious.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                iShownCalendar.add(Calendar.MONTH, -1);
                initDates();
            }
        });
        JLabel lblNext = new JLabel("Next >>");
        lblNext.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent me) {
                iShownCalendar.add(Calendar.MONTH, 1);
                initDates();
            }
        });
        jpanScrollDate.add(lblPrevious);
        jpanScrollDate.add(Box.createHorizontalGlue());
        jpanScrollDate.add(lblCurrentMonthYear);
        jpanScrollDate.add(Box.createHorizontalGlue());
        jpanScrollDate.add(lblNext);

        JPanel jpanChooser = new JPanel(new BorderLayout());
        // jpanChooser.setBorder(BorderFactory.createTitledBorder("select date"));
        jpanChooser.add(jpanDateHolder, BorderLayout.CENTER);
        jpanChooser.add(jpanScrollDate, BorderLayout.SOUTH);

        this.getContentPane().add(jpanChooser, BorderLayout.CENTER);
        // this.getContentPane().add(jpanButtons, BorderLayout.SOUTH);

        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                close();
            }
        });
        this.pack();
        this.setSize(new Dimension(this.getSize().width+100, this.getSize().height+100));
    }

    /**
     * Sets the color of the labels according to the type of day.
     *
     * @param aZeroBaseDayIndex the index of the day, zero based
     * @param aLabel the label used for the date
     */
    private void setColour(int aZeroBaseDayIndex, JLabel aLabel) {
        switch(aZeroBaseDayIndex) {
            case 0:
                aLabel.setForeground(Color.RED);
                break;
            case 6:
                aLabel.setForeground(Color.BLUE);
                break;
        }
    }

    /**
     * Changes the text on the day label according to wether it has the mouse focus.
     *
     * @param aLblIndex
     * @param aHasFocus
     */
    private void mouseFocus(int aLblIndex, boolean aHasFocus) {
        String nbr = (iDays[aLblIndex] == null)?"":""+iDays[aLblIndex];
        if(aHasFocus) {
            lblDays[aLblIndex].setText("<html><u>" + nbr + "</u></html>");
        } else {
            lblDays[aLblIndex].setText(nbr);
        }
    }

    /**
     * Initiates the dates.
     */
    private void initDates() {
        iShownCalendar.set(Calendar.DATE, 1);
        int dayOfWeek = iShownCalendar.get(Calendar.DAY_OF_WEEK);
        int daysInMonth = iShownCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int displayDay = 1;
        for(int i=0;i<lblDays.length;i++) {
            // Remove any border.
            lblDays[i].setBorder(BorderFactory.createEmptyBorder());
            int dayNbr = i+1;
            if(dayNbr < dayOfWeek || displayDay > daysInMonth) {
                lblDays[i].setText("");
                iDays[i] = null;
            } else {
                // In one case, we want a special border here - if the label is
                // the currently selected date!
                if( iSelectedCalendar != null &&
                        ( iShownCalendar.get(Calendar.YEAR) == iSelectedCalendar.get(Calendar.YEAR) &&
                          iShownCalendar.get(Calendar.MONTH) == iSelectedCalendar.get(Calendar.MONTH) &&
                          displayDay == iSelectedCalendar.get(Calendar.DAY_OF_MONTH)
                        )) {
                    lblDays[i].setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
                }
                lblDays[i].setText("" + displayDay);
                iDays[i] = Integer.valueOf(displayDay);
                displayDay++;
            }
        }
        String currentMonthYear = iSDFCurrentMonthyear.format(iShownCalendar.getTime());
        lblCurrentMonthYear.setText(currentMonthYear);
        this.setTitle(iCoreTitle + " (" + currentMonthYear + ")");
    }

    /**
     * Creates and returns the button panel.
     *
     * @return the button panel
     */
    private JPanel getButtonPanel() {

        JButton btnOK = new JButton("OK");
        btnOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btnOKPressed();
            }
        });

        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                btnCancelPressed();
            }
        });

        JPanel jpanButtons = new JPanel();
        jpanButtons.setLayout(new BoxLayout(jpanButtons, BoxLayout.X_AXIS));
        jpanButtons.add(Box.createHorizontalGlue());
        jpanButtons.add(btnCancel);
        jpanButtons.add(Box.createHorizontalStrut(5));
        jpanButtons.add(btnOK);
        jpanButtons.add(Box.createHorizontalStrut(15));

        return jpanButtons;
    }

    /**
     * Called if the OK button is pressed.
     */
    private void btnOKPressed() {
        if(iSelectedCalendar != null) {
            close();
        } else {
            JOptionPane.showMessageDialog(this, "You have not yet selected a date!", "No date selected!", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Called if the cancel button is pressed.
     */
    private void btnCancelPressed() {
        iSelectedCalendar = null;
        close();
    }

    /**
     * Called if a date is clicked.
     *
     * @param aCounter the index if the clicked day
     */
    private void dateClicked(int aCounter) {
        iSelectedDayIndex = aCounter;
        int currentDay = iDays[iSelectedDayIndex].intValue();
        if(iSelectedCalendar == null) {
            iSelectedCalendar = Calendar.getInstance();
        }
        iSelectedCalendar.set(iShownCalendar.get(Calendar.YEAR), iShownCalendar.get(Calendar.MONTH), currentDay);
        for (int i = 0; i < lblDays.length; i++) {
            if(i == iSelectedDayIndex) {
                lblDays[i].setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            } else {
                lblDays[i].setBorder(BorderFactory.createEmptyBorder());
            }
        }
    }

    /**
     * Closes the DataChooser.
     */
    private void close() {
        DateChooser.this.setVisible(false);
        DateChooser.this.dispose();
    }

    /**
     * Creates and opens a new DateChooser dialog.
     *
     * @param args
     */
    public static void main(String[] args) {
        JFrame temp = new JFrame();
        DateChooser dc = new DateChooser(temp);
        dc.setVisible(true);
        Calendar c = dc.getSelectedDate();
        if(c != null) {
            logger.info("\n\nSelected date was: " + new SimpleDateFormat("dd-MM-yyyy").format(c.getTime()) + "\n\n");
        } else {
            logger.info("\n\nNo date selected!\n\n");
        }
        System.exit(0);
    }
}
