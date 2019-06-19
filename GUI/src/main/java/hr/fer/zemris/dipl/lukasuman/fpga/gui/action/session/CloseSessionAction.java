package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.session;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.AbstractAppAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.LocalizationKeys;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * This class represents an action which is performed when a tab needs to be closed.
 * @author Luka Suman
 * @version 1.0
 */
public class CloseSessionAction extends AbstractAppAction {

	/**Accelerator key (shortcut).*/
	private static final String ACC_KEY = "control Q";

	/**Mnemonic key.*/
	private static final int MNEMONIC_KEY = KeyEvent.VK_Q;

	public CloseSessionAction(JFPGA jfpga) {
		super(jfpga, LocalizationKeys.CLOSE_KEY);
		setValues(ACC_KEY, MNEMONIC_KEY);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton) {
			JButton button = (JButton)e.getSource();
			Component component = jfpga.getMapCloseButtonToComp().get(button);
			if (component != null) {
				int newIndex = jfpga.getIndexComponent(component);
				jfpga.setCurrentSession(newIndex);
			}
		}

		jfpga.removeSession(jfpga.getIndexCurrentSession());
	}
}
