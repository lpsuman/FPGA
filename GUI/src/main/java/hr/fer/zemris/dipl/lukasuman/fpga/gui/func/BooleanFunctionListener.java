package hr.fer.zemris.dipl.lukasuman.fpga.gui.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;

import java.util.BitSet;
import java.util.List;

public interface BooleanFunctionListener {

    void booleanFunctionAdded(BooleanFunction booleanFunction, int index);
    void booleanFunctionRemoved(BooleanFunction booleanFunction, int index);
    void booleanFunctionRenamed(BooleanFunction booleanFunction, int index, String oldName);
    void booleanFunctionInputsEdited(BooleanFunction booleanFunction, int index, List<String> oldInputs);
    void booleanFunctionTableEdited(BooleanFunction booleanFunction, int index, BitSet oldTable);
}
