package hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.operator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AtomicOperatorStatistics implements OperatorStatistics {

    private List<AtomicInteger> stats;

    public AtomicOperatorStatistics() {
        stats = new ArrayList<>();
        for (int i = 0; i < 4; ++i) {
            stats.add(new AtomicInteger());
        }
    }

    @Override
    public void incrementNumUsed(double prevFitness, double newFitness, double bestFitness) {
        stats.get(0).incrementAndGet();
        double deltaFitness = newFitness - prevFitness;

        if (deltaFitness < 0.0) {
            stats.get(1).incrementAndGet();
        } else if (deltaFitness > 0.0){
            stats.get(2).incrementAndGet();
        }

        if (newFitness > bestFitness) {
            stats.get(3).incrementAndGet();
        }
    }

    @Override
    public int getNumUsed() {
        return stats.get(0).get();
    }

    @Override
    public int getNumDecreasedFitness() {
        return stats.get(1).get();
    }

    @Override
    public int getNumIncreasedFitness() {
        return stats.get(2).get();
    }

    @Override
    public int getNumIncreasedBestFitness() {
        return stats.get(3).get();
    }

    @Override
    public void add(OperatorStatistics other) {
        stats.get(0).addAndGet(other.getNumUsed());
        stats.get(1).addAndGet(other.getNumDecreasedFitness());
        stats.get(2).addAndGet(other.getNumIncreasedFitness());
        stats.get(3).addAndGet(other.getNumIncreasedBestFitness());
    }

    @Override
    public void reset() {
        for (int i = 0; i < 4; ++i) {
            stats.get(i).set(0);
        }
    }

    @Override
    public String toString() {
        return String.format("%10d %10d (%.2f) %10d (%.2f) %6d (%.5f)",
                getNumUsed(),
                getNumDecreasedFitness(), 100.0 * getNumDecreasedFitness() / getNumUsed(),
                getNumIncreasedFitness(), 100.0 * getNumIncreasedFitness() / getNumUsed(),
                getNumIncreasedBestFitness(), 100.0 * getNumIncreasedBestFitness() / getNumUsed());
    }
}
