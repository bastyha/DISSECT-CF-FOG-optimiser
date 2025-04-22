package hu.u_szeged.inf.sed.fog;

public class FitnessWrapper implements Comparable<FitnessWrapper> {
    Double totalCost = 0.0;
    Double energy = 0.0;
    Double simLength = 0.0;
    Double fitness = 0.0;

    public FitnessWrapper(Double totalCost, Double energy, Double simLength, Double fitness) {
        this.totalCost = totalCost;
        this.energy = energy;
        this.simLength = simLength;
        this.fitness = fitness;
    }


    public FitnessWrapper(Double totalCost, Double energy, Double simLength) {
        this(
                totalCost,
                energy,
                simLength,
                normalizeCost(totalCost) + normalizeSimLength(simLength) + normalizeEnergy(energy)
        );
    }

    @Override
    public int compareTo(FitnessWrapper o) {
        return this.fitness.compareTo(o.fitness);
    }

    private static double normalizeSimLength(double simLength) {
        return (simLength - 4_000.0) / (20_000.0 - 4_000.0);
    }

    private static double normalizeEnergy(double energy) {
        return (energy - 1.0) / (14.0 - 1.0);
    }

    private static double normalizeCost(double price) {
        return (price - 600.0) / (2800.0 - 600.0);
    }

    @Override
    public String toString() {
        return String.format("FitnessWrapper{\n" +
                        "\ttotalCost=%.05f,\n" +
                        "\tenergy=%.05f,\n" +
                        "\tsimLength=%.05f,\n" +
                        "\tfitness=%.05f\n" +
                        '}',
                totalCost,
                energy,
                simLength,
                fitness
        );
    }
}
