package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.session;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.AbstractAppAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.session.SessionController;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.session.SessionData;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.nio.file.Files;
import java.nio.file.Path;

public class SaveSessionAction extends AbstractAppAction {

    private static final long serialVersionUID = 5998729400009704486L;

    /**Accelerator key (shortcut).*/
    private static final String ACC_KEY = "control S";

    /**Mnemonic key.*/
    private static final int MNEMONIC_KEY = KeyEvent.VK_S;

    public SaveSessionAction(JFPGA jfpga) {
        super(jfpga, LocalizationKeys.SAVE_KEY);
        setValues(ACC_KEY, MNEMONIC_KEY);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SessionController currentSession = jfpga.getCurrentSession();

        if (currentSession == null) {
            JOptionPane.showMessageDialog(
                    jfpga,
                    jfpga.getFlp().getString(LocalizationKeys.THERE_IS_NO_SESSION_TO_SAVE_KEY),
                    jfpga.getFlp().getString(LocalizationKeys.WARNING_KEY),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        SessionData sessionData = currentSession.getSessionData();
        JFileChooser jfc = new JFileChooser();
        Path destinationFilePath = sessionData.getFilePath();

        if (destinationFilePath == null || (e != null && e.getActionCommand() != null &&
                e.getActionCommand().equals(LocalizationKeys.SAVE_SESSION_AS_KEY))) {

            jfc.setDialogTitle(jfpga.getFlp().getString(LocalizationKeys.SAVE_SESSION_KEY));
            if (jfc.showSaveDialog(jfpga) != JFileChooser.APPROVE_OPTION) {
                JOptionPane.showMessageDialog(
                        jfpga,
                        jfpga.getFlp().getString(LocalizationKeys.NOTHING_WAS_SAVED_KEY),
                        jfpga.getFlp().getString(LocalizationKeys.WARNING_KEY),
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            destinationFilePath = jfc.getSelectedFile().toPath();
            if (Files.exists(destinationFilePath)) {
                int decision = JOptionPane.showConfirmDialog(
                        jfpga,
                        String.format("%s %s", jfpga.getFlp().getString(LocalizationKeys.FILE_ALREADY_EXISTS_KEY),
                                jfpga.getFlp().getString(LocalizationKeys.OVERWRITE_KEY)),
                        jfpga.getFlp().getString(LocalizationKeys.WARNING_KEY),
                        JOptionPane.YES_NO_OPTION);
                if (decision == JOptionPane.NO_OPTION) {
                    return;
                }
            }
        }

        try {
            SessionData.serializeToFile(sessionData, destinationFilePath.toString());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    jfpga,
                    jfpga.getFlp().getString(LocalizationKeys.FILE_COULD_NOT_BE_SAVED_KEY),
                    jfpga.getFlp().getString(LocalizationKeys.ERROR_KEY),
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(
                jfpga,
                jfpga.getFlp().getString(LocalizationKeys.FILE_SAVED_KEY),
                jfpga.getFlp().getString(LocalizationKeys.NOTIFICATION_KEY),
                JOptionPane.INFORMATION_MESSAGE);

        currentSession.setEdited(false);
    }
}
