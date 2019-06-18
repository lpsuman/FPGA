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
        IRNG random = RNG.getRNG();

        if (mutationChance == 0.0) {
            mutateData(candidate.getData(), random);
        } else {
            for (int i = 0, n = calcNumMutations(random); i < n; i++) {
                mutateData(candidate.getData(), random);
            }
        }
    }

    protected abstract void mutateData(int[] data, IRNG random);

    private int calcNumMutations(IRNG random) {
        int maxNumMutations = (int)(mutationChance * clbController.getNumCLB());
        if (mutationChance == 0.0 || maxNumMutations <= 1) {
            return 1;
        }
        return Math.max(1, mirroredGaussian(random, 1, maxNumMutations));
    }

    protected int mirroredGaussian(IRNG random, double min, double max) {
        double scale = mutationChance * (max - min);
        double gaussian = random.nextGaussian(-scale, scale);
        return (int)(Math.abs(gaussian) + min);
    }

    @Override
    public double getMutationChance() {
        return mutationChance;
    }

    @Override
    public void setMutationChance(double mutationChance) {
        this.mutationChance = mutationChance;
    }

    public void setClbController(CLBController clbController) {
        this.clbController = clbController;
    }
}
