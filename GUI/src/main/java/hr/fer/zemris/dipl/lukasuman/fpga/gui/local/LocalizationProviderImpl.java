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
public class LocalizationProviderImpl extends AbstractLocalizationProvider {

	/**Stored singleton instance.*/
	private static final LocalizationProviderImpl instance = new LocalizationProviderImpl();

	/**The language (hr/en/de).*/
	private String language;

	/**The resource bundle.*/
	private ResourceBundle bundle;

	/**
	 * Creates a new {@link LocalizationProviderImpl}.
	 */
	private LocalizationProviderImpl() {
		super();
	}

	/**
	 * Used to get the singleton instance of {@linkplain LocalizationProviderImpl}.
	 * @return Returns the instance.
	 */
	public static LocalizationProviderImpl getInstance() {
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
		return bundle.getString(key);
	}

	/**
	 * @return Returns the {@link #language}.
	 */
	public String getLanguage() {
		return language;
	}
}
