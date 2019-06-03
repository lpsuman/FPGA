package hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution;

import java.util.Arrays;

public class IntArraySolution extends AbstractSolution<int[]> {

    private static final long serialVersionUID = -6954770884747463440L;

    public IntArraySolution(int[] data) {
        super(data);
    }

    private IntArraySolution(int[] data, double fitness) {
        super(data, fitness);
    }

    @Override
    public Solution<int[]> duplicate() {
        return new IntArraySolution(this.data.clone(), this.fitness);
    }

    @Override
    public void copyOver(Solution<int[]> other) {
        System.arraycopy(data, 0, other.getData(), 0, data.length);
        other.setFitness(fitness);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(data);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        IntArraySolution that = (IntArraySolution) obj;
        return Arrays.equals(data, that.data);
    }
}
