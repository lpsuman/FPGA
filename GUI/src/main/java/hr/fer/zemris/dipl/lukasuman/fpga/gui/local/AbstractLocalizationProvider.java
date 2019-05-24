package hr.fer.zemris.dipl.lukasuman.fpga.gui.local;

import java.util.ArrayList;
import java.util.List;

/**
 * This abstract class implements two functionalities from the {@linkplain LocalizationProvider}
 * interface: adding and removing listeners.
 * @author Luka Suman
 * @version 1.0
 */
public abstract class AbstractLocalizationProvider implements LocalizationProvider {

	/**Error message when given listener is null.*/
	private static final String ERR_MSG = "Listener must not be null!";

	/**List of listeners.*/
	private List<LocalizationListener> listeners;

	/**
	 * Used to initialize the abstract class {@link AbstractLocalizationProvider}.
	 */
	protected AbstractLocalizationProvider() {
		listeners = new ArrayList<>();
	}

	@Override
	public void addLocalizationListener(LocalizationListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException(ERR_MSG);
		}
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	@Override
	public void removeLocalizationListener(LocalizationListener listener) {
		if (listener == null) {
			throw new IllegalArgumentException(ERR_MSG);
		}
		listeners.remove(listener);
	}

	/**
	 * Used to notify all listeners that the localization has changed.
	 */
	protected void fire() {
		for (LocalizationListener listener : listeners) {
			listener.localizationChanged();
		}
	}
}
