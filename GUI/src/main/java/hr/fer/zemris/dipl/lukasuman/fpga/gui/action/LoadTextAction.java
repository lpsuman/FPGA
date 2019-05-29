package hr.fer.zemris.dipl.lukasuman.fpga.gui.action;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.listener.SetListener;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.util.List;

public class LoadTextAction extends AbstractAppAction {

    private SetListener<TextLoadListener> listeners;

    public LoadTextAction(JFPGA jfpga, String localizationKey) {
        super(jfpga, localizationKey);
        listeners = new SetListener<>();
    }

    public void addTextLoadListener(TextLoadListener listener) {
        listeners.addListener(listener);
    }

    public void removeTextLoadListener(TextLoadListener listener) {
        listeners.removeListener(listener);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Path[] filePaths = askForFilesToOpen(LocalizationKeys.LOAD_EXPRESSION_KEY, textFileFilter);

        if (filePaths == null) {
            return;
        }

        for (Path filePath : filePaths) {
            if (filePath == null) {
                continue;
            }

            List<String> lines = Utility.readTextFile(filePath.toString());
            if (lines == null) {
                warnCouldNotOpen(filePath, LocalizationKeys.IO_EXCEPTION_OCCURRED_KEY);
                continue;
            }

            listeners.getListeners().forEach(l -> l.textLoaded(lines));
        }
    }
}
