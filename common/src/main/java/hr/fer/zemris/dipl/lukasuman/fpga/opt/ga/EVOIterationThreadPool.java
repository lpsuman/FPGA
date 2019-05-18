package hr.fer.zemris.dipl.lukasuman.fpga.opt.ga;

import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.crossover.Crossover;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.mutation.Mutation;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.selection.Selection;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.selection.TournamentSelection;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.evaluator.Evaluator;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.listener.TerminationListener;
import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

public class EVOIterationThreadPool<T extends Solution> implements GAThreadPool<T>, TerminationListener {

    private final Thread[] threads;
    private final Function<Runnable, Thread> threadFactory;
    private final List<T> RED_PILL;
    private final Runnable runnable;

    private volatile boolean isRunning;
    private volatile boolean isShuttingDown;
    private volatile boolean isShutDown;
    private AtomicInteger remainingRedPills;

    private AtomicBoolean shouldTerminate;
    private AtomicInteger numRemainingTerminationAcknowledged;

    private final BlockingQueue<List<T>> incoming = new LinkedBlockingDeque<>();
    private final BlockingQueue<List<T>> calculated = new LinkedBlockingDeque<>();

    private final Selection<T> selection;
    private final Crossover<T> crossover;
    private final Mutation<T> mutation;
    private final ThreadLocal<Evaluator<T>> threadLocalEvaluator;

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

        this.selection = selection;
        this.crossover = crossover;
        this.mutation = mutation;
        this.threadLocalEvaluator = ThreadLocal.withInitial(evaluatorSupplier);

        runnable = () -> {
            threadLocalEvaluator.get().addTerminationListener(EVOIterationThreadPool.this);

            while (true) {
                List<T> population = takeFromQueue(incoming);

                if (population == RED_PILL) {
                    if (remainingRedPills.decrementAndGet() == 0) {
                        shutdownComplete();
                    }
                    break;
                }

                List<T> parents = pickParents(population);
                List<T> children = crossover.crossover(parents.get(0), parents.get(1));
                List<T> mutatedChildren = new ArrayList<>(children.size());

                for (T child : children) {
                    threadLocalEvaluator.get().evaluateSolution(child, false);
                    T mutated = mutation.mutate(child);
                    threadLocalEvaluator.get().evaluateSolution(mutated, true);

                    mutatedChildren.add(mutated);
                }

                putInQueue(calculated, mutatedChildren);

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
        calculated.clear();
        remainingRedPills.set(0);

        shouldTerminate.set(false);
        numRemainingTerminationAcknowledged.set(-1);

        System.out.println("Threadpool starting.");

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

        if (!calculated.isEmpty()) {
            System.out.println(String.format(
                    "Threadpool was shutdown with %d elements in calculated.", calculated.size()));
        }

        System.out.println("Threadpool shutdown is complete.");
        notifyAll();
    }

    private static <T> void putInQueue(BlockingQueue<T> queue, T element) {
        while (true) {
            try {
                queue.put(element);
                break;
            } catch (InterruptedException ignored) {
            }
        }
    }

    private static <T> T takeFromQueue(BlockingQueue<T> queue) {
        while (true) {
            try {
                return queue.take();
            } catch (InterruptedException ignored) {
            }
        }
    }

    private List<T> pickParents(List<T> population) {
        List<T> parents = new ArrayList<>();
        T firstParent = selection.selectFromPopulation(population);
        T secondParent = null;

        while (secondParent == null || secondParent == firstParent) {
            secondParent = selection.selectFromPopulation(population);
        }

        parents.add(firstParent);
        parents.add(secondParent);
        return parents;
    }

    @Override
    public synchronized boolean submitPopulation(List<T> population) {
        if (!isRunning || isShutDown || isShuttingDown) {
            return false;
        }
        putInQueue(incoming, population);
        return true;
    }

    @Override
    public List<T> takeChildren() {
        if (isShutDown && calculated.isEmpty()) {
            return null;
        } else {
            return takeFromQueue(calculated);
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
}
