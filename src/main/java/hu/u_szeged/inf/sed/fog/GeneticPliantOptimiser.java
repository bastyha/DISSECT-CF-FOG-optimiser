package hu.u_szeged.inf.sed.fog;
import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.util.Factory;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class GeneticPliantOptimiser {
    public static double fitness(final Genotype<DoubleGene> entity){
        double price = entity.get(0).gene().allele();
        System.out.println("nume: "+price);
        try {
            var res = FogExample.sim(price);
            var cost = res.getCost().getTotalCost();
            return cost;

        }catch (Exception e){
            e.printStackTrace();
        }
        return  1;
    }

    public static void evolution(){
        Factory<Genotype<DoubleGene>> factory = Genotype.of(
                DoubleChromosome.of(0, 30)
        );

        final Engine<DoubleGene, Double> engine = Engine.builder(GeneticPliantOptimiser::fitness, factory)
                .populationSize(3)
                .survivorsSelector(new TournamentSelector<>(4))
                .offspringSelector(new TournamentSelector<>(5))
                .alterers(
                        new Mutator<>(0.2),
                        new MeanAlterer<>(0.2)
                )
                .minimizing()
                .executor(Executors.newSingleThreadExecutor()) // to execute all the fitness functions on the main thread
                .build();
        var result = engine
                .stream()
                .limit(10)
                .collect(EvolutionResult.toBestPhenotype());
        System.out.println("Res: "+ result);
    }
}
