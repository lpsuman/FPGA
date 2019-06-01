package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.func;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.controllers.AbstractGUIController;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Duplicateable;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Nameable;

import java.util.function.Supplier;

public class DisplayAllTableItemAction<T extends Duplicateable & Nameable> extends AbstractTableItemAction<T> {

    public DisplayAllTableItemAction(JFPGA jfpga, Supplier<AbstractGUIController<T>> controllerProvider, String localizationKey) {
        super(jfpga, controllerProvider, localizationKey);
    }

    @Override
    protected void doAction(AbstractGUIController<T> controller) {
        controller.setItems(controller.getAllItems());
    }
}