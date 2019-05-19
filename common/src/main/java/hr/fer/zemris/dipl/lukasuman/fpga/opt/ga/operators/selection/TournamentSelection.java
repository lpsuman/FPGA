package hr.fer.zemris.dipl.lukasuman.fpga.opt.ga.operators.selection;

import hr.fer.zemris.dipl.lukasuman.fpga.opt.generic.solution.Solution;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.IRNG;
import hr.fer.zemris.dipl.lukasuman.fpga.rng.RNG;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Constants;
import hr.fer.zemris.dipl.lukasuman.fpga.util.Utility;

import java.util.List;

public class TournamentSelection<T> implements Selection<T> {

    private final int tournamentSize;

    public TournamentSelection(int tournamentSize) {
        Utility.checkLimit(Constants.TOURNAMENT_SIZE_LIMIT, tournamentSize);
        this.tournamentSize = tournamentSize;
    }

    public TournamentSelection() {
        this(Constants.DEFAULT_TOURNAMENT_SIZE);
    }

    @Override
    public Solution<T> selectFromPopulation(List<Solution<T>> population) {
        IRNG random = RNG.getRNG();

        Solution<T> candidate = pickRandom(population, random);

        for (int i = 1; i < tournamentSize; ++i) {
            Solution<T> competitor = pickRandom(population, random);

            if (competitor.getFitness() > candidate.getFitness()) {
                candidate = competitor;
            }
        }

        return candidate;
    }

    private Solution<T> pickRandom(List<Solution<T>> population, IRNG random) {
        return population.get(random.nextInt(0, population.size()));
    }
}
