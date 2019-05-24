package hr.fer.zemris.dipl.lukasuman.fpga.gui.local;

/**
 * This interface defines a single method which a localization listener must have.
 * @author Luka Suman
 * @version 1.0
 */
public interface LocalizationListener {

	/**
	 * This method should be called whenever the localization changes.
	 */
	void localizationChanged();
}
