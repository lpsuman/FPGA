package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.session;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.AbstractAppAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

/**
 * This class represents an action which is performed when the applications needs to be shut down.
 * @author Luka Suman
 * @version 1.0
 */
public class ExitAction extends AbstractAppAction {

	private static final long serialVersionUID = -5050548213592449042L;

	/**Accelerator key (shortcut).*/
	private static final String ACC_KEY = "alt F4";

	/**Mnemonic key.*/
	private static final int MNEMONIC_KEY = KeyEvent.VK_ESCAPE;

	public ExitAction(JFPGA jfpga) {
		super(jfpga, LocalizationKeys.EXIT_KEY);
		setValues(ACC_KEY, MNEMONIC_KEY);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		jfpga.dispatchEvent(new WindowEvent(jfpga, WindowEvent.WINDOW_CLOSING));
	}
}
