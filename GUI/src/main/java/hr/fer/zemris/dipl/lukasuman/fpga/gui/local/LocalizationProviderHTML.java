package hr.fer.zemris.dipl.lukasuman.fpga.gui.local;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.GUIConstants;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * This class represents a singleton localization provider.
 * @author Luka Suman
 * @version 1.0
 */
public class LocalizationProviderHTML extends AbstractLocalizationProvider {

	/**Stored singleton instance.*/
	private static final LocalizationProviderHTML instance = new LocalizationProviderHTML();

	/**The language (hr/en/de).*/
	private String language;

	/**The resource bundle.*/
	private ResourceBundle bundle;

	/**
	 * Creates a new {@link LocalizationProviderHTML}.
	 */
	private LocalizationProviderHTML() {
		super();
	}

	/**
	 * Used to get the singleton instance of {@linkplain LocalizationProviderHTML}.
	 * @return Returns the instance.
	 */
	public static LocalizationProviderHTML getInstance() {
		return instance;
	}

	/**
	 * @param language The {@link #language} to set.
	 */
	public void setLanguage(String language) {
		Utility.checkNull(language, "language");
		if (this.language == null || !this.language.equals(language)) {
			this.language = language;
			Locale locale = Locale.forLanguageTag(language);
			bundle = ResourceBundle.getBundle(GUIConstants.TRANSLATION_BUNDLE_PATH, locale);
			fire();
		}
	}

	@Override
	public String getString(String key) {
		Utility.checkNull(key, "localization key");
		String result = bundle.getString(key);

		if (result.contains("<br>")) {
			result = "<html><center>" + result + "</center></html>";
		}

		return result;
	}

	/**
	 * @return Returns the {@link #language}.
	 */
	public String getLanguage() {
		return language;
	}
}
