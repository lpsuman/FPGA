package hr.fer.zemris.dipl.lukasuman.fpga.gui.local;

import javax.swing.*;
import java.util.MissingResourceException;

/**
 * This class represents an {@linkplain AbstractAction} which supports localization.
 * @author Luka Suman
 * @version 1.0
 */
public abstract class LocalizableAction extends AbstractAction {

	private static final long serialVersionUID = 6383078233483329169L;

	private LocalizationHandler localizationHandler;

	public LocalizableAction(String localizationKey, LocalizationProvider lp) {
		super();
		localizationHandler = new LocalizationHandler(localizationKey, lp, this::updateValues);
		updateValues();
	}

	/**
	 * Updates the values which depend on the localization.
	 */
	private void updateValues() {
		putValue(Action.NAME, localizationHandler.getString());
		try {
			putValue(Action.SHORT_DESCRIPTION, localizationHandler.getLp()
					.getString(localizationHandler.getKey() + LocalizationKeys.SHORT_DESC_SUFFIX_KEY));
		} catch (MissingResourceException exc) {
			putValue(Action.SHORT_DESCRIPTION, localizationHandler.getString());
		}
	}

	protected String getLocalizationKey() {
		return localizationHandler.getKey();
	}
}
