package hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.operator;

public interface OperatorStatistics {

    void incrementNumUsed(double prevFitness, double newFitness, double bestFitness);
    int getNumUsed();
    int getNumDecreasedFitness();
    int getNumIncreasedFitness();
    int getNumIncreasedBestFitness();
    void add(OperatorStatistics other);
    void reset();
}
