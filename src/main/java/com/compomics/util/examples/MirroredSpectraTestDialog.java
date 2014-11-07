package com.compomics.util.examples;

import com.compomics.util.gui.interfaces.SpectrumAnnotation;
import com.compomics.util.gui.spectrum.DefaultSpectrumAnnotation;
import com.compomics.util.gui.spectrum.SpectrumPanel;
import com.compomics.util.io.PklFile;
import java.awt.Color;
import java.io.File;
import java.util.Vector;

/**
 * Mirrored spectra test class.
 *
 * @author Harald Barsnes
 */
public class MirroredSpectraTestDialog extends javax.swing.JDialog {

    /**
     * Creates a new MirroredSpectraTestDialog
     *
     * @param parent
     * @param modal
     */
    public MirroredSpectraTestDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        try {
            File spectrumFileA = new File(getJarFilePath() + "/exampleFiles/exampleSpectrumA.pkl");
            PklFile pklFileA = new PklFile(spectrumFileA);

            File spectrumFileB = new File(getJarFilePath() + "/exampleFiles/exampleSpectrumB.pkl");
            PklFile pklFileB = new PklFile(spectrumFileB);

            SpectrumPanel spectrumPanel = new SpectrumPanel(
                    pklFileA.getMzValues(), pklFileA.getIntensityValues(),
                    pklFileA.getPrecursorMz(), "" + pklFileA.getPrecurorCharge(),
                    "" + pklFileA.getFileName(),
                    50, false, false, false, 2, false);

            // add a second normal spectrum
            //spectrumPanel.addAdditionalDataset(pklFileB.getMzValues(), pklFileB.getIntensityValues(), Color.BLUE, Color.BLUE);
            
            // add a second mirrored spectrum
            spectrumPanel.addMirroredSpectrum(pklFileB.getMzValues(), pklFileB.getIntensityValues(),
                    pklFileB.getPrecursorMz(), "" + pklFileB.getPrecurorCharge(),
                    "" + pklFileB.getFileName(), false, Color.BLUE, Color.BLUE);

            // add annotations for the normal spectra
            Vector<SpectrumAnnotation> currentAnnotations = new Vector();
            currentAnnotations.add(new DefaultSpectrumAnnotation(175.119495, -0.006822999999997137, SpectrumPanel.determineColorOfPeak("y1"), "y1"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(389.251235, 4.6299999996790575E-4, SpectrumPanel.determineColorOfPeak("y3"), "y3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(460.288345, -0.003290999999990163, SpectrumPanel.determineColorOfPeak("y4"), "y4"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(559.356755, -2.4200000007112976E-4, SpectrumPanel.determineColorOfPeak("y5"), "y5"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(660.404435, -0.002686000000039712, SpectrumPanel.determineColorOfPeak("y6"), "y6"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(820.4350840000001, 8.09999999091815E-5, SpectrumPanel.determineColorOfPeak("y7"), "y7"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(271.177006, -0.003444999999999254, SpectrumPanel.determineColorOfPeak("y2-NH3"), "y2-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(288.203555, -0.002484999999978754, SpectrumPanel.determineColorOfPeak("y2"), "y2"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(158.092946, -5.020000000115488E-4, SpectrumPanel.determineColorOfPeak("y1-NH3"), "y1-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(372.224686, 0.001030999999954929, SpectrumPanel.determineColorOfPeak("y3-NH3"), "y3-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(443.261796, 0.0025039999999876272, SpectrumPanel.determineColorOfPeak("y4-NH3"), "y4-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(274.12253400000003, 0.00181899999995494, SpectrumPanel.determineColorOfPeak("b2"), "b2"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(458.20561749999996, 0.05911150000002863, SpectrumPanel.determineColorOfPeak("Prec-H2O 2+"), "Prec-H2O 2+"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(129.000000, 0.10726900000000228, SpectrumPanel.determineColorOfPeak("iR"), "iR"));
            spectrumPanel.setAnnotations(currentAnnotations);

            // add annotations for the mirrored spectra
            currentAnnotations = new Vector();
            currentAnnotations.add(new DefaultSpectrumAnnotation(175.119495, -0.010621000000014647, SpectrumPanel.determineColorOfPeak("y1"), "y1"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(387.27196499999997, -0.0044499999999629836, SpectrumPanel.determineColorOfPeak("y3"), "y3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(500.356025, -0.002353999999968437, SpectrumPanel.determineColorOfPeak("y4"), "y4"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(571.393135, -0.004269000000022061, SpectrumPanel.determineColorOfPeak("y5"), "y5"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(685.436065, -0.013534999999933461, SpectrumPanel.determineColorOfPeak("y6"), "y6"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(813.494645, 0.005993999999986954, SpectrumPanel.determineColorOfPeak("y7"), "y7"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(257.161356, -0.007209999999986394, SpectrumPanel.determineColorOfPeak("y2-NH3"), "y2-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(370.245416, -9.159999999610591E-4, SpectrumPanel.determineColorOfPeak("y3-NH3"), "y3-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(796.468096, 0.0018540000000939472, SpectrumPanel.determineColorOfPeak("y7-NH3"), "y7-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(274.187905, -0.004702000000008866, SpectrumPanel.determineColorOfPeak("y2"), "y2"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(158.092946, -0.008444000000025653, SpectrumPanel.determineColorOfPeak("y1-NH3"), "y1-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(668.4095159999999, 0.0019680000000334985, SpectrumPanel.determineColorOfPeak("y6-NH3"), "y6-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(276.134815, -0.002712000000030912, SpectrumPanel.determineColorOfPeak("b2"), "b2"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(259.108266, -0.004803000000038082, SpectrumPanel.determineColorOfPeak("b2-NH3"), "b2-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(242.164738, -0.08587800000000811, SpectrumPanel.determineColorOfPeak("y4++-NH3"), "y4++-NH3"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(129, 0.09981500000000665, SpectrumPanel.determineColorOfPeak("iR"), "iR"));
            currentAnnotations.add(new DefaultSpectrumAnnotation(120, 0.08159999999999457, SpectrumPanel.determineColorOfPeak("iF"), "iF"));
            spectrumPanel.setAnnotationsMirrored(currentAnnotations);

            // add a third mirrored spectrum
//            spectrumPanel.addMirroredSpectrum(pklFileA.getMzValues(), pklFileA.getIntensityValues(),
//                    pklFileA.getPrecursorMz(), "" + pklFileA.getPrecurorCharge(),
//                    "" + pklFileA.getFileName(), false, Color.GREEN, Color.BLUE);
            backgroundPanel.add(spectrumPanel);

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

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Mirrored Spectra Demo");

        backgroundPanel.setLayout(new javax.swing.BoxLayout(backgroundPanel, javax.swing.BoxLayout.LINE_AXIS));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(backgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 1085, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(backgroundPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 650, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(MirroredSpectraTestDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(MirroredSpectraTestDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(MirroredSpectraTestDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MirroredSpectraTestDialog.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                MirroredSpectraTestDialog dialog = new MirroredSpectraTestDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel backgroundPanel;
    // End of variables declaration//GEN-END:variables

    /**
     * Returns the path to the jar file.
     *
     * @return the path to the jar file
     */
    private String getJarFilePath() {
        String path = this.getClass().getResource("MirroredSpectraTestDialog.class").getPath();

        if (path.lastIndexOf("/utilities-") != -1) {
            // remove starting 'file:' tag if there
            if (path.startsWith("file:")) {
                path = path.substring("file:".length(), path.lastIndexOf("/utilities-"));
            } else {
                path = path.substring(0, path.lastIndexOf("/utilities-"));
            }
            path = path.replace("%20", " ");
            path = path.replace("%5b", "[");
            path = path.replace("%5d", "]");
        } else {
            path = ".";
        }

        return path;
    }
}
