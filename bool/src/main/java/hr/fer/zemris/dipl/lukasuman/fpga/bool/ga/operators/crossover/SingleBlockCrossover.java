package hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.crossover;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.CLBController;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.IRNG;

public class SingleBlockCrossover extends AbstractBoolCrossover {

    private static final double DEFAULT_OPERATOR_CHANCE = 1.0;

    private boolean isAligned;

    public SingleBlockCrossover(CLBController clbController, boolean isAligned) {
        super(clbController, isAligned ? DEFAULT_OPERATOR_CHANCE * ALIGNED_CHANCE_MODIFIER : DEFAULT_OPERATOR_CHANCE);
        this.isAligned = isAligned;
    }

    public SingleBlockCrossover(CLBController clbController) {
        this(clbController, true);
    }

    @Override
    public void crossoverData(int[] first_data, int[] second_data, IRNG random) {
        int firstIndex = random.nextInt(0, clbController.getNumCLB());
        if (isAligned) {
            clbController.swapSingleCLB(first_data, second_data, firstIndex);
        } else {
            int secondIndex = random.nextInt(0, clbController.getNumCLB());
            clbController.swapSingleCLB(first_data, second_data, firstIndex, secondIndex);
        }
    }
}
