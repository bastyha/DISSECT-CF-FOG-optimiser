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

    private static double normalizeCost(double price) {
        double priceMin = 2200.0;
        double priceMax = 4500.0;
        if (price > priceMax) {
            System.err.println("Price is bigger than what we normalize with");
        }
        if (price < priceMin) {
            System.err.println("Price is smaller than what we normalize with");
        }
        return (price - priceMin) / (priceMax - priceMin);
    }

    private static double normalizeEnergy(double energy) {
        double energyMin = 7.0;
        double energyMax = 8.0;
        if (energy > energyMax) {
            System.err.println("Energy is bigger than what we normalize with");
        }
        if (energy < energyMin) {
            System.err.println("Energy is smaller than what we normalize with");
        }
        return (energy - energyMin) / (energyMax - energyMin);
    }

    private static double normalizeSimLength(double simLength) {
        double simLengthMin = 12_000.0;
        double simLengthMax = 23_000.0;
        if (simLength > simLengthMax) {
            System.err.println("Simulation length is bigger than what we normalize with");
        }
        if (simLength < simLengthMin) {
            System.err.println("Simulation length is smaller than what we normalize with");
        }
        return (simLength - simLengthMin) / (simLengthMax - simLengthMin);
    }

    @Override
    public String toString() {
        return String.format("FitnessWrapper{" +
                        "totalCost=%.05f; " +
                        "energy=%.05f; " +
                        "simLength=%.05f; " +
                        "fitness=%.05f" +
                        '}',
                totalCost,
                energy,
                simLength,
                fitness
        );
    }
}
