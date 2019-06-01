package hr.fer.zemris.dipl.lukasuman.fpga.gui.controllers;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.table.TableItemListener;

import java.util.BitSet;
import java.util.List;

public interface BooleanFunctionListener extends TableItemListener<BooleanFunction> {

    void booleanFunctionInputsEdited(BooleanFunction booleanFunction, int index, List<String> oldInputs);
    void booleanFunctionTableEdited(BooleanFunction booleanFunction, int index, BitSet oldTable);
}
