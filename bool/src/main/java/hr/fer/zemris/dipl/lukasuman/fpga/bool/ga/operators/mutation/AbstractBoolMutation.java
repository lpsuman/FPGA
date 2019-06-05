package hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.mutation;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.CLBController;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.mutation.Mutation;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.operator.AbstractOperator;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.IRNG;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.RNG;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;

public abstract class AbstractBoolMutation extends AbstractOperator implements Mutation<int[]> {

    protected CLBController clbController;
    protected double mutationChance;

    public AbstractBoolMutation(CLBController clbController, double chance, double mutationChance) {
        super(chance);
        this.clbController = clbController;
        this.mutationChance = mutationChance;
    }

    @Override
    public void mutate(Solution<int[]> candidate) {
        for (int i = 0, n = calcNumMutations(); i < n; i++) {
            mutateData(candidate.getData(), RNG.getRNG());
        }
    }

    protected abstract void mutateData(int[] data, IRNG random);

    protected int calcNumMutations() {
        if (mutationChance == 0.0) {
            return 1;
        }
        return (int) Math.max(1, mutationChance * clbController.getNumCLB());
    }

    public void setClbController(CLBController clbController) {
        this.clbController = clbController;
    }
}
