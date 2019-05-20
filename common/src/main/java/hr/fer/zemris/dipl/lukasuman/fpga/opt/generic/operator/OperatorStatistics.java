package hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.operator;

import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.util.List;

public interface OperatorStatistics {

    void incrementNumUsed(double prevFitness, double newFitness, double bestFitness);
    int getNumUsed();
    int getNumDecreasedFitness();
    int getNumIncreasedFitness();
    int getNumIncreasedBestFitness();
    void add(OperatorStatistics other);
    void reset();

    static void sumStatistics(List<OperatorStatistics> target, List<OperatorStatistics> other) {
        Utility.checkNull(target, "target list of operator statistics");
        Utility.checkNull(other, "other list of operator statistics");

        if (target.size() != other.size()) {
            throw new IllegalArgumentException(String.format("Both lists of operators statistics must have the same size (%d and %d).", target.size(), other.size()));
        }

        for (int i = 0, n = target.size(); i < n; ++i) {
            target.get(i).add(other.get(i));
        }
    }

    static void resetStatistics(List<OperatorStatistics> statistics) {
        Utility.checkNull(statistics, "list of operator statistics");
        statistics.forEach(OperatorStatistics::reset);
    }
}
