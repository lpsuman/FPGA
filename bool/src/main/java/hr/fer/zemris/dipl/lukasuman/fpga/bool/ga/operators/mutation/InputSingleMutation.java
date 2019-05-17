package hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.mutation;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.CLBController;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.IRNG;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;

public class InputSingleMutation extends AbstractBoolMutation {

    private static final double DEFAULT_CHANCE = 2.0;

    public InputSingleMutation(CLBController clbController, double mutationChance) {
        super(clbController, DEFAULT_CHANCE, mutationChance);
    }

    public InputSingleMutation(CLBController clbController) {
        this(clbController, Constants.DEFAULT_INPUT_SINGLE_MUTATION_CHANCE);
    }

    @Override
    public void mutateData(int[] data, IRNG random) {
        int mutationIndex = random.nextInt(0, clbController.getNumCLB());
        int offset = clbController.calcCLBOffset(mutationIndex);
        int inputIndex = random.nextInt(0, clbController.getNumCLBInputs());

        data[offset + inputIndex] = random.nextInt(0, mutationIndex + clbController.getNumInputs());
    }
}
