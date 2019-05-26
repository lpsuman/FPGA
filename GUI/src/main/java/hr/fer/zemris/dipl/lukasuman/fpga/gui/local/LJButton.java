package hr.fer.zemris.dipl.lukasuman.fpga.gui.local;

import javax.swing.*;

public class LJButton extends JButton {

    private LocalizationHandler localizationHandler;

    public LJButton(String key, LocalizationProvider lp) {
        super();
        localizationHandler = new LocalizationHandler(key, lp, this::updateText);
        updateText();
    }

    private void updateText() {
        setText(localizationHandler.getString());
    }
}
