package hr.fer.zemris.dipl.lukasuman.fpga.bool.model;

public interface CLBChangeListener {

    void numCLBInputsChanged(int prevNumCLBInputs, int newNumCLBInputs);
    void numCLBChanged(int prevNumCLB, int newNumCLB);
}
