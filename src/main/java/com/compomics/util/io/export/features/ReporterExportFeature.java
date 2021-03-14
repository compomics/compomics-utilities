package com.compomics.util.io.export.features;

import com.compomics.util.io.export.ExportFeature;

/**
 * Reporter extension of the utilities export features.
 *
 * @author Marc Vaudel
 * @author Harald Barsnes
 */
public interface ReporterExportFeature extends ExportFeature {

    /**
     * Indicates whether the feature is channel dependent.
     *
     * @return a boolean indicating whether the feature is channel dependent
     */
    public boolean hasChannels();
}
