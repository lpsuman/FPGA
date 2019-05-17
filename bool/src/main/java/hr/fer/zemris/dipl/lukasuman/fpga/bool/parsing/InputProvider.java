package hr.fer.zemris.dipl.lukasuman.fpga.bool.parsing;

public interface InputProvider {

    int getIndexForID(String inputID);
    boolean getInput(int inputIndex);
}
