package hr.fer.zemris.dipl.lukasuman.fpga.gui.local;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.MissingResourceException;

public class LJComboBoxRenderer extends DefaultListCellRenderer {

    private List<Object> items;
    private LocalizationProvider lp;
    private List<String> localizationKeys;

    public LJComboBoxRenderer(List<Object> items, LocalizationProvider lp, List<String> localizationKeys) {
        this.items = Utility.checkIfValidCollection(items, "items");
        this.lp = Utility.checkNull(lp, "loc provider");
        setLocalizationKeys(localizationKeys);
    }

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {

        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        setText(lp.getString(localizationKeys.get(items.indexOf(value))));

        if (index > -1 && value != null && localizationKeys != null && index < localizationKeys.size()) {
            try {
                list.setToolTipText(lp.getString(localizationKeys.get(index) + LocalizationKeys.SHORT_DESC_SUFFIX_KEY));
            } catch (MissingResourceException exc) {
                list.setToolTipText(lp.getString(localizationKeys.get(index)));
            }
        }

        return label;
    }

    public void setLocalizationKeys(List<String> localizationKeys) {
        this.localizationKeys = Utility.checkIfValidCollection(localizationKeys, "loc keys");
    }
}