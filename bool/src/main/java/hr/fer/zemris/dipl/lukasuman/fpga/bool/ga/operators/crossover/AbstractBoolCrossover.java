package hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.crossover;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.CLBController;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.crossover.Crossover;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.operator.AbstractOperator;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.IRNG;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.RNG;

public abstract class AbstractBoolCrossover extends AbstractOperator implements Crossover<int[]> {

    protected static final double ALIGNED_CHANCE_MODIFIER = 0.5;

    protected CLBController clbController;

    public AbstractBoolCrossover(CLBController clbController, double chance) {
        super(chance);
        this.clbController = clbController;
    }

    @Override
    public void crossover(Solution<int[]> firstParent, Solution<int[]> secondParent) {
        if (clbController.getNumCLB() >= 2) {
            crossoverData(firstParent.getData(), secondParent.getData(), RNG.getRNG());
        }
    }

    protected abstract void crossoverData(int[] firstData, int[] secondData, IRNG random);

    protected void crossoverInterval(int[] firstData, int[] secondData, int firstIndex, int secondIndex, int length,
                                     boolean switchInputs, boolean switchLUT) {

        for (int i = 0; i < length; ++i) {
            clbController.swapSingleCLB(firstData, secondData, firstIndex + i, secondIndex + i,
                    switchInputs, switchLUT);
        }
    }

    protected void crossoverInterval(int[] firstData, int[] secondData, int firstIndex, int secondIndex, int length) {
        crossoverInterval(firstData, secondData, firstIndex, secondIndex, length, true, true);
    }

    public void setClbController(CLBController clbController) {
        this.clbController = clbController;
    }
}
