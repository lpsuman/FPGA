package hr.fer.zemris.dipl.lukasuman.fpga.gui.func;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BooleanVector;

public interface BooleanVectorListener {

    void booleanVectorAdded(BooleanVector booleanVector, int index);
    void booleanVectorRemoved(BooleanVector booleanVector, int index);
    void booleanVectorRenamed(BooleanVector booleanVector, int index);
}
