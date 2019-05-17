package hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.mutation;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.CLBController;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.IRNG;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;

public class TableCopyMutation extends AbstractBoolMutation {

    private static final double DEFAULT_CHANCE = 1.0;

    public TableCopyMutation(CLBController clbController, double mutationChance) {
        super(clbController, DEFAULT_CHANCE, mutationChance);
    }

    public TableCopyMutation(CLBController clbController) {
        this(clbController, Constants.DEFAULT_TABLE_COPY_MUTATION_CHANCE);
    }

    @Override
    protected void mutateData(int[] data, IRNG random) {
        int firstOffset = clbController.calcLUTOffset(random.nextInt(0, clbController.getNumCLB()));
        int secondOffset = clbController.calcLUTOffset(random.nextInt(0, clbController.getNumCLB()));

        for (int i = 0, n = clbController.getIntsPerLUT(); i < n; ++i) {
            data[firstOffset + i] = data[secondOffset + i];
        }
    }
}
