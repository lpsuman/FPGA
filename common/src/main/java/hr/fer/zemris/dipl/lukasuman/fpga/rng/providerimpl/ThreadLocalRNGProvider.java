package hr.fer.zemris.dipl.lukasuman.fpga.rng.providerimpl;

import hr.fer.zemris.dipl.lukasuman.fpga.rng.IRNG;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.RNGProvider;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.rngimpl.RandomRNGImpl;

public class ThreadLocalRNGProvider implements RNGProvider {

    private ThreadLocal<IRNG> threadLocalRNG;

    public ThreadLocalRNGProvider() {
        this.threadLocalRNG = ThreadLocal.withInitial(RandomRNGImpl::new);
    }

    @Override
    public IRNG getRNG() {
        return threadLocalRNG.get();
    }
}
