package hr.fer.zemris.dipl.lukasuman.fpga.gui.action;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class LoadTextAction extends AbstractAppAction {

    private Consumer<List<String>> textConsumer;

    public LoadTextAction(JFPGA jfpga, String localizationKey, Consumer<List<String>> textConsumer) {
        super(jfpga, localizationKey);
        this.textConsumer = Utility.checkNull(textConsumer, "text consumer");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Path[] filePaths = askForFilesToOpen(getLocalizationKey(), textFileFilter);

        if (filePaths == null) {
            return;
        }

        for (Path filePath : filePaths) {
            if (filePath == null) {
                continue;
            }

            List<String> lines;
            try {
                lines = Utility.readTextFileByLines(filePath.toString());
            } catch (IOException exc) {
                exc.printStackTrace();
                warnCouldNotOpen(filePath, LocalizationKeys.IO_EXCEPTION_OCCURRED_KEY);
                continue;
            }

            textConsumer.accept(lines);
        }
    }
}
