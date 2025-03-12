package hu.u_szeged.inf.sed.fog;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.util.Factory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class GeneticPliantOptimiser {
    public static double fitness(final Genotype<DoubleGene> entity){
        double fitness = 0.0;
        double param = entity.gene().allele();
        try {
            Process process = new ProcessBuilder(
                    "java",
                    "-cp",
                    "dissect-cf-fog-1.0.0-SNAPSHOT-jar-with-dependencies.jar",
                    "hu.u_szeged.inf.fog.simulator.demo.IoTSimulation",
                    "LPDS_original.xml",
                    Double.toString(param))
                    .directory(new File("src/main/resources"))
                    .start();

            // if i don't read the error stream, the application won't run
            process.getErrorStream().readAllBytes();
            // get output of program az string
            String text = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            // get last line of output
            var split_text = text.split("\n");
            var object = split_text[split_text.length-1];

            // convert last line of output to a *very* generic map
            // TODO: create a class which the output HAS to match
            TypeToken<Map<Object, Object>> mapType = new TypeToken<>(){};
            Map<Object, Object> objectObjectMap = new Gson().fromJson(object, mapType);

            // extract fitness from output
            fitness = (double) ((Map<Object, Object>) objectObjectMap.get("cost")).get("totalCost");

            // wait for the process to finish
            process.waitFor();

            System.out.printf("%f -> %f%n", param, fitness);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return fitness;

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
//                .executor(Executors.newSingleThreadExecutor()) // to execute all the fitness functions on the main thread
                .build();
        var result = engine
                .stream()
                .limit(10)
                .collect(EvolutionResult.toBestPhenotype());
        System.out.println("Res: "+ result);
    }
}
