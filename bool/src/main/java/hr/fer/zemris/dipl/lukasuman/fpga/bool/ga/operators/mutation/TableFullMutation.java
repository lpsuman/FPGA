package hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.mutation;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.CLBController;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.IRNG;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;

public class TableFullMutation extends AbstractBoolMutation {

    private static final double DEFAULT_OPERATOR_CHANCE = 0.25;
    public static final double DEFAULT_TABLE_FULL_MUTATION_CHANCE = Constants.OPERATOR_CHANCE_MULTIPLIER * 0.10;

    public TableFullMutation(CLBController clbController, double mutationChance) {
        super(clbController, DEFAULT_OPERATOR_CHANCE, mutationChance);
    }

    public TableFullMutation(CLBController clbController) {
        this(clbController, DEFAULT_TABLE_FULL_MUTATION_CHANCE);
    }

    @Override
    public void mutateData(int[] data, IRNG random) {
        int mutationIndex = random.nextInt(0, clbController.getNumCLB());
        clbController.randomizeTable(data, mutationIndex, random);
    }
}
