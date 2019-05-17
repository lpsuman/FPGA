package hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.mutation;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.CLBController;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.IRNG;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;

public class TableSingleMutation extends AbstractBoolMutation {

    private static final double DEFAULT_CHANCE = 2.0;

    public TableSingleMutation(CLBController clbController, double mutationChance) {
        super(clbController, DEFAULT_CHANCE, mutationChance);
    }

    public TableSingleMutation(CLBController clbController) {
        this(clbController, Constants.DEFAULT_TABLE_SINGLE_MUTATION_CHANCE);
    }

    @Override
    public void mutateData(int[] data, IRNG random) {
        int mutationIndex = random.nextInt(0, clbController.getNumCLB());
        int bitMutationIndex = random.nextInt(0, clbController.getBitsPerLUT());

        clbController.randomizeTableBit(data, mutationIndex, bitMutationIndex, random);
    }
}
