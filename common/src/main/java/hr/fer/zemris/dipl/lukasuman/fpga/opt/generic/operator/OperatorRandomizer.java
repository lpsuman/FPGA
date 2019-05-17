package hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.operator;

import java.util.List;

public interface OperatorRandomizer {

    List<OperatorStatistics> getLatestResults();
    List<OperatorStatistics> getCumulativeResults();
    String resultsToString(List<OperatorStatistics> operatorStatistics);
    void reset();
}
