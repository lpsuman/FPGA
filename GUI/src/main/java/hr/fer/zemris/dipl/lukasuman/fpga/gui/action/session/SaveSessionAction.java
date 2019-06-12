package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.session;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.MyGson;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.AbstractAppAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.session.SessionController;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.session.SessionData;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Path;

public class SaveSessionAction extends AbstractAppAction {

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
            warnNothingToSave(LocalizationKeys.THERE_IS_NO_SESSION_TO_SAVE_KEY);
            return;
        }

        SessionData sessionData = currentSession.getSessionData();
        Path destinationFilePath = sessionData.getFilePath();

        if (destinationFilePath == null || (e != null && e.getActionCommand() != null &&
                e.getActionCommand().equals(LocalizationKeys.SAVE_SESSION_AS_KEY))) {

            destinationFilePath = askForSaveDestination(LocalizationKeys.SAVE_SESSION_KEY, sessionFileFilter);
            if (destinationFilePath == null) {
                return;
            }

            sessionData.setFilePath(destinationFilePath);
        }

        try {
            MyGson.writeToJson(destinationFilePath.toString(), sessionData, SessionData.class);
        } catch (IOException exc) {
            exc.printStackTrace();
            warnCouldNotSave(destinationFilePath, LocalizationKeys.IO_EXCEPTION_OCCURRED_KEY);
            return;
        }

        notifyFileSaved(destinationFilePath);
        currentSession.setEdited(false);
    }
}
