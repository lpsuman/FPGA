package hr.fer.zemris.dipl.lukasuman.fpga.opt.ga;

import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.crossover.Crossover;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.mutation.Mutation;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.selection.Selection;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.selection.TournamentSelection;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.evaluator.Evaluator;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.listener.GenerationListener;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.IRNG;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.RNG;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

public class AnnealedThreadPool<T> implements GAThreadPool<T>, GenerationListener {

    private final Object CALCULATION_SYNC_OBJECT = new Object();

    private final Thread[] threads;
    private final Function<Runnable, Thread> threadFactory;
    private final List<Solution<T>> RED_PILL;
    private final Runnable runnable;
    private boolean enablePrinting;

    private volatile boolean isRunning;
    private volatile boolean isShuttingDown;
    private volatile boolean isShutDown;
    private AtomicInteger remainingRedPills;
    private AtomicBoolean shouldTerminate;
    private AtomicInteger numRemainingTerminationAcknowledged;
    private AtomicInteger numEvaluated;

    private final BlockingQueue<List<Solution<T>>> incoming = new LinkedBlockingDeque<>();

    private final Selection<T> selection;
    private final Crossover<T> crossover;
    private final Mutation<T> mutation;
    private final ThreadLocal<Evaluator<T>> threadLocalEvaluator;
    private double generationProgress;
    private double annealingThreshold;
    private boolean evaluateAfterCrossover;

    private List<Solution<T>> newPopulation;
    private AtomicInteger copyOverIndex;

    public AnnealedThreadPool(Crossover<T> crossover, Mutation<T> mutation, Supplier<Evaluator<T>> evaluatorSupplier,
                              double annealingThreshold, int numThreads, Selection<T> selection,
                              Function<Runnable, Thread> threadFactory, boolean evaluateAfterCrossover) {

        this.selection = selection;
        this.crossover = crossover;
        this.mutation = mutation;
        this.threadLocalEvaluator = ThreadLocal.withInitial(evaluatorSupplier);

        Utility.checkLimit(Constants.ANNEALING_THRESHOLD_LIMIT, annealingThreshold);
        this.annealingThreshold = annealingThreshold;
        this.evaluateAfterCrossover = evaluateAfterCrossover;

        shouldTerminate = new AtomicBoolean();
        numRemainingTerminationAcknowledged = new AtomicInteger();
        numEvaluated = new AtomicInteger();
        copyOverIndex = new AtomicInteger();

        enablePrinting = true;
        this.threads = new Thread[numThreads];
        this.threadFactory = threadFactory;
        RED_PILL = new ArrayList<>();
        isRunning = false;
        isShuttingDown = false;
        isShutDown = true;
        remainingRedPills = new AtomicInteger();

        runnable = () -> {
            threadLocalEvaluator.get().addTerminationListener(AnnealedThreadPool.this);
            IRNG random = RNG.getRNG();

            while (true) {
                List<Solution<T>> population = takeFromQueue(incoming);

                if (population == RED_PILL) {
                    if (remainingRedPills.decrementAndGet() == 0) {
                        shutdownComplete();
                    }
                    break;
                }

                Solution<T> firstParent = AnnealedThreadPool.this.selection.selectFromPopulation(population);
                Solution<T> firstChild = newPopulation.get(copyOverIndex.incrementAndGet());
                firstParent.copyOver(firstChild);

                Solution<T> secondParent = null;
                while (secondParent == null || secondParent == firstParent) {
                    secondParent = AnnealedThreadPool.this.selection.selectFromPopulation(population);
                }
                Solution<T> secondChild = newPopulation.get(copyOverIndex.incrementAndGet());
                secondParent.copyOver(secondChild);

                boolean acceptyOnlyImproving = true;
                if (generationProgress < AnnealedThreadPool.this.annealingThreshold
                        && random.nextDouble(0.0, AnnealedThreadPool.this.annealingThreshold) > generationProgress) {
                    acceptyOnlyImproving = false;
                }

                if (generationProgress < AnnealedThreadPool.this.annealingThreshold) {
                    AnnealedThreadPool.this.crossover.crossover(firstChild, secondChild);
                    if (acceptyOnlyImproving || AnnealedThreadPool.this.evaluateAfterCrossover) {
                        threadLocalEvaluator.get().evaluateSolution(firstChild, false);
                        threadLocalEvaluator.get().evaluateSolution(secondChild, false);
                    }
                }

                if (!acceptyOnlyImproving
                        || (firstChild.getFitness() < firstParent.getFitness())) {
                    AnnealedThreadPool.this.mutation.mutate(firstChild);
                    threadLocalEvaluator.get().evaluateSolution(firstChild, true);
                }

                if (!acceptyOnlyImproving
                        || (secondChild.getFitness() < secondParent.getFitness())) {
                    AnnealedThreadPool.this.mutation.mutate(secondChild);
                    threadLocalEvaluator.get().evaluateSolution(secondChild, true);
                }

                if (acceptyOnlyImproving) {
                    if (firstChild.getFitness() < firstParent.getFitness()) {
                        firstParent.copyOver(firstChild);
                    }
                    if (secondChild.getFitness() < secondParent.getFitness()) {
                        secondParent.copyOver(secondChild);
                    }
                }

                if (numEvaluated.addAndGet(2) == newPopulation.size()) {
                    synchronized (CALCULATION_SYNC_OBJECT) {
                        CALCULATION_SYNC_OBJECT.notifyAll();
                    }
                }

                if (shouldTerminate.get()) {
                    synchronized (AnnealedThreadPool.this) {
                        if (numRemainingTerminationAcknowledged.compareAndSet(-1, threads.length)) {
//                            if (enablePrinting) System.out.println("Terminate called.");
                        }

                        if (numRemainingTerminationAcknowledged.decrementAndGet() == 0) {
                            shouldTerminate.set(false);
                            shutdown();
                            notifyAll();
                        } else {
                            while (true) {
                                try {
                                    wait();
                                    break;
                                } catch (InterruptedException ignored) {
                                }
                            }
                        }
//                        if (enablePrinting) System.out.println("Termination check complete.");
                    }
                }
            }
        };
    }

    public AnnealedThreadPool(Crossover<T> crossover, Mutation<T> mutation, Supplier<Evaluator<T>> evaluatorSupplier,
                              double annealingThreshold, int numThreads, Selection<T> selection) {

        this(crossover, mutation, evaluatorSupplier, annealingThreshold, numThreads, selection, Thread::new, Constants.DEFAULT_USE_STATISTICS);
    }

    public AnnealedThreadPool(Crossover<T> crossover, Mutation<T> mutation, Supplier<Evaluator<T>> evaluatorSupplier,
                              double annealingThreshold, int numThreads) {

        this(crossover, mutation, evaluatorSupplier, annealingThreshold, numThreads, new TournamentSelection<>());
    }

    public AnnealedThreadPool(Crossover<T> crossover, Mutation<T> mutation, Supplier<Evaluator<T>> evaluatorSupplier,
                              double annealingThreshold) {

        this(crossover, mutation, evaluatorSupplier, annealingThreshold, Constants.DEFAULT_NUM_WORKERS);
    }

    public AnnealedThreadPool(Crossover<T> crossover, Mutation<T> mutation, Supplier<Evaluator<T>> evaluatorSupplier) {
        this(crossover, mutation, evaluatorSupplier, Constants.DEFAULT_ANNEALING_THRESHOLD);
    }

    public AnnealedThreadPool(Crossover<T> crossover, Mutation<T> mutation, Supplier<Evaluator<T>> evaluatorSupplier,
                              AnnealedThreadPoolConfig config) {
        this(crossover, mutation, evaluatorSupplier, config.getAnnealingThreshold(), config.getNumThreads());
        this.evaluateAfterCrossover = config.isEvaluateAfterCrossover();
    }

    @Override
    public synchronized void runThreads() {
        while (isRunning || !isShutDown || isShuttingDown) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        isRunning = true;
        isShutDown = false;
        isShuttingDown = false;
        incoming.clear();
        remainingRedPills.set(0);
        generationProgress(0.0);

        shouldTerminate.set(false);
        numRemainingTerminationAcknowledged.set(-1);

//        if (enablePrinting) System.out.println(String.format("Threadpool starting with %d threads.", threads.length));

        for (int i = 0; i < threads.length; i++) {
            threads[i] = threadFactory.apply(runnable);
            threads[i].setDaemon(true);
            threads[i].start();
        }
    }

    private static <T> void putInQueue(BlockingQueue<List<Solution<T>>> queue, List<Solution<T>> element) {
        while (true) {
            try {
                queue.put(element);
                return;
            } catch (InterruptedException ignored) {
            }
        }
    }

    private static <T> List<Solution<T>> takeFromQueue(BlockingQueue<List<Solution<T>>> queue) {
        while (true) {
            try {
                return queue.take();
            } catch (InterruptedException ignored) {
            }
        }
    }

    @Override
    public void setNewPopulation(List<Solution<T>> newPopulation, int currentSize) {
        this.newPopulation = Utility.checkNull(newPopulation, "new population");
        copyOverIndex.set(currentSize - 1);
        numEvaluated.set(currentSize);
    }

    @Override
    public synchronized boolean submitPopulation(List<Solution<T>> population) {
        if (!isRunning || isShutDown || isShuttingDown) {
            return false;
        }
//        if (enablePrinting) System.out.println("Submitting.");
        putInQueue(incoming, population);
        return true;
    }

    @Override
    public void waitForCalculation() {
        synchronized (CALCULATION_SYNC_OBJECT) {
            if (!isRunning || numEvaluated.get() == newPopulation.size()) {
                return;
            }
            while (true) {
                try {
//                    if (enablePrinting) System.out.println("Waiting for calculation.");
                    CALCULATION_SYNC_OBJECT.wait();
//                    if (enablePrinting) System.out.println("No longer wating for calculation");
                    return;
                } catch (InterruptedException ignored) {
                }
            }
        }
    }

    @Override
    public synchronized void shutdown() {
        if (!isRunning || isShutDown || isShuttingDown) {
            return;
        }

        isShuttingDown = true;
//        if (enablePrinting) System.out.println("Threadpool is shutting down.");

        for (int i = 0; i < threads.length; i++) {
            putInQueue(incoming, RED_PILL);
            remainingRedPills.incrementAndGet();
        }
    }

    private synchronized void shutdownComplete() {
        isShuttingDown = false;
        isShutDown = true;
        isRunning = false;

        if (!incoming.isEmpty()) {
            if (enablePrinting) System.err.println(String.format(
                    "Threadpool was shutdown with %d elements in incoming.", incoming.size()));
        }

//        if (enablePrinting) System.out.println("Threadpool shutdown is complete.");
        notifyAll();

        synchronized (CALCULATION_SYNC_OBJECT) {
            CALCULATION_SYNC_OBJECT.notifyAll();
        }
    }

    @Override
    public synchronized boolean isRunning() {
        return isRunning && (!isShutDown && !isShuttingDown);
    }

    @Override
    public void terminate() {
        if (!isShuttingDown) {
            shouldTerminate.set(true);
        }
    }

    @Override
    public void setIgnoreTermination(boolean ignoreTermination) {
        // do nothing
    }

    @Override
    public void generationProgress(double generationProgress) {
        this.generationProgress = generationProgress;
//        mutation.setMutationChance(1.0 - generationProgress);
    }

    @Override
    public void setEnablePrinting(boolean enablePrinting) {
        this.enablePrinting = enablePrinting;
    }
}
