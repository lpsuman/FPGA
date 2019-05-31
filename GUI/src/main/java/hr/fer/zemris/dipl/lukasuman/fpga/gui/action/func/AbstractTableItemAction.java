package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.func;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.AbstractAppAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.func.AbstractGUIController;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Duplicateable;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Nameable;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.awt.event.ActionEvent;
import java.util.function.Supplier;

public abstract class AbstractTableItemAction<T extends Duplicateable & Nameable> extends AbstractAppAction {

    private Supplier<AbstractGUIController<T>> controllerProvider;

    public AbstractTableItemAction(JFPGA jfpga, Supplier<AbstractGUIController<T>> controllerProvider, String localizationKey) {
        super(jfpga, localizationKey);
        this.controllerProvider = Utility.checkNull(controllerProvider, "controller provider");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        AbstractGUIController<T> controller = controllerProvider.get();
        int indexSelectedItem = controller.getIndexSelectedItem();

        if (indexSelectedItem < 0) {
            return;
        }

        doAction(controller);
    }

    protected abstract void doAction(AbstractGUIController<T> controller);
}