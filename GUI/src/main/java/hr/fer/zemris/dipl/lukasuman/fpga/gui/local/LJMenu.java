package hr.fer.zemris.dipl.lukasuman.fpga.gui.local;

import javax.swing.*;

/**
 * This class represents a {@linkplain JMenu} which supports localization.
 * @author Luka Suman
 * @version 1.0
 */
public class LJMenu extends JMenu {

	/**Serial key.*/
	private static final long serialVersionUID = 3006027052461167368L;

	/**The localization key.*/
	private String key;

	/**The localization provider.*/
	private LocalizationProvider lp;

	/**
	 * Creates a new {@link LJMenu} with the specified parameters.
	 * @param key See {@linkplain #key}.
	 * @param lp See {@linkplain #lp}.
	 */
	public LJMenu(String key, LocalizationProvider lp) {
		super();
		if (key == null || lp ==  null) {
			throw new IllegalArgumentException("Both key and provider must not be null!");
		}
		this.key = key;
		this.lp = lp;

		updateText();

		lp.addLocalizationListener(() -> {
			updateText();
		});
	}

	/**
	 * Updates the menus text according to the localization.
	 */
	private void updateText() {
		setText(lp.getString(key));
	}
}
