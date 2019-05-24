package hr.fer.zemris.dipl.lukasuman.fpga.gui.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;

import java.util.BitSet;
import java.util.List;

public abstract class BooleanFunctionAdapter implements BooleanFunctionListener {

    @Override
    public void booleanFunctionAdded(BooleanFunction booleanFunction, int indexInList) {
    }

    @Override
    public void booleanFunctionRemoved(BooleanFunction booleanFunction, int indexInList) {
    }

    @Override
    public void booleanFunctionNameEdited(BooleanFunction booleanFunction, int indexInList, String oldName) {
    }

    @Override
    public void booleanFunctionInputsEdited(BooleanFunction booleanFunction, int indexInList, List<String> oldInputs) {
    }

    @Override
    public void booleanFunctionTableEdited(BooleanFunction booleanFunction, int indexInList, BitSet oldTable) {
    }
}
