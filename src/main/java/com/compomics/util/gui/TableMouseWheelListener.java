package com.compomics.util.gui;

import com.compomics.util.gui.tablemodels.SelfUpdatingTableModel;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JTable;

/**
 * MouseWheelListener for SelfUpdatingTableModels that supports scrolling in a
 * more efficient way.
 *
 * @author Harald Barsnes
 */
public class TableMouseWheelListener implements MouseWheelListener {

    /**
     * The table to be monitored. Must have a SelfUpdatingTableModel as the
     * table model.
     */
    private JTable table;
    /**
     * The last time the a scrolling event was triggered.
     */
    private long lastScrollEvent = System.currentTimeMillis();
    /**
     * The time (in milliseconds) to wait after the last scrolling event before
     * checking if the user is done scrolling and if so update the table
     * content.
     */
    private int scrollingTableUpdateDelay = 1500;

    /**
     * Constructor.
     *
     * @param table the table
     */
    public TableMouseWheelListener(JTable table) {
        this.table = table;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        long now = System.currentTimeMillis();

        ((SelfUpdatingTableModel) table.getModel()).setIsScrolling(true);
        table.revalidate();
        table.repaint();

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                if (now - lastScrollEvent > (scrollingTableUpdateDelay - 700)) {
                    ((SelfUpdatingTableModel) table.getModel()).setIsScrolling(false);
                    table.revalidate();
                    table.repaint();
                }
            }
        }, scrollingTableUpdateDelay);

        lastScrollEvent = now;
    }
}
