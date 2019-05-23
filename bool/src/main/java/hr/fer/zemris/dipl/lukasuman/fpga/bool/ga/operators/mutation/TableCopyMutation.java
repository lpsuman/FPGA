package hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.mutation;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.CLBController;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.IRNG;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;

public class TableCopyMutation extends AbstractBoolMutation {

    private static final double DEFAULT_OPERATOR_CHANCE = 0.5;
    public static final double DEFAULT_TABLE_COPY_MUTATION_CHANCE = Constants.OPERATOR_CHANCE_MULTIPLIER * 0.10;

    public TableCopyMutation(CLBController clbController, double mutationChance) {
        super(clbController, DEFAULT_OPERATOR_CHANCE, mutationChance);
    }

    public TableCopyMutation(CLBController clbController) {
        this(clbController, DEFAULT_TABLE_COPY_MUTATION_CHANCE);
    }

    @Override
    protected void mutateData(int[] data, IRNG random) {
        int firstOffset = clbController.calcLUTOffset(random.nextInt(0, clbController.getNumCLB()));
        int secondOffset = clbController.calcLUTOffset(random.nextInt(0, clbController.getNumCLB()));

        System.arraycopy(data, firstOffset, data, secondOffset, clbController.getIntsPerLUT());
    }
}
