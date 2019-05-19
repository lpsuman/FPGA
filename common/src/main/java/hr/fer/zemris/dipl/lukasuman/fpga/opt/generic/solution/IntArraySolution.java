package hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution;

public class IntArraySolution extends AbstractSolution<int[]> {

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
}
