package hr.fer.zemris.dipl.lukasuman.fpga.gui.controllers;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;

import java.util.BitSet;
import java.util.List;

public abstract class BooleanFunctionAdapter implements BooleanFunctionListener {

    @Override
    public void itemAdded(BooleanFunction item, int indexInTable) {
    }

    @Override
    public void itemRemoved(BooleanFunction item, int indexInTable) {
    }

    @Override
    public void itemRenamed(BooleanFunction item, int indexInTable, String oldName) {
    }

    @Override
    public void itemListChanged() {
    }

    @Override
    public void booleanFunctionInputsEdited(BooleanFunction booleanFunction, int index, List<String> oldInputs) {
    }

    @Override
    public void booleanFunctionTableEdited(BooleanFunction booleanFunction, int index, BitSet oldTable) {
    }
}
