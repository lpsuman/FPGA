package hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.mutation;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.CLBController;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.IRNG;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;

public class InputSingleMutation extends AbstractBoolMutation {

    private static final double DEFAULT_OPERATOR_CHANCE = 2.0;
    public static final double DEFAULT_INPUT_SINGLE_MUTATION_CHANCE = Constants.OPERATOR_CHANCE_MULTIPLIER * 0.10;

    public InputSingleMutation(CLBController clbController, double mutationChance) {
        super(clbController, DEFAULT_OPERATOR_CHANCE, mutationChance);
    }

    public InputSingleMutation(CLBController clbController) {
        this(clbController, DEFAULT_INPUT_SINGLE_MUTATION_CHANCE);
    }

    @Override
    public void mutateData(int[] data, IRNG random) {
        int mutationIndex = random.nextInt(0, clbController.getNumCLB());
        int offset = clbController.calcCLBOffset(mutationIndex);
        int numCLBInputs = clbController.getNumCLBInputs();

        if (mutationChance == 0.0) {
            int inputIndex = random.nextInt(0, numCLBInputs);
            data[offset + inputIndex] = random.nextInt(0, mutationIndex + clbController.getNumInputs());
        } else {
            for (int i = 0, n = mirroredGaussian(random, 1, numCLBInputs); i < n; i++) {
                int inputIndex = random.nextInt(0, numCLBInputs);
                data[offset + inputIndex] = random.nextInt(0, mutationIndex + clbController.getNumInputs());
            }
        }
    }
}
