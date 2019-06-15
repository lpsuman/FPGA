package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.func;

import com.google.gson.JsonParseException;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.MyGson;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.controllers.AbstractGUIController;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Duplicateable;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Nameable;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

public class LoadTableItemAction<T extends Duplicateable & Nameable> extends AbstractTableItemAction<T> {

    private Class<T[]> arrayClass;

    public LoadTableItemAction(JFPGA jfpga, Supplier<AbstractGUIController<T>> controllerProvider,
                               String localizationKey, String targetLocalizationKey, Class<T[]> arrayClass) {
        super(jfpga, controllerProvider, localizationKey, targetLocalizationKey);
        this.arrayClass = Utility.checkNull(arrayClass, "array class");
    }

    @Override
    protected void doAction(AbstractGUIController<T> controller) {
        Path[] filePaths = askForFilesToOpen(localizationHandler.getKey(), JSON_FILE_FILTER);

        if (filePaths == null) {
            return;
        }

        for (int i = 0; i < filePaths.length; i++) {
            Path filePath = filePaths[i];
            if (filePath == null) {
                continue;
            }

            List<T> tableItems;

            try {
                tableItems = MyGson.readFromJsonAsList(filePath.toString(), arrayClass);
            } catch (IOException exc) {
                exc.printStackTrace();
                warnCouldNotOpen(filePath, LocalizationKeys.IO_EXCEPTION_OCCURRED_KEY);
                return;
            } catch (JsonParseException exc) {
                exc.printStackTrace();
                warnCouldNotOpen(filePath, String.format(LocalizationKeys.INVALID_DATA_FORMAT_KEY + "\n%s", exc.getMessage()));
                return;
            }

            tableItems.forEach(controller::addItem);
        }
    }
}
