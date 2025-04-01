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


public class GeneticPliantOptimiser {

    private  static  double normalizeCost(double price){
        return (price - 15) / 10;
    }

    public static double fitness(final Genotype<DoubleGene> entity){
        double totalCost = Integer.MAX_VALUE;
        double energy = Integer.MAX_VALUE;
        double simLength = Integer.MAX_VALUE;
        try {
            Process process = new ProcessBuilder(
                    "java",
                    "-cp",
                    "dissect-cf-fog-1.0.0-SNAPSHOT-jar-with-dependencies.jar",
                    "hu.u_szeged.inf.fog.simulator.demo.IoTSimulation",
                    "LPDS_original.xml",
                    Double.toString(entity.get(0).gene().allele()),
                    Double.toString(entity.get(1).gene().allele()),
//                    "null",
//                    "null",
                    Double.toString(entity.get(2).gene().allele()),
                    Double.toString(entity.get(3).gene().allele()),
                    Double.toString(entity.get(4).gene().allele()),
                    Double.toString(entity.get(5).gene().allele())
                    )
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
            TypeToken<Map<Object, Object>> mapType = new TypeToken<>(){};
            Map<Object, Object> objectObjectMap = new Gson().fromJson(object, mapType);

            // extract fitness from output
            totalCost = (double) ((Map<Object, Object>) objectObjectMap.get("cost")).get("totalCost");
            energy= (double) ((Map<Object, Object>) objectObjectMap.get("architecture")).get("totalEnergyConsumptionOfDevicesInWatt");
            simLength= (double) ((Map<Object, Object>) objectObjectMap.get("architecture")).get("simulationLength");

            // wait for the process to finish
            process.waitFor();

            System.out.printf("%s -> %.4f, %.4f, %.4f\n",   entity.toString() , totalCost,energy,simLength);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return normalizeCost(totalCost)+normalizeEnergy(energy)+normalizeSimLength(simLength);
//        return simLength;

    }

    private static double normalizeSimLength(double simLength) {
        return (simLength -670) / (800 - 670);
    }

    private static double normalizeEnergy(double energy) {
        return (energy - 0.2)/(0.4-0.2);
    }

    public static void evolution(){
        Factory<Genotype<DoubleGene>> factory = Genotype.of(
                DoubleChromosome.of(0, 30),
                DoubleChromosome.of(1, 50),
                DoubleChromosome.of(-20, -0.2),
                DoubleChromosome.of(10, 1000),
                DoubleChromosome.of(-20, -0.2),
                DoubleChromosome.of(10, 1000)
        );
        System.out.println(factory);


        final Engine<DoubleGene, Double> engine = Engine.builder(GeneticPliantOptimiser::fitness, factory)
                .populationSize(10)
                .survivorsSelector(new TournamentSelector<>(4))
                .offspringSelector(new TournamentSelector<>(5))
                .alterers(
                        new Mutator<>(0.2),
                        new MeanAlterer<>(0.2)
                )
//                .executor(Executors.newSingleThreadExecutor()) // to execute all the fitness functions on the main thread
                .minimizing()
                .build();
        var result = engine
                .stream()
                .limit(20)
                .collect(EvolutionResult.toBestPhenotype());
        System.out.println("Res: "+ result);
    }
}
