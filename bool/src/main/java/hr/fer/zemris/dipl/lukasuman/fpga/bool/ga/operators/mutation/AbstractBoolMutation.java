package hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.mutation;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.BoolVecSolution;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.CLBController;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.mutation.Mutation;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.operator.AbstractOperator;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.IRNG;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.RNG;

public abstract class AbstractBoolMutation extends AbstractOperator implements Mutation<BoolVecSolution> {

    protected CLBController clbController;
    protected double mutationChance;

    public AbstractBoolMutation(CLBController clbController, double chance, double mutationChance) {
        super(chance);
        this.clbController = clbController;
        this.mutationChance = mutationChance;
    }

    @Override
    public BoolVecSolution mutate(BoolVecSolution candidate) {
        for (int i = 0, n = calcNumMutations(); i < n; ++i) {
            mutateData(candidate.getData(), RNG.getRNG());
        }
        return candidate;
    }

    protected abstract void mutateData(int[] data, IRNG random);

    protected int calcNumMutations() {
        return (int) Math.ceil(mutationChance * clbController.getNumCLB());
    }
}
