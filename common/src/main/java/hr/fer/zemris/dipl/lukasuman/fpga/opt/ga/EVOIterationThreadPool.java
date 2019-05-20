package hr.fer.zemris.dipl.lukasuman.fpga.opt.ga;

import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.crossover.Crossover;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.mutation.Mutation;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.selection.Selection;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.selection.TournamentSelection;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.evaluator.Evaluator;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.listener.TerminationListener;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;
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

public class EVOIterationThreadPool<T> implements GAThreadPool<T>, TerminationListener {

    private final Object CALCULATION_SYNC_OBJECT = new Object();

    private final Thread[] threads;
    private final Function<Runnable, Thread> threadFactory;
    private final List<Solution<T>> RED_PILL;
    private final Runnable runnable;

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

    private List<Solution<T>> newPopulation;
    private AtomicInteger copyOverIndex;

    public EVOIterationThreadPool(int numThreads, Selection<T> selection, Crossover<T> crossover,
                                  Mutation<T> mutation, Supplier<Evaluator<T>> evaluatorSupplier,
                                  Function<Runnable, Thread> threadFactory) {

        this.threads = new Thread[numThreads];
        this.threadFactory = threadFactory;
        RED_PILL = new ArrayList<>();
        isRunning = false;
        isShuttingDown = false;
        isShutDown = true;
        remainingRedPills = new AtomicInteger();

        shouldTerminate = new AtomicBoolean();
        numRemainingTerminationAcknowledged = new AtomicInteger();
        numEvaluated = new AtomicInteger();

        this.selection = selection;
        this.crossover = crossover;
        this.mutation = mutation;
        this.threadLocalEvaluator = ThreadLocal.withInitial(evaluatorSupplier);

        copyOverIndex = new AtomicInteger();

        runnable = () -> {
            threadLocalEvaluator.get().addTerminationListener(EVOIterationThreadPool.this);

            while (true) {
                List<Solution<T>> population = takeFromQueue(incoming);

                if (population == RED_PILL) {
                    if (remainingRedPills.decrementAndGet() == 0) {
                        shutdownComplete();
                    }
                    break;
                }

                Solution<T> firstParent = selection.selectFromPopulation(population);
                Solution<T> firstChild = newPopulation.get(copyOverIndex.incrementAndGet());
                firstParent.copyOver(firstChild);

                Solution<T> secondParent = null;
                while (secondParent == null || secondParent == firstParent) {
                    secondParent = selection.selectFromPopulation(population);
                }
                Solution<T> secondChild = newPopulation.get(copyOverIndex.incrementAndGet());
                secondParent.copyOver(secondChild);

                crossover.crossover(firstChild, secondChild);

                threadLocalEvaluator.get().evaluateSolution(firstChild, false);
                mutation.mutate(firstChild);
                threadLocalEvaluator.get().evaluateSolution(firstChild, true);

                threadLocalEvaluator.get().evaluateSolution(secondChild, false);
                mutation.mutate(secondChild);
                threadLocalEvaluator.get().evaluateSolution(secondChild, true);

                if (numEvaluated.addAndGet(2) == newPopulation.size()) {
                    synchronized (CALCULATION_SYNC_OBJECT) {
                        CALCULATION_SYNC_OBJECT.notifyAll();
                    }
                }

                if (shouldTerminate.get()) {
                    synchronized (EVOIterationThreadPool.this) {
                        if (numRemainingTerminationAcknowledged.compareAndSet(-1, threads.length)) {
                            System.out.println("Terminate called.");
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
//                        System.out.println("Termination check complete.");
                    }
                }
            }
        };
    }

    public EVOIterationThreadPool(int numThreads, Selection<T> selection, Crossover<T> crossover,
                                  Mutation<T> mutation, Supplier<Evaluator<T>> evaluatorSupplier) {

        this(numThreads, selection, crossover, mutation, evaluatorSupplier, Thread::new);
    }

    public EVOIterationThreadPool(int numThreads, Crossover<T> crossover,
                                  Mutation<T> mutation, Supplier<Evaluator<T>> evaluatorSupplier) {

        this(numThreads, new TournamentSelection<>(), crossover, mutation, evaluatorSupplier);
    }

    public EVOIterationThreadPool(Crossover<T> crossover, Mutation<T> mutation,
                                  Supplier<Evaluator<T>> evaluatorSupplier) {

        this(Constants.DEFAULT_NUM_WORKERS, crossover, mutation, evaluatorSupplier);
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

        shouldTerminate.set(false);
        numRemainingTerminationAcknowledged.set(-1);

        System.out.println(String.format("Threadpool starting with %d threads.", threads.length));

        for (int i = 0; i < threads.length; ++i) {
            threads[i] = threadFactory.apply(runnable);
            threads[i].start();
        }
    }

    private synchronized void shutdownComplete() {
        isShuttingDown = false;
        isShutDown = true;
        isRunning = false;

        if (!incoming.isEmpty()) {
            throw new IllegalStateException(String.format(
                    "Threadpool was shutdown with %d elements in incoming.", incoming.size()));
        }

        System.out.println("Threadpool shutdown is complete.");
        notifyAll();

        synchronized (CALCULATION_SYNC_OBJECT) {
            CALCULATION_SYNC_OBJECT.notifyAll();
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
//        System.out.println("Submitting.");
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
//                    System.out.println("Waiting for calculation.");
                    CALCULATION_SYNC_OBJECT.wait();
//                    System.out.println("No longer wating for calculation");
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
        System.out.println("Threadpool is shutting down.");

        for (int i = 0; i < threads.length; ++i) {
            putInQueue(incoming, RED_PILL);
            remainingRedPills.incrementAndGet();
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
}
