package hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.crossover;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.CLBController;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.crossover.Crossover;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.operator.AbstractOperator;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.IRNG;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.RNG;

public abstract class AbstractBoolCrossover extends AbstractOperator implements Crossover<int[]> {

    protected static final double ALIGNED_CHANCE_MODIFIER = 2.0;

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
            swapSingleCLB(firstData, secondData, firstIndex + i, secondIndex + i, switchInputs, switchLUT);
        }
    }

    protected void crossoverInterval(int[] firstData, int[] secondData, int firstIndex, int secondIndex, int length) {
        crossoverInterval(firstData, secondData, firstIndex, secondIndex, length, true, true);
    }

    protected static void swapSingleData(int[] firstData, int[] secondData, int firstIndex, int secondIndex) {
        int temp = firstData[firstIndex];
        firstData[firstIndex] = secondData[secondIndex];
        secondData[secondIndex] = temp;
    }

    protected static void swapSingleData(int[] data, int firstIndex, int secondIndex) {
        swapSingleData(data, data, firstIndex, secondIndex);
    }

    protected void swapSingleCLB(int[] firstData, int[] secondData, int firstIndex, int secondIndex,
                                 boolean switchInputs, boolean switchLUT) {

        int firstOffset = clbController.calcCLBOffset(firstIndex);
        int secondOffset = clbController.calcCLBOffset(secondIndex);
        int numCLBInputs = clbController.getNumCLBInputs();

        if (switchInputs) {
            for (int i = 0; i < numCLBInputs; ++i) {
                swapSingleData(firstData, secondData, firstOffset + i, secondOffset + i);

                if (firstIndex != secondIndex) {
                    if (firstIndex > secondIndex) {
                        clbController.checkInput(secondData, secondIndex, i);
                    } else {
                        clbController.checkInput(firstData, firstIndex, i);
                    }
                }
            }
        }

        firstOffset += numCLBInputs;
        secondOffset += numCLBInputs;

        if (switchLUT) {
            for (int i = 0, m = clbController.getIntsPerLUT(); i < m; ++i) {
                swapSingleData(firstData, secondData, firstOffset + i, secondOffset + i);
            }
        }
    }

    protected void swapSingleCLB(int[] firstData, int[] secondData, int firstIndex, int secondIndex) {
        swapSingleCLB(firstData, secondData, firstIndex, secondIndex, true, true);
    }

    protected void swapSingleCLB(int[] firstData, int[] secondData, int index,
                                 boolean switchInputs, boolean switchLUT) {

        swapSingleCLB(firstData, secondData, index, index, switchInputs, switchLUT);
    }

    protected void swapSingleCLB(int[] firstData, int[] secondData, int index) {
        swapSingleCLB(firstData, secondData, index, true, true);
    }
}
