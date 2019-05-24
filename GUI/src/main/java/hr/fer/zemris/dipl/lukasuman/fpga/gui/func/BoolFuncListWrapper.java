package hr.fer.zemris.dipl.lukasuman.fpga.gui.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanFunction;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

public class BoolFuncListWrapper extends BooleanFunctionAdapter {

    private BooleanFunction boolFunc;
    private int indexInList;

    public BoolFuncListWrapper(BooleanFunction boolFunc, int indexInList) {
        Utility.checkNull(boolFunc, "boolean function");
        this.boolFunc = boolFunc;
        this.indexInList = indexInList;
    }

    @Override
    public void booleanFunctionAdded(BooleanFunction booleanFunction, int indexInList) {
        if (indexInList <= this.indexInList) {
            this.indexInList++;
        }
    }

    @Override
    public void booleanFunctionRemoved(BooleanFunction booleanFunction, int indexInList) {
        if (indexInList < this.indexInList) {
            this.indexInList--;
        }
    }

    public BooleanFunction getBoolFunc() {
        return boolFunc;
    }

    @Override
    public String toString() {
        return String.format("%2d  %2d  %s", indexInList, boolFunc.getNumInputs(), boolFunc.getName());
    }
}
