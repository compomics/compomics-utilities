/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.util.protein_sequences_manager;

import com.compomics.util.gui.waiting.waitinghandlers.ProgressDialogX;
import com.compomics.util.protein_sequences_manager.gui.sequences_import.ImportSequencesFromUniprotDialog;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 *
 * @author Kenneth
 */
public class DownloadingUtil {

    public static boolean downloadFileFromURL(ImportSequencesFromUniprotDialog parent, URL url, File outputFile, ProgressDialogX dialog) throws IOException {
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();

        //start the downloading and update the dialog every read?
        BufferedInputStream in = null;
        FileOutputStream fout = null;
        try {
            in = new BufferedInputStream(url.openStream());
            fout = new FileOutputStream(outputFile);
            final byte data[] = new byte[1024];
            int totalCount = 0;
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                if (!parent.isCanceled() & !dialog.isRunCanceled()) {
                    fout.write(data, 0, count);
                    totalCount += count;
                    dialog.setSecondaryProgressText("Downloaded " + totalCount + " bytes");
                } else {
                    return false;
                }
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (fout != null) {
                fout.close();
            }
            dialog.setSecondaryProgressText("");
            dialog.resetSecondaryProgressCounter();
        }
        return true;
    }
}
