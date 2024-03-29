package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.func;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.JFPGA;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.action.AbstractAppAction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.controllers.AbstractGUIController;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Duplicateable;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Nameable;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.awt.event.ActionEvent;
import java.util.function.Supplier;

public abstract class AbstractTableItemAction<T extends Duplicateable & Nameable> extends AbstractAppAction {

    private Supplier<AbstractGUIController<T>> controllerProvider;
    protected String targetLocalizationKey;

    public AbstractTableItemAction(JFPGA jfpga, Supplier<AbstractGUIController<T>> controllerProvider,
                                   String localizationKey, String targetLocalizationKey) {
        super(jfpga, localizationKey);
        this.controllerProvider = Utility.checkNull(controllerProvider, "controller provider");
        this.targetLocalizationKey = targetLocalizationKey;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        doAction(controllerProvider.get());
    }

    protected abstract void doAction(AbstractGUIController<T> controller);
}