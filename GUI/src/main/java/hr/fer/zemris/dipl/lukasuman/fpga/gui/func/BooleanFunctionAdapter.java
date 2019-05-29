package hr.fer.zemris.dipl.lukasuman.fpga.gui.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;

import java.util.BitSet;
import java.util.List;

public abstract class BooleanFunctionAdapter implements BooleanFunctionListener {

    @Override
    public void booleanFunctionAdded(BooleanFunction booleanFunction, int index) {
    }

    @Override
    public void booleanFunctionRemoved(BooleanFunction booleanFunction, int index) {
    }

    @Override
    public void booleanFunctionRenamed(BooleanFunction booleanFunction, int index, String oldName) {
    }

    @Override
    public void booleanFunctionInputsEdited(BooleanFunction booleanFunction, int index, List<String> oldInputs) {
    }

    @Override
    public void booleanFunctionTableEdited(BooleanFunction booleanFunction, int index, BitSet oldTable) {
    }
}
