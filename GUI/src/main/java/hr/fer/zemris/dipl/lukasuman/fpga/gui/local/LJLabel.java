package hr.fer.zemris.dipl.lukasuman.fpga.gui.local;

import javax.swing.*;

/**
 * This class represents a {@linkplain JLabel} which supports localization.
 * @author Luka Suman
 * @version 1.0
 */
public class LJLabel extends JLabel {

	private static final long serialVersionUID = -7060968585517028424L;

	private LocalizationHandler localizationHandler;

	public LJLabel(String key, LocalizationProvider lp) {
		super();
		localizationHandler = new LocalizationHandler(key, lp, this::updateText);
		updateText();
	}

	private void updateText() {
		setText(localizationHandler.getString());
	}
}
