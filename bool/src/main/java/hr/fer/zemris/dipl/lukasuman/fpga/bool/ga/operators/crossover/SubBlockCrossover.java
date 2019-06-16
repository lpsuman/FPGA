package hr.fer.zemris.dipl.lukasuman.fpga.bool.ga.operators.crossover;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.model.CLBController;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.IRNG;

import java.util.LinkedList;
import java.util.Queue;

public class SubBlockCrossover extends AbstractBoolCrossover {

    private static final double DEFAULT_OPERATOR_CHANCE = 2.0;

    private transient ThreadLocal<Queue<Integer>> blockQueue;

    public SubBlockCrossover(CLBController clbController, boolean isAligned) {
        super(clbController, isAligned ? DEFAULT_OPERATOR_CHANCE * ALIGNED_CHANCE_MODIFIER : DEFAULT_OPERATOR_CHANCE, isAligned);
        blockQueue = ThreadLocal.withInitial(LinkedList::new);
    }

    @Override
    protected void crossoverData(int[] firstData, int[] secondData, IRNG random) {
        blockQueue.get().add(random.nextInt(0, clbController.getNumCLB()));
        int numInputs = clbController.getNumInputs();

        while (!blockQueue.get().isEmpty()) {
            int indexCLB = blockQueue.get().poll();
            clbController.swapSingleCLB(firstData, secondData, indexCLB, indexCLB, true, true);

            int offset = clbController.calcCLBOffset(indexCLB);
            for (int i = 0; i < clbController.getNumCLBInputs(); i++) {
                int input = firstData[offset + i];
                if (input >= numInputs) {
                    blockQueue.get().add(input - numInputs);
                }
            }
        }
    }
}
