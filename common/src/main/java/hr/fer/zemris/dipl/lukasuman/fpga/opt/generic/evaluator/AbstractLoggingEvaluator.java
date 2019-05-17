package hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.evaluator;

import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public abstract class AbstractLoggingEvaluator<T extends Solution> extends AbstractEvaluator<T> {

    protected boolean enableLogging;
    private StringBuilder stringBuilder;
    private List<String> checkpoints;

    protected AbstractLoggingEvaluator() {
        setLogging(false);
    }

    protected void log(Supplier<String> callback) {
        if (enableLogging) {
            stringBuilder.append(callback.get());
        }
    }

    @Override
    public void setLogging(boolean isEnabled) {
        enableLogging = isEnabled;
        if (isEnabled && stringBuilder == null) {
            stringBuilder = new StringBuilder();
        }
    }

    @Override
    public void log(String str) {
        if (enableLogging) {
            stringBuilder.append(str);
        }
    }

    @Override
    public void log(char c) {
        if (enableLogging) {
            stringBuilder.append(c);
        }
    }

    @Override
    public String getLog() {
        return stringBuilder.toString();
    }

    @Override
    public void resetLog() {
        stringBuilder.setLength(0);
    }

    @Override
    public void logCheckpoint() {
        if (checkpoints == null) {
            checkpoints = new ArrayList<>();
        }
        checkpoints.add(stringBuilder.toString());
    }

    @Override
    public List<String> getCheckpoints() {
        return checkpoints;
    }

    @Override
    public void resetCheckpoints() {
        if (checkpoints != null) {
            checkpoints.clear();
        }
    }
}
