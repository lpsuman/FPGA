package hr.fer.zemris.dipl.lukasuman.fpga.rng.rngimpl;

import hr.fer.zemris.dipl.lukasuman.fpga.rng.IRNG;

import java.util.BitSet;
import java.util.Random;

public class RandomRNGImpl implements IRNG {

    private Random random;

    public RandomRNGImpl() {
        this.random = new Random();
    }

    @Override
    public int nextInt() {
        return random.nextInt();
    }

    @Override
    public int nextInt(int min, int max) {
        return random.nextInt(max - min) + min;
    }

    @Override
    public float nextFloat() {
        return random.nextFloat();
    }

    @Override
    public float nextFloat(float min, float max) {
        return random.nextFloat() * (max - min) + min;
    }

    @Override
    public double nextDouble() {
        return random.nextDouble();
    }

    @Override
    public double nextDouble(double min, double max) {
        return random.nextDouble() * (max - min) + min;
    }

    @Override
    public boolean nextBoolean() {
        return random.nextBoolean();
    }

    @Override
    public BitSet nextBitSet(int size) {
        byte[] randomBytes = new byte[size / 8];
        random.nextBytes(randomBytes);
        return BitSet.valueOf(randomBytes);
    }
}