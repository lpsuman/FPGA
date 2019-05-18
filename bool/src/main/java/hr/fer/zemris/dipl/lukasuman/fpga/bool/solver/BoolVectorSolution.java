package hr.fer.zemris.dipl.lukasuman.fpga.bool.solver;

import hr.fer.zemris.dipl.lukasuman.fpga.bool.AbstractNameHandler;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BlockConfiguration;
import hr.fer.zemris.dipl.lukasuman.fpga.bool.func.BoolVector;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.io.Serializable;

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
}
