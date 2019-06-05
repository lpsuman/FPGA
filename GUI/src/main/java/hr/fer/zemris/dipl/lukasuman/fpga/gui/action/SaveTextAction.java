package hr.fer.zemris.dipl.lukasuman.fpga.gui.action;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.awt.event.ActionEvent;
import java.nio.file.Path;
import java.util.function.Supplier;

public class SaveTextAction extends AbstractAppAction {

    private Supplier<String> textSupplier;

    public SaveTextAction(JFPGA jfpga, String localizationKey, Supplier<String> textSupplier) {
        super(jfpga, localizationKey);
        this.textSupplier = Utility.checkNull(textSupplier, "text supplier");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Path filePath = askForSaveDestination(getLocalizationKey(), textFileFilter);

        if (filePath == null) {
            return;
        }

        if (!Utility.saveTextFile(filePath.toString(), textSupplier.get())) {
            warnCouldNotSave(filePath, LocalizationKeys.IO_EXCEPTION_OCCURRED_KEY);
        }
    }
}