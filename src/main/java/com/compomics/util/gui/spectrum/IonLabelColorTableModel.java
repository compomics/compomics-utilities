package com.compomics.util.gui.spectrum;

import com.compomics.util.experiment.biology.Ion;
import com.compomics.util.experiment.biology.Ion.IonType;
import com.compomics.util.experiment.biology.IonFactory;
import com.compomics.util.experiment.biology.NeutralLoss;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JColorChooser;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Marc Vaudel
 */
public class IonLabelColorTableModel extends DefaultTableModel {

    private HashMap<String, Ion> ionMap;
    private ArrayList<String> keys;

    /**
     * Constructor which sets a new table.
     *
     * @param iontypes 
     * @param neutralLosses 
     */
    public IonLabelColorTableModel(HashMap<IonType, ArrayList<Integer>> iontypes, ArrayList<NeutralLoss> neutralLosses) {
        ionMap = new HashMap<String, Ion>();
        keys = new ArrayList<String>();
        Ion currentIon;
        for (IonType ionType : iontypes.keySet()) {
            if (ionType == IonType.IMMONIUM_ION) {
                //@TODO: we usually group immonium ions, is it a good idea?
                ionMap.put("Immonium Ion", Ion.getGenericIon(ionType, 0));
                keys.add("Immonium Ion");
            } else {
                for (Integer subtype : iontypes.get(ionType)) {
                    for (ArrayList<NeutralLoss> possibleCombination : IonFactory.getAccountedNeutralLosses(neutralLosses)) {
                        currentIon = Ion.getGenericIon(ionType, subtype, possibleCombination);
                        String key = currentIon.getName();
                        ionMap.put(key, currentIon);
                        keys.add(key);
                    }
                }
            }
        }
    }

    /**
     * Constructor which sets a new empty table.
     *
     */
    public IonLabelColorTableModel() {
        ionMap = new HashMap<String, Ion>();
        keys = new ArrayList<String>();
    }

    @Override
    public int getRowCount() {
        if (keys == null) {
            return 0;
        }
        return keys.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return " ";
            case 1:
                return "Ion";
            case 2:
                return "Color";
            default:
                return "";
        }
    }

    @Override
    public Object getValueAt(int row, int column) {
        switch (column) {
            case 0:
                return row + 1;
            case 1:
                return keys.get(row);
            case 2:
                return SpectrumPanel.determineDefaultFragmentIonColor(ionMap.get(keys.get(row)), true);
            default:
                return "";
        }
    }

    @Override
    public Class getColumnClass(int columnIndex) {
        for (int i = 0; i < getRowCount(); i++) {
            if (getValueAt(i, columnIndex) != null) {
                return getValueAt(i, columnIndex).getClass();
            }
        }
        return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 2;
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
        Color newColor = JColorChooser.showDialog(null, "Pick a Color", SpectrumPanel.determineDefaultFragmentIonColor(ionMap.get(keys.get(row)), true));
        SpectrumPanel.setIonColor(ionMap.get(keys.get(row)), newColor);
    }
}
