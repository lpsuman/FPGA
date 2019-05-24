package hr.fer.zemris.dipl.lukasuman.fpga.gui.local;

import javax.swing.*;

/**
 * This class represents a {@linkplain JLabel} which supports localization.
 * @author Luka Suman
 * @version 1.0
 */
public class LJLabel extends JLabel {

	/**Serial ID.*/
	private static final long serialVersionUID = -7060968585517028424L;

	/**Delimiter for the status bar labels (between text and number).*/
	private static final String STATUS_LABEL_DELIMITER = ": ";

	/**The localization key.*/
	private String key;

	/**The localization provider.*/
	private LocalizationProvider lp;

	/**
	 * Creates a new {@link LJLabel} with the specified parameters.
	 * @param key See {@linkplain #key}.
	 * @param lp See {@linkplain #lp}.
	 */
	public LJLabel(String key, LocalizationProvider lp) {
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
	 * Updates the labels text (it depends on the localization).
	 */
	private void updateText() {
		String previousText = getText();
		int pos = previousText.indexOf(STATUS_LABEL_DELIMITER);
		String newText = STATUS_LABEL_DELIMITER;
		if (pos > 0) {
			newText = previousText.substring(pos);
		}
		setText(lp.getString(key) + newText);
	}
}
