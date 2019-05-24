package hr.fer.zemris.dipl.lukasuman.fpga.gui.local;

/**
 * This class is a decorator for {@linkplain LocalizationProvider}. It is able to
 * connect/disconnect to the parent provider so that the listeners can be collected by the garbage
 * collector.
 * @author Luka Suman
 * @version 1.0
 */
public class LocalizationProviderBridge extends AbstractLocalizationProvider {

	/**The parent localization provider.*/
	private LocalizationProvider parent;

	/**Listener used as a bridge.*/
	private LocalizationListener bridgeListener = () -> {
		fire();
	};

	/**If there is currently a connection.*/
	private boolean isConnected;

	/**
	 * Creates a new {@link LocalizationProviderBridge} with the specified parameter.
	 * @param parent See {@linkplain #parent}.
	 */
	public LocalizationProviderBridge(LocalizationProvider parent) {
		super();
		if (parent == null) {
			throw new IllegalArgumentException("Parent must not be null!");
		}
		this.parent = parent;
	}

	@Override
	public String getString(String key) {
		return parent.getString(key);
	}

	/**
	 * Connects this bridge with the parent.
	 */
	public void connect() {
		if (!isConnected) {
			parent.addLocalizationListener(bridgeListener);
			isConnected = true;
		}
	}

	/**
	 * Disconnects this bridge from the parent.
	 */
	public void disconnect() {
		if (isConnected) {
			parent.removeLocalizationListener(bridgeListener);
			isConnected = false;
		}
	}
}
