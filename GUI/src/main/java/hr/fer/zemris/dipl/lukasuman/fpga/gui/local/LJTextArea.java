package hr.fer.zemris.dipl.lukasuman.fpga.gui.local;

import javax.swing.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class LJTextArea extends JTextArea {

    private LocalizationHandler localizationHandler;
    private boolean isTempTextDisplayed;

    public LJTextArea(String key, LocalizationProvider lp) {
        super();
        localizationHandler = new LocalizationHandler(key, lp, this::updateText);
        isTempTextDisplayed = true;
        updateText();

        addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (isTempTextDisplayed) {
                    isTempTextDisplayed = false;
                    setText("");
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (getText().trim().isEmpty()) {
                    isTempTextDisplayed = true;
                    updateText();
                }
            }
        });
    }

    private void updateText() {
        if (isTempTextDisplayed) {
            setText(localizationHandler.getString());
        }
    }
}
