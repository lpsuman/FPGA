package hr.fer.zemris.dipl.lukasuman.fpga.gui.local;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class LJTextField extends JTextField {

    private LocalizationHandler localizationHandler;

    public LJTextField(String key, LocalizationProvider lp) {
        super();
        localizationHandler = new LocalizationHandler(key, lp, this::updateText);
        updateText();

        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (getText().equals(localizationHandler.getString())) {
                    setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (getText().trim().isEmpty()) {
                    updateText();
                }
            }
        });
    }

    private void updateText() {
        setText(localizationHandler.getString());
    }
}
