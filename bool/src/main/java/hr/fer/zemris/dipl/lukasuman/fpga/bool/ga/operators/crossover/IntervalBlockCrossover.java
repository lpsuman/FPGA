package hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.crossover;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.CLBController;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.IRNG;

public class IntervalBlockCrossover extends AbstractBoolCrossover {

    private static final double DEFAULT_OPERATOR_CHANCE = 2.0;

    public IntervalBlockCrossover(CLBController clbController, boolean isAligned) {
        super(clbController, isAligned ? DEFAULT_OPERATOR_CHANCE * ALIGNED_CHANCE_MODIFIER : DEFAULT_OPERATOR_CHANCE, isAligned);
    }

    public IntervalBlockCrossover(CLBController clbController) {
        this(clbController, true);
    }

    @Override
    protected void crossoverData(int[] firstData, int[] secondData, IRNG random) {
        int numCLB = clbController.getNumCLB();
        int intervalSize = random.nextInt(1, numCLB);
        int firstIndex = random.nextInt(0, numCLB - intervalSize);

        if (isAligned) {
            crossoverInterval(firstData, secondData, firstIndex, firstIndex, intervalSize);
        } else {
            int secondIndex = random.nextInt(0, numCLB - intervalSize);
            crossoverInterval(firstData, secondData, firstIndex, secondIndex, intervalSize);
        }
    }
}
