package hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.crossover;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.CLBController;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.IRNG;

public class IntervalBlockCrossover extends AbstractBoolCrossover {

    private static final double DEFAULT_CHANCE = 1.0;

    private boolean isAligned;

    public IntervalBlockCrossover(CLBController clbController, boolean isAligned) {
        super(clbController, isAligned ? DEFAULT_CHANCE * ALIGNED_CHANCE_MODIFIER : DEFAULT_CHANCE);
        this.isAligned = isAligned;
    }

    public IntervalBlockCrossover(CLBController clbController) {
        this(clbController, true);
    }

    @Override
    protected void crossoverData(int[] firstData, int[] secondData, IRNG random) {
        int numCLB = clbController.getNumCLB();
        int intervalSize = random.nextInt(1, Math.max(2, numCLB / 2 + 1));
        int firstIndex = random.nextInt(0, numCLB - intervalSize);

        if (isAligned) {
            crossoverInterval(firstData, secondData, firstIndex, firstIndex, intervalSize);
        } else {
            int secondIndex = random.nextInt(0, numCLB - intervalSize);
            crossoverInterval(firstData, secondData, firstIndex, secondIndex, intervalSize);
        }
    }
}
