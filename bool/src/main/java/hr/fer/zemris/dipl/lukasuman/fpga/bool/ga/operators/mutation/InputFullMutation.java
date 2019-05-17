package hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.mutation;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.CLBController;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.IRNG;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;

public class InputFullMutation extends AbstractBoolMutation {

    private static final double DEFAULT_CHANCE = 1.0;

    public InputFullMutation(CLBController clbController, double mutationChance) {
        super(clbController, DEFAULT_CHANCE, mutationChance);
    }

    public InputFullMutation(CLBController clbController) {
        this(clbController, Constants.DEFAULT_INPUT_FULL_MUTATION_CHANCE);
    }

    @Override
    public void mutateData(int[] data, IRNG random) {
        int mutationIndex = random.nextInt(0, clbController.getNumCLB());
        int offset = clbController.calcCLBOffset(mutationIndex);

        for (int j = 0; j < clbController.getNumCLBInputs(); ++j) {
            data[offset + j] = random.nextInt(0, mutationIndex + clbController.getNumInputs());
        }
    }
}
