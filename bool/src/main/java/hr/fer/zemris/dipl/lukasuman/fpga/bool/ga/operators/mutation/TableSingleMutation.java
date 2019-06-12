package hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.mutation;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.CLBController;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.IRNG;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;

public class TableSingleMutation extends AbstractBoolMutation {

    private static final double DEFAULT_OPERATOR_CHANCE = 5.0;
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
        int bitMutationIndex = random.nextInt(0, clbController.getBitsPerLUT());

        clbController.randomizeTableBit(data, mutationIndex, bitMutationIndex, random);
    }
}
