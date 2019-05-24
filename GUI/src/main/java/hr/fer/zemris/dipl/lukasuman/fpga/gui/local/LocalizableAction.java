package hr.fer.zemris.dipl.lukasuman.fpga.gui.local;

import javax.swing.*;
import java.util.MissingResourceException;

/**
 * This class represents an {@linkplain AbstractAction} which supports localization.
 * @author Luka Suman
 * @version 1.0
 */
public abstract class LocalizableAction extends AbstractAction {

	/**Serial ID.*/
	private static final long serialVersionUID = 6383078233483329169L;

	/**Key suffix for fetching action's description.*/
	private static final String KEY_SHORT_DESC_SUFFIX = "_desc";

	/**The localization key.*/
	private String key;

	/**The localization provider.*/
	private LocalizationProvider lp;

	/**
	 * Creates a new {@link LocalizableAction} with the specified parameters.
	 * @param key See {@linkplain #key}.
	 * @param lp See {@linkplain #lp}.
	 */
	public LocalizableAction(String key, LocalizationProvider lp) {
		super();
		if (key == null || lp ==  null) {
			throw new IllegalArgumentException("Both key and provider must not be null!");
		}
		this.key = key;
		this.lp = lp;

		updateValues();

		lp.addLocalizationListener(() -> {
			updateValues();
		});
	}

	/**
	 * Updates the values which depend on the localization.
	 */
	private void updateValues() {
		putValue(Action.NAME, lp.getString(key));
		try {
			putValue(Action.SHORT_DESCRIPTION, lp.getString(key + KEY_SHORT_DESC_SUFFIX));
		} catch (MissingResourceException exc) {
			putValue(Action.SHORT_DESCRIPTION, lp.getString(key));
		}
	}
}
