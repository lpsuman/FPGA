package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.func;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.controllers.AbstractGUIController;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationKeys;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.local.LocalizationProvider;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Duplicateable;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Nameable;

import java.util.function.Supplier;

public class RemoveTableItemAction<T extends Duplicateable & Nameable> extends AbstractTableItemAction<T> {

    public RemoveTableItemAction(JFPGA jfpga, Supplier<AbstractGUIController<T>>  controllerProvider,
                                 String localizationKey, String targetLocalizationKey) {
        super(jfpga, controllerProvider, localizationKey, targetLocalizationKey);
    }

    @Override
    protected void doAction(AbstractGUIController<T> controller) {
        if (controller.getIndexSelectedItem() < 0) {
            LocalizationProvider locProv = jfpga.getFlp();
            jfpga.showWarningMsg(String.format("%s " + locProv.getString(LocalizationKeys.SELECT_S_FROM_THE_TABLE_MSG_KEY),
                    locProv.getString(LocalizationKeys.NOTHING_TO_REMOVE_MSG_KEY),
                    locProv.getString(targetLocalizationKey)));
            return;
        }

        for (int indexSelectedItem : controller.getIndicesSelectedItems()) {
            controller.removeItem(indexSelectedItem);
        }
    }
}