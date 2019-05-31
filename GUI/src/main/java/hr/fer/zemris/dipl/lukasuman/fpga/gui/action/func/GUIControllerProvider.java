package hr.fer.zemris.dipl.lukasuman.fpga.gui.action.func;

import hr.fer.zemris.dipl.lukasuman.fpga.gui.func.AbstractGUIController;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Nameable;

public interface GUIControllerProvider<T extends Nameable> {

    AbstractGUIController<T> getGUIController();
}
