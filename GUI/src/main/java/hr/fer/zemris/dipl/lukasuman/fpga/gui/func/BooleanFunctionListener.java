package hr.fer.zemris.dipl.lukasuman.fpga.gui.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;

import java.util.BitSet;
import java.util.List;

public interface BooleanFunctionListener {

    void booleanFunctionAdded(BooleanFunction booleanFunction, int indexInList);
    void booleanFunctionRemoved(BooleanFunction booleanFunction, int indexInList);
    void booleanFunctionNameEdited(BooleanFunction booleanFunction, int indexInList, String oldName);
    void booleanFunctionInputsEdited(BooleanFunction booleanFunction, int indexInList, List<String> oldInputs);
    void booleanFunctionTableEdited(BooleanFunction booleanFunction, int indexInList, BitSet oldTable);
}
