package hr.fer.zemris.dipl.lukasuman.fpga.gui.controllers;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.table.TableItemListener;

public interface BooleanVectorListener extends TableItemListener<BooleanVector> {

    void booleanVectorFunctionsChanged();
}
