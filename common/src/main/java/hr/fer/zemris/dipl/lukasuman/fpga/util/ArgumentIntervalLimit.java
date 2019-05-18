package hr.fer.zemris.dipl.lukasuman.fpga.util;

public class ArgumentIntervalLimit<T extends Number> implements ArgumentLimit<T> {

    private String argName;
    private T lowerLimit;
    private T upperLimit;

    public ArgumentIntervalLimit(String argName, T lowerLimit, T upperLimit) {
        Utility.checkNull(argName, "argument name");
        if (lowerLimit != null && upperLimit != null && lowerLimit.doubleValue() > upperLimit.doubleValue()) {
            throw new IllegalArgumentException("Lower limit must not be greater than the upper limit.");
        }
        this.argName = Utility.capitalizedString(argName);
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
    }

    @Override
    public String testForLimit(T arg) {
        double argValue = arg.doubleValue();
        double minValue = lowerLimit != null ? lowerLimit.doubleValue() : -Double.MAX_VALUE;
        double maxValue = upperLimit != null ? upperLimit.doubleValue() : Double.MAX_VALUE;

        if (argValue < minValue) {
            return String.format(generateMsg(true), argValue, minValue);
        } else if (argValue > maxValue) {
            return String.format(generateMsg(false), argValue, maxValue);
        }

        return null;
    }

    private String generateMsg(boolean isMin) {
        String temp1 = isMin ? "small" : "big";
        String temp2 = isMin ? "minimum" : "maximum";
        return String.format("%s is too %s (%%.4f was given while %%.4f is %s).", argName, temp1, temp2);
    }

    @Override
    public T getLowerLimit() {
        return lowerLimit;
    }

    @Override
    public T getUpperLimit() {
        return upperLimit;
    }
}
