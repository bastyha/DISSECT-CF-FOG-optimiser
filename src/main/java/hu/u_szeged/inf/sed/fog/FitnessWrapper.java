package hu.u_szeged.inf.sed.fog;

public class FitnessWrapper implements Comparable<FitnessWrapper> {
    Double totalCost = 0.0;
    Double energy = 0.0;
    Double simLength = 0.0;
    Double fitness = 0.0;

    public FitnessWrapper(Double totalCost, Double energy, Double simLength) {
        this.totalCost = totalCost;
        this.energy = energy;
        this.simLength = simLength;
        this.fitness = normalizeCost(totalCost) + normalizeSimLength(simLength) + normalizeEnergy(energy);
    }

    @Override
    public int compareTo(FitnessWrapper o) {
        return this.fitness.compareTo(o.fitness);
    }

    private static double normalizeSimLength(double simLength) {
        return (simLength - 670) / (800 - 670);
    }

    private static double normalizeEnergy(double energy) {
        return (energy - 0.2) / (0.4 - 0.2);
    }

    private static double normalizeCost(double price) {
        return (price - 15) / 10;
    }

    @Override
    public String toString() {
        return "FitnessWrapper{" +
                "totalCost=" + totalCost +
                ", energy=" + energy +
                ", simLength=" + simLength +
                ", fitness=" + fitness +
                '}';
    }
}
