package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.session;

import com.google.gson.JsonParseException;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.MyGson;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.AbstractAppAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.session.SessionData;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Path;

public class OpenSessionAction extends AbstractAppAction {

    /**Accelerator key (shortcut).*/
    private static final String ACC_KEY = "control O";
    /**Mnemonic key.*/
    private static final int MNEMONIC_KEY = KeyEvent.VK_O;

    public OpenSessionAction(JFPGA jfpga) {
        super(jfpga, LocalizationKeys.OPEN_KEY);
        setValues(ACC_KEY, MNEMONIC_KEY);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Path[] filePaths = askForFilesToOpen(LocalizationKeys.OPEN_SESSION_KEY, sessionFileFilter);

        if (filePaths == null) {
            return;
        }

        for (int i = 0; i < filePaths.length; i++) {
            SessionData sessionData;
            Path filePath = filePaths[i];

            if (filePath == null) {
                continue;
            }

            try {
                sessionData = MyGson.readFromJson(filePath.toString(), SessionData.class);
                sessionData.setFilePath(filePath);
            } catch (IOException exc) {
                exc.printStackTrace();
                warnCouldNotOpen(filePath, LocalizationKeys.IO_EXCEPTION_OCCURRED_KEY);
                return;
            } catch (JsonParseException exc) {
                exc.printStackTrace();
                warnCouldNotOpen(filePath, String.format(LocalizationKeys.INVALID_DATA_FORMAT_KEY + "\n%s", exc.getMessage()));
                return;
            }

            jfpga.createNewSession(sessionData, jfpga.getBlueDiskette());
        }
    }
}
