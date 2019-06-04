package hr.fer.zemris.dipl.lukasuman.fpga.gui.local;

import javax.swing.*;
import java.util.Arrays;
import java.util.List;

public class LJComboBox<E> extends JComboBox<E> {

    public LJComboBox(E[] items, LocalizationProvider lp, List<String> localizationKeys) {
        super(items);
        setRenderer(new LJComboBoxRenderer(Arrays.asList(items), lp, localizationKeys));
        if (localizationKeys.size() != items.length) {
            throw new IllegalArgumentException("Number of items and localization keys must be equal.");
        }
        lp.addLocalizationListener(this::revalidate);
    }
}
