package hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.mutation;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.CLBController;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.IRNG;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;

public class TableSingleMutation extends AbstractBoolMutation {

    private static final double DEFAULT_OPERATOR_CHANCE = 4.0;
    public static final double DEFAULT_TABLE_SINGLE_MUTATION_CHANCE = Constants.OPERATOR_CHANCE_MULTIPLIER * 0.10;

    public TableSingleMutation(CLBController clbController, double mutationChance) {
        super(clbController, DEFAULT_OPERATOR_CHANCE, mutationChance);
    }

    public TableSingleMutation(CLBController clbController) {
        this(clbController, DEFAULT_TABLE_SINGLE_MUTATION_CHANCE);
    }

    @Override
    public void mutateData(int[] data, IRNG random) {
        int mutationIndex = random.nextInt(0, clbController.getNumCLB());
        int bitsPerLUT = clbController.getBitsPerLUT();

        if (mutationChance == 0.0) {
            int bitMutationIndex = random.nextInt(0, bitsPerLUT);
            clbController.flipTableBit(data, mutationIndex, bitMutationIndex);
        } else {
            for (int i = 0, n = mirroredGaussian(random, 1, bitsPerLUT); i < n; i++) {
                int bitMutationIndex = random.nextInt(0, bitsPerLUT);
                clbController.flipTableBit(data, mutationIndex, bitMutationIndex);
            }
        }
    }
}
