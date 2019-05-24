package hr.fer.zemris.dipl.lukasuman.fpga.gui.action;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizableAction;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import javax.swing.*;

/**
 * This class is used to store functionalities common to all actions.
 * @author Luka Suman
 * @version 1.0
 */
public abstract class AbstractSessionAction extends LocalizableAction {

	/**Serial ID.*/
	private static final long serialVersionUID = -4311960612363301231L;

	/**The parent application.*/
	protected JFPGA jfpga;


	public AbstractSessionAction(JFPGA jfpga, String key) {
		super(key, Utility.checkNull(jfpga, "JFPGA").getFlp());
		this.jfpga = jfpga;
	}

	/**
	 * Sets the actions values.
	 * @param keyStroke The keystroke (combination).
	 * @param keyEvent The key event (mnemonic).
	 */
	public void setValues(String keyStroke, int keyEvent) {
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(keyStroke));
		putValue(Action.MNEMONIC_KEY, keyEvent);
	}
}
