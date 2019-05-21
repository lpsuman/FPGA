package hr.fer.zemris.dipl.lukasuman.fpga.bool.solver;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.AbstractNameHandler;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BlockConfiguration;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolVector;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.io.Serializable;
import java.util.List;

public class BoolVectorSolution extends AbstractNameHandler implements Serializable {

    private static final long serialVersionUID = 2274732589717742103L;

    private static final String DEFAULT_NAME = "VectorSolution";
    private static final String NAME_MSG = "vector solution's name";
    private static final String BLOCK_CONFIG_MSG = "block configuration";

    private BoolVector boolVector;
    private BlockConfiguration blockConfiguration;

    public BoolVectorSolution(String name, BoolVector boolVector, BlockConfiguration blockConfiguration) {
        super(name);
        this.boolVector = Utility.checkNull(boolVector, "boolean vector");
        this.blockConfiguration = Utility.checkNull(blockConfiguration, BLOCK_CONFIG_MSG);
    }

    public BoolVectorSolution(BoolVector boolVector, BlockConfiguration blockConfiguration) {
        this(DEFAULT_NAME, boolVector, blockConfiguration);
    }

    public BoolVector getBoolVector() {
        return boolVector;
    }

    public BlockConfiguration getBlockConfiguration() {
        return blockConfiguration;
    }

    public void setBlockConfiguration(BlockConfiguration blockConfiguration) {
        this.blockConfiguration = Utility.checkNull(blockConfiguration, BLOCK_CONFIG_MSG);
    }

    @Override
    protected String getNameMessage() {
        return NAME_MSG;
    }

    public boolean canBeConvertedToExpression() {
        return blockConfiguration.getNumCLBInputs() == 2;
    }

    public String getAsExpression(int indexBoolFunc) {
        if (!canBeConvertedToExpression()) {
            throw new UnsupportedOperationException(
                    "Only solutions with 2 CLB inputs can be converted to an expression.");
        }
        Utility.checkRange(indexBoolFunc, 0, boolVector.getBoolFunctions().size() - 1);

        int indexFuncOutputBlock = blockConfiguration.getOutputIndices().get((indexBoolFunc));
        String[] perBlockStrings = new String[boolVector.getNumInputs() + blockConfiguration.getNumCLB()];

        return recursiveBlockToString(indexFuncOutputBlock, perBlockStrings);
    }

    private String recursiveBlockToString(int indexCLB, String[] perBlockStrings) {
        int numInputs = boolVector.getNumInputs();
        if (indexCLB < numInputs) {
            return boolVector.getSortedInputIDs().get(indexCLB);
        }
        int[] data = blockConfiguration.getData();

        int inputA = data[(indexCLB - numInputs) * 3];
        String leftString = perBlockStrings[inputA];
        if (leftString == null) {
            leftString = recursiveBlockToString(inputA, perBlockStrings);
            perBlockStrings[inputA] = leftString;
        }

        int inputB = data[(indexCLB - numInputs) * 3 + 1];
        String rightString = perBlockStrings[inputB];
        if (rightString == null) {
            rightString = recursiveBlockToString(inputB, perBlockStrings);
            perBlockStrings[inputB] = rightString;
        }

        int truthTable = data[(indexCLB - numInputs) * 3 + 2];
        return FuncToExpressionConverter.getString(truthTable, leftString, rightString);
    }

    public static BoolVectorSolution mergeSolutions(List<BoolVectorSolution> solutions) {
        Utility.checkIfValidCollection(solutions, "list of solutions to merge");
        int numSolutions = solutions.size();

        //TODO do this ASAP

        return null;
    }
}
