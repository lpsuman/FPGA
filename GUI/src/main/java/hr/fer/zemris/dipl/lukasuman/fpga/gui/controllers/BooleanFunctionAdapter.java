package hr.fer.zemris.dipl.lukasuman.fpga.gui.controllers;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.gui.table.TableItemAdapter;

import java.util.BitSet;
import java.util.List;

public abstract class BooleanFunctionAdapter extends TableItemAdapter<BooleanFunction> implements BooleanFunctionListener {

    @Override
    public void booleanFunctionInputsEdited(BooleanFunction booleanFunction, int index, List<String> oldInputs) {
    }

    @Override
    public void booleanFunctionTableEdited(BooleanFunction booleanFunction, int index, BitSet oldTable) {
    }
}
