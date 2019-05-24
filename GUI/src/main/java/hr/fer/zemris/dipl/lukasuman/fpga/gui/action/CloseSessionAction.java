package hr.fer.zemris.dipl.lukasuman.fpga.gui.action;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * This class represents an action which is performed when a tab needs to be closed.
 * @author Luka Suman
 * @version 1.0
 */
public class CloseSessionAction extends AbstractSessionAction {

	/**Serial ID.*/
	private static final long serialVersionUID = -3526670863895113909L;

	/**Localization key for this action.*/
	private static final String LOCAL_KEY = "close";

	/**Accelerator key (shortcut).*/
	private static final String ACC_KEY = "control Q";

	/**Mnemonic key.*/
	private static final int MNEMONIC_KEY = KeyEvent.VK_Q;

	public CloseSessionAction(JFPGA jfpga) {
		super(jfpga, LOCAL_KEY);
		setValues(ACC_KEY, MNEMONIC_KEY);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
//		if (e.getSource() instanceof JButton) {
//			JButton button = (JButton)e.getSource();
//			Component tabComp = notepad.getMapCloseButtonToComp().get(button);
//			if (tabComp != null) {
//				int newIndex = tabPane.indexOfComponent(tabComp);
//				tabPane.setSelectedIndex(newIndex);
//			}
//		}
//
//		notepad.removeTab(tabPane.getSelectedIndex());
	}

}
