package hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.operator;

import hr.fer.zemris.dipl.lukasuman.fpga.util.Resetable;

import java.util.List;

public interface OperatorRandomizer extends Resetable {

    List<OperatorStatistics> getLatestResults();
    List<OperatorStatistics> getCumulativeResults();
    List<OperatorStatistics> getGlobalResults();

    String resultsToString(List<OperatorStatistics> operatorStatistics);
}
