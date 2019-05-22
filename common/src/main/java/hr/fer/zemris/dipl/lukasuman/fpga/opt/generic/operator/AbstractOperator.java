package hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.operator;

public abstract class AbstractOperator implements Operator {

    private double chance;

    public AbstractOperator(double chance) {
        this.chance = chance;
    }

    @Override
    public double getChance() {
        return chance;
    }

    @Override
    public void setChance(double chance) {
        this.chance = chance;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
