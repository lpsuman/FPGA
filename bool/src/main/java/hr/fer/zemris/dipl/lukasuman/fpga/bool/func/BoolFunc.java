package hr.fer.zemris.dipl.lukasuman.fpga.bool.func;

import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.util.BitSet;
import java.util.List;

public class BoolFunc {

    private List<String> inputIDs;
    private BitSet truthTable;

    public BoolFunc(List<String> inputIDs, BitSet truthTable) {
        this.inputIDs = Utility.checkNull(inputIDs, "input IDs");
        Utility.checkLimit(Constants.NUM_FUNCTION_INPUTS_LIMIT, inputIDs.size());
        inputIDs.forEach(id -> Utility.checkNull(id, "input ID for function"));
        this.truthTable = Utility.checkNull(truthTable, "truth table");

        if (truthTable.length() > (1 << inputIDs.size())) {
            throw new IllegalArgumentException(String.format(
                    "Not enough inputs (%d) for the given truth table (of min size %d).",
                    inputIDs.size(), truthTable.length()));
        }
    }

    public BoolFunc(int numInputs, BitSet truthTable) {
        this(BoolFuncController.generateDefaultInputIDs(numInputs), truthTable);
    }

    public List<String> getInputIDs() {
        return inputIDs;
    }

    public BitSet getTruthTable() {
        return truthTable;
    }
}
