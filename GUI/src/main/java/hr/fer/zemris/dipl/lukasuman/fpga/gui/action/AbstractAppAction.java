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
import java.nio.file.Paths;

/**
 * This class is used to store functionalities common to all actions.
 * @author Luka Suman
 * @version 1.0
 */
public abstract class AbstractAppAction extends LocalizableAction {

	protected static final FileNameExtensionFilter JSON_FILE_FILTER = new FileNameExtensionFilter(".json (JSON file format)", "json");
	protected static final FileNameExtensionFilter TEXT_FILE_FILTER = new FileNameExtensionFilter(".txt (Text File)", "txt");

	/**The parent application.*/
	protected JFPGA jfpga;

	public AbstractAppAction(JFPGA jfpga, String localizationKey) {
		super(localizationKey, Utility.checkNull(jfpga, "JFPGA").getFlp());
		this.jfpga = jfpga;

	}

	/**
	 * Sets the actions values.
	 * @param keyStroke The keystroke (combination).
	 * @param keyEvent The key event (mnemonic).
	 */
	protected void setValues(String keyStroke, int keyEvent) {
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(keyStroke));
		putValue(Action.MNEMONIC_KEY, keyEvent);
	}

	private JFileChooser getFileChooser(String titleKey, FileFilter fileFilter, boolean enableMultiSelection) {
		JFileChooser jfc = new JFileChooser(GUIConstants.getLastSessionsFilePath());
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

	protected Path[] askForFilesToOpen(String openDialogTitleKey, FileFilter fileFilter) {
		JFileChooser jfc = getFileChooser(openDialogTitleKey, fileFilter, true);

		if (jfc.showOpenDialog(jfpga) != JFileChooser.APPROVE_OPTION) {
			return null;
		}

		File[] fileNames = jfc.getSelectedFiles();
		Path[] filePaths = new Path[fileNames.length];

		for (int i = 0; i < filePaths.length; i++) {
			filePaths[i] = fileNames[i].toPath();

			if (!Files.isReadable(filePaths[i])) {
				jfpga.showErrorMsg(String.format(jfpga.getFlp().getString(LocalizationKeys.FILE_S_DOES_NOT_EXIST_KEY),
						fileNames[i].getAbsolutePath()));
				filePaths[i] = null;
			}
		}

		return filePaths;
	}

	protected void warnCouldNotOpen(Path filePath, String reasonMsgKey) {
		jfpga.showErrorMsg(String.format(jfpga.getFlp().getString(LocalizationKeys.FILE_S_COULD_NOT_BE_OPENED_KEY) + "\n%s",
				filePath, jfpga.getFlp().getString(reasonMsgKey)));
	}

	protected void warnNothingToSave(String nothingToSaveKey) {
		jfpga.showWarningMsg(jfpga.getFlp().getString(nothingToSaveKey));
	}

	protected Path askForSaveDestination(String saveDialogTitleKey, FileNameExtensionFilter fileFilter) {
		JFileChooser jfc = getFileChooser(saveDialogTitleKey, fileFilter, false);

		if (jfc.showSaveDialog(jfpga) != JFileChooser.APPROVE_OPTION) {
			jfpga.showWarningMsg(jfpga.getFlp().getString(LocalizationKeys.NOTHING_WAS_SAVED_KEY));
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
		} else {
			String pathStr = destinationFilePath.toString();
			int dotIndex = pathStr.lastIndexOf('.');

			if (dotIndex == -1) {
				pathStr += "." + fileFilter.getExtensions()[0];
				destinationFilePath = Paths.get(pathStr);
			}
		}

		return destinationFilePath;
	}

	protected void warnCouldNotSave(Path filePath, String reasonMsgKey) {
		jfpga.showWarningMsg(String.format(jfpga.getFlp().getString(LocalizationKeys.FILE_S_COULD_NOT_BE_SAVED_KEY) + "\n%s",
				filePath, jfpga.getFlp().getString(reasonMsgKey)));
	}

	protected void notifyFileSaved(Path filePath) {
		jfpga.showInfoMsg(String.format(jfpga.getFlp().getString(LocalizationKeys.FILE_S_SAVED_KEY), filePath));
	}
}
