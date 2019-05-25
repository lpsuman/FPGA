package hr.fer.zemris.dipl.lukasuman.fpga.gui.local;

import javax.swing.*;

/**
 * This class represents a {@linkplain JMenu} which supports localization.
 * @author Luka Suman
 * @version 1.0
 */
public class LJMenu extends JMenu {

	private static final long serialVersionUID = 3006027052461167368L;

	private LocalizationHandler localizationHandler;

	public LJMenu(String key, LocalizationProvider lp) {
		super();
		localizationHandler = new LocalizationHandler(key, lp, this::updateText);
		updateText();
	}

	private void updateText() {
		setText(localizationHandler.getString());
	}
}
