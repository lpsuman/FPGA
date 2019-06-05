package hr.fer.zemris.dipl.lukasuman.fpga.gui.local;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.GUIConstants;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.GUIUtility;

import javax.swing.*;
import java.util.MissingResourceException;

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
		setBorder(GUIUtility.getBorder(GUIConstants.DEFAULT_LABEL_BORDER_SIZE));
		localizationHandler = new LocalizationHandler(key, lp, this::updateText);
		updateText();
	}

	public LJLabel(String key, LocalizationProvider lp, int horizontalAlignment) {
		this(key, lp);
		setHorizontalAlignment(horizontalAlignment);
	}

	private void updateText() {
		setText(localizationHandler.getString());
		try {
			setToolTipText(localizationHandler.getLp().getString(localizationHandler.getKey() + LocalizationKeys.SHORT_DESC_SUFFIX_KEY));
		} catch (MissingResourceException exc) {
			setToolTipText(localizationHandler.getString());
		}
	}
}
