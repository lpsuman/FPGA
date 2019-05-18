package hr.fer.zemris.dipl.lukasuman.fpga.util;

public interface ArgumentLimit<T> {

    T getLowerLimit();
    T getUpperLimit();
    String testForLimit(T arg);
}
