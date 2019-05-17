package hr.fer.zemris.dipl.lukasuman.fpga.rng;

import java.util.BitSet;

public interface IRNG {

    int nextInt();
    int nextInt(int min, int max);

    float nextFloat();
    float nextFloat(float min, float max);

    double nextDouble();
    double nextDouble(double min, double max);

    boolean nextBoolean();

    BitSet nextBitSet(int size);
}
