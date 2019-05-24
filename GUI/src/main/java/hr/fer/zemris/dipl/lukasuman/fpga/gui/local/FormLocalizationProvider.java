package hr.fer.zemris.dipl.lukasuman.fpga.gui.local;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * This class represents a {@linkplain LocalizationProvider} which is used to automatically
 * disconnect the specified frame from the specified {@linkplain LocalizationProvider} when the
 * window is closed.
 * @author Luka Suman
 * @version 1.0
 */
public class FormLocalizationProvider extends LocalizationProviderBridge {

	/**
	 * Creates a new {@link FormLocalizationProvider} with the specified parameters. The specified
	 * provider and frame are "connected" when the frame is opened and automatically closed when
	 * the frame is closed.
	 * @param provider The localization provider.
	 * @param frame The frame.
	 */
	public FormLocalizationProvider(LocalizationProvider provider, Frame frame) {
		super(provider);
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowOpened(WindowEvent e) {
				connect();
			}

			@Override
			public void windowClosed(WindowEvent e) {
				disconnect();
			}
		});
	}
}
