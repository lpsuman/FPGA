package hr.fer.zemris.dipl.lukasuman.fpga.util;

/**
 * This interface requires a single reset method to be implemented. Classes implementing this interface should be
 * reusable meaning that reseting them is the same as making a new instance. In other words, all internal memory of
 * the instance should be reset to its default values.
 */
public interface Resetable {

    /**
     * Resets this object, making it reusable without the need of a new instance.
     */
    void reset();
}
