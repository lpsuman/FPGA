package hr.fer.zemris.dipl.lukasuman.fpga.gui.local;

/**
 * This interface defines the methods that a localization provider must have.
 * @author Luka Suman
 * @version 1.0
 */
public interface LocalizationProvider {

	/**
	 * Used to register a localization listener.
	 * @param listener The listener.
	 * */
	void addLocalizationListener(LocalizationListener listener);

	/**
	 * Used to unregister a localization listener.
	 * @param listener The listener.
	 */
	void removeLocalizationListener(LocalizationListener listener);

	/**
	 * Used to get the localization of the specified key.
	 * @param key The key.
	 * @return Returns the localization.
	 */
	String getString(String key);
}
