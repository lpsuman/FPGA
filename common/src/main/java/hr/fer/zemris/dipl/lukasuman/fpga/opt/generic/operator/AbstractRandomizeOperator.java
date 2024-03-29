package hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.operator;

import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.listener.FitnessListener;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.listener.TerminationListener;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.IRNG;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.RNG;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractRandomizeOperator implements OperatorRandomizer, FitnessListener, TerminationListener, Operator {

    private static class WaitingSolutionData {
        private double prevFitness;
        private int indexOperator;

        public WaitingSolutionData(double prevFitness, int indexOperator) {
            this.prevFitness = prevFitness;
            this.indexOperator = indexOperator;
        }
    }

    protected boolean useStatistics;
    private transient List<OperatorStatistics> statsList;
    private List<OperatorStatistics> cumulativeStatsList;
    private List<OperatorStatistics> globalStatsList;

    private transient ThreadLocal<Map<Solution, WaitingSolutionData>> waitingSolutions;
    private double bestFitness;
    private boolean shouldIgnoreFitnessChanges;

    protected List<String> operatorNames;
    protected List<Double> operatorChances;

    //TODO modification of operator chances
    //TODO operator chances in constructor

    public AbstractRandomizeOperator(int numOperators, boolean useStatistics) {
        Utility.checkLimit(Constants.NUM_OPERATORS_LIMIT, numOperators);
        this.useStatistics = useStatistics;

        if (useStatistics) {
            statsList = new ArrayList<>(numOperators);
            cumulativeStatsList = new ArrayList<>(numOperators);
            globalStatsList = new ArrayList<>(numOperators);
            for (int i = 0; i < numOperators; i++) {
                statsList.add(new AtomicOperatorStatistics());
                cumulativeStatsList.add(new AtomicOperatorStatistics());
                globalStatsList.add(new AtomicOperatorStatistics());
            }
            waitingSolutions = ThreadLocal.withInitial(HashMap::new);
        }

        bestFitness = 0.0;
        operatorNames = new ArrayList<>(numOperators);
        operatorChances = null;
    }

    public AbstractRandomizeOperator(List<? extends Operator> operators, boolean useStatistics) {
        this(Utility.checkNull(operators, "operators").size(), useStatistics);

        if (operators.isEmpty()) {
            throw new IllegalArgumentException("List of operators must not be empty.");
        }

        calcOperatorChances(operators);
    }

    private void checkIfUsingStatistics() {
        if (!useStatistics) {
            throw new IllegalArgumentException("Operator randomizer is not setup to track operator statistics.");
        }
    }

    private void calcOperatorChances(List<? extends Operator> operators) {
        int numOperators = operators.size();
        operatorChances = new ArrayList<>(numOperators);
        double chanceSum = operators.stream().mapToDouble(Operator::getChance).sum();

        for (Operator operator : operators) {
            this.operatorChances.add(operator.getChance() / chanceSum);
        }
    }

    protected int calcRandomOperatorIndex() {
        IRNG random = RNG.getRNG();

        if (operatorChances == null) {
            return random.nextInt(0, statsList.size());
        } else {
            double chance = random.nextDouble(0.0, 1.0);

            for (int i = 0, n = operatorChances.size(); i < n; i++) {
                chance -= operatorChances.get(i);
                if (chance <= 0.0) {
                    return i;
                }
            }

            throw new IllegalStateException("Operator chances are invalid.");
        }
    }

    protected void addWaitingSolution(Solution solution, double prevFitness, int indexOperator) {
        checkIfUsingStatistics();
        waitingSolutions.get().put(solution, new WaitingSolutionData(prevFitness, indexOperator));
    }

    @Override
    public List<OperatorStatistics> getLatestResults() {
        checkIfUsingStatistics();
        return statsList;
    }

    @Override
    public List<OperatorStatistics> getCumulativeResults() {
        checkIfUsingStatistics();
        return cumulativeStatsList;
    }

    @Override
    public List<OperatorStatistics> getGlobalResults() {
        checkIfUsingStatistics();
        return globalStatsList;
    }

    @Override
    public String resultsToString(List<OperatorStatistics> operatorStatistics) {
        checkIfUsingStatistics();
        StringBuilder sb = new StringBuilder();
        sb.append('\n').append(getResultMessage()).append('\n');
        sb.append(String.format("%14s %24s %21s %19s\n", "Times used", "Decreased fitness", "Increased fitness", "Increased best"));

        OperatorStatistics totalStatistics = new AtomicOperatorStatistics();
        for (int i = 0, n = operatorStatistics.size(); i < n; i++) {
            sb.append(operatorNames.get(i)).append('\n');
            sb.append(operatorStatistics.get(i)).append('\n');
            totalStatistics.add(operatorStatistics.get(i));
        }
        sb.append("Total:").append('\n');
        sb.append(totalStatistics).append('\n');

        return sb.toString();
    }

    protected abstract String getResultMessage();

    @Override
    public void reset() {
        checkIfUsingStatistics();
        OperatorStatistics.resetStatistics(statsList);
        OperatorStatistics.resetStatistics(cumulativeStatsList);
    }

    @Override
    public void fitnessChanged(Solution solution) {
        checkIfUsingStatistics();
        WaitingSolutionData data = waitingSolutions.get().get(solution);
        if (data == null) {
            return;
        }
        waitingSolutions.get().remove(solution);

        if (!shouldIgnoreFitnessChanges) {
            statsList.get(data.indexOperator).incrementNumUsed(data.prevFitness, solution.getFitness(), bestFitness);
        }
    }

    @Override
    public void updateBestFitness(Solution solution) {
        checkIfUsingStatistics();
        bestFitness = solution.getFitness();
    }

    @Override
    public void terminate() {
        checkIfUsingStatistics();
        OperatorStatistics.sumStatistics(cumulativeStatsList, statsList);
        OperatorStatistics.sumStatistics(globalStatsList, statsList);
        OperatorStatistics.resetStatistics(statsList);
    }

    @Override
    public void setIgnoreFitnessChanges(boolean ignoreFitnessChanges) {
        checkIfUsingStatistics();
        shouldIgnoreFitnessChanges = ignoreFitnessChanges;
    }

    @Override
    public void setIgnoreTermination(boolean ignoreTermination) {
        // do nothing
    }

    private static List<Double> getOperatorChances(List<Operator> operators) {
        List<Double> result = new ArrayList<>(operators.size());
        operators.forEach(m -> result.add(m.getChance()));
        return result;
    }

    @Override
    public double getChance() {
        return 1.0;
    }

    @Override
    public void setChance(double chance) {
        // do  nothing
    }
}
