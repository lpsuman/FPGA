package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.func;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.GUIConstants;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.controllers.AbstractGUIController;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Duplicateable;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Nameable;
import java.util.function.Supplier;

public class DuplicateTableItemAction<T extends Duplicateable & Nameable> extends AbstractTableItemAction<T> {

    public DuplicateTableItemAction(JFPGA jfpga, Supplier<AbstractGUIController<T>> controllerProvider, String localizationKey) {
        super(jfpga, controllerProvider, localizationKey);
    }

    @Override
    protected void doAction(AbstractGUIController<T> controller) {
        if (controller.getIndexSelectedItem() < 0) {
            return;
        }

        T duplicate = (T) controller.getSelectedItem().getDuplicate();

        if (!duplicate.getName().endsWith(GUIConstants.DUPLICATE_NAME_SUFFIX)) {
            duplicate.setName(duplicate.getName() + GUIConstants.DUPLICATE_NAME_SUFFIX);
        }

        controller.addItem(duplicate);
    }
}
