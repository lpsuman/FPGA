package hr.fer.zemris.dipl.lukasuman.fpga.gui.action;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.GUIConstants;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizableAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This class is used to store functionalities common to all actions.
 * @author Luka Suman
 * @version 1.0
 */
public abstract class AbstractAppAction extends LocalizableAction {

	private static final long serialVersionUID = -4311960612363301231L;

	protected static FileFilter sessionFileFilter = new FileNameExtensionFilter(".ser (Java Serializable)", "ser");

	/**The parent application.*/
	protected JFPGA jfpga;

	public AbstractAppAction(JFPGA jfpga, String key) {
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

	private JFileChooser getFileChooser(String titleKey, FileFilter fileFilter, boolean enableMultiSelection) {
		JFileChooser jfc = new JFileChooser(GUIConstants.DATA_ABSOLUTE_PATH);
		jfc.setDialogTitle(jfpga.getFlp().getString(titleKey));

		if (fileFilter != null) {
			jfc.setAcceptAllFileFilterUsed(false);
			jfc.addChoosableFileFilter(fileFilter);
			jfc.addChoosableFileFilter(jfc.getAcceptAllFileFilter());
		}

		if (enableMultiSelection) {
			jfc.setMultiSelectionEnabled(true);
		}

		return jfc;
	}

	public Path[] askForFilesToOpen(String openDialogTitleKey, FileFilter fileFilter) {
		JFileChooser jfc = getFileChooser(openDialogTitleKey, fileFilter, true);

		if (jfc.showOpenDialog(jfpga) != JFileChooser.APPROVE_OPTION) {
			return null;
		}

		File[] fileNames = jfc.getSelectedFiles();
		Path[] filePaths = new Path[fileNames.length];

		for (int i = 0; i < filePaths.length; i++) {
			filePaths[i] = fileNames[i].toPath();

			if (!Files.isReadable(filePaths[i])) {
				JOptionPane.showMessageDialog(
						jfpga,
						String.format(jfpga.getFlp().getString(LocalizationKeys.FILE_S_DOES_NOT_EXIST),
								fileNames[i].getAbsolutePath()),
						jfpga.getFlp().getString(LocalizationKeys.ERROR_KEY),
						JOptionPane.ERROR_MESSAGE);
				filePaths[i] = null;
			}
		}

		return filePaths;
	}

	public void warnCouldNotOpen(Path filePath, String reasonMsgKey) {
		JOptionPane.showMessageDialog(
				jfpga,
				String.format(jfpga.getFlp().getString(LocalizationKeys.FILE_S_COULD_NOT_BE_OPENED_KEY) + "\n%s",
						filePath, jfpga.getFlp().getString(reasonMsgKey)),
				jfpga.getFlp().getString(LocalizationKeys.ERROR_KEY),
				JOptionPane.ERROR_MESSAGE);
	}

	public void warnNothingToSave(String nothingToSaveKey) {
		JOptionPane.showMessageDialog(
				jfpga,
				jfpga.getFlp().getString(nothingToSaveKey),
				jfpga.getFlp().getString(LocalizationKeys.WARNING_KEY),
				JOptionPane.WARNING_MESSAGE);
	}

	public Path askForSaveDestination(String saveDialogTitleKey, FileFilter fileFilter) {
		JFileChooser jfc = getFileChooser(saveDialogTitleKey, fileFilter, false);

		if (jfc.showSaveDialog(jfpga) != JFileChooser.APPROVE_OPTION) {
			JOptionPane.showMessageDialog(
					jfpga,
					jfpga.getFlp().getString(LocalizationKeys.NOTHING_WAS_SAVED_KEY),
					jfpga.getFlp().getString(LocalizationKeys.WARNING_KEY),
					JOptionPane.WARNING_MESSAGE);
			return null;
		}

		Path destinationFilePath = jfc.getSelectedFile().toPath();
		if (Files.exists(destinationFilePath)) {
			int decision = JOptionPane.showConfirmDialog(
					jfpga,
					String.format("%s %s", jfpga.getFlp().getString(LocalizationKeys.FILE_ALREADY_EXISTS_KEY),
							jfpga.getFlp().getString(LocalizationKeys.OVERWRITE_KEY)),
					jfpga.getFlp().getString(LocalizationKeys.WARNING_KEY),
					JOptionPane.YES_NO_OPTION);
			if (decision == JOptionPane.NO_OPTION) {
				return null;
			}
		}

		return destinationFilePath;
	}

	public void warnCouldNotSave(Path filePath, String reasonMsgKey) {
		JOptionPane.showMessageDialog(
				jfpga,
				String.format(jfpga.getFlp().getString(LocalizationKeys.FILE_S_COULD_NOT_BE_SAVED_KEY) + "\n%s",
						filePath, jfpga.getFlp().getString(reasonMsgKey)),
				jfpga.getFlp().getString(LocalizationKeys.ERROR_KEY),
				JOptionPane.WARNING_MESSAGE);
	}

	public void notifyFileSaved(Path filePath) {
		JOptionPane.showMessageDialog(
				jfpga,
				String.format(jfpga.getFlp().getString(LocalizationKeys.FILE_S_SAVED_KEY), filePath),
				jfpga.getFlp().getString(LocalizationKeys.NOTIFICATION_KEY),
				JOptionPane.INFORMATION_MESSAGE);
	}
}
