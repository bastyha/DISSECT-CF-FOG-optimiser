package hu.u_szeged.inf.sed.fog;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.jenetics.DoubleGene;
import io.jenetics.GaussianMutator;
import io.jenetics.LinearRankSelector;
import io.jenetics.MeanAlterer;
import io.jenetics.engine.*;
import io.jenetics.util.DoubleRange;
import io.jenetics.util.ISeq;
import io.jenetics.util.RandomRegistry;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.TimeUnit;


public class GeneticPliantOptimiser {
    static final Gson gson = new Gson();
    static final List<Object> SIMULATOR_CONFIG = List.of("LPDS_original.xml");
    static final Vector<FitnessWrapper> fitnessMean = new Vector<>();
    static final Vector<FitnessWrapper> fitnessBest = new Vector<>();
//    static final FitnessWrapper maxFitness = new FitnessWrapper(0.0, 0.0, 0.0);
//    static final FitnessWrapper minFitness = new FitnessWrapper(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);

    public static FitnessWrapper fitness(SigmoidParams entity) {
        double totalCost = Integer.MAX_VALUE;
        double energy = Integer.MAX_VALUE;
        double simLength = Integer.MAX_VALUE;
        FitnessWrapper res = null;
        try {

            Process process = new ProcessBuilder(
                    "java",
                    "-cp",
                    "dissect-cf-fog-1.0.0-SNAPSHOT-jar-with-dependencies.jar",
                    "hu.u_szeged.inf.fog.simulator.demo.IoTSimulation",
                    gson.toJson(SIMULATOR_CONFIG),
                    gson.toJson(entity)
            )
                    .directory(new File("src/main/resources"))
                    .start();

            // if i don't read the error stream, the application won't run
            process.getErrorStream().readAllBytes();
//            System.out.println(error);
            // wait for the process to finish
            process.waitFor();
            Thread.sleep(100);
            // get output of program az string
            String text = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

//            System.out.println(text);
            // get last line of output
            var split_text = text.split("\n");
            var object = split_text[split_text.length - 1];

            // convert last line of output to a *very* generic map
            TypeToken<Map<Object, Object>> mapType = new TypeToken<>() {
            };
            Map<Object, Object> objectObjectMap = null;
            try {
                objectObjectMap = gson.fromJson(object, mapType);
            } catch (Exception e) {
                System.err.println(e.getMessage());
                System.err.println("Error was caused by:");
                System.err.println(text);
            }

            // extract fitness from output
            totalCost = (double) ((Map<Object, Object>) objectObjectMap.get("cost")).get("totalCost");
            energy = (double) ((Map<Object, Object>) objectObjectMap.get("architecture")).get("totalEnergyConsumptionOfDevicesInWatt");
            simLength = (double) ((Map<Object, Object>) objectObjectMap.get("architecture")).get("simulationLength");

            res = new FitnessWrapper(totalCost, energy, simLength);
            System.out.println(entity);
            System.out.println(res);
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (res == null) {
            res = new FitnessWrapper(totalCost, energy, simLength);
        }
        return res;
    }

    public static void save_to_csv(Vector<FitnessWrapper> what, String filename) {
        try (FileWriter fw = new FileWriter("src/main/resources/evo_res/" + filename + ".csv")) {
            fw.write("totalCost; energy; simLength; fitness\n");
            for (FitnessWrapper fitnessWrapper : what)
                fw.append(String.format("%15.05f; %15.05f; %15.05f; %15.05f\n", fitnessWrapper.totalCost,
                        fitnessWrapper.energy,
                        fitnessWrapper.simLength,
                        fitnessWrapper.fitness
                ));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void statistics(
            final EvolutionResult<DoubleGene, FitnessWrapper> result
    ) {
        fitnessBest.add(result.bestPhenotype().fitness());
        FitnessWrapper mean = new FitnessWrapper(0.0, 0.0, 0.0, 0.0);
        for (var i : result.population()) {
            if (i.fitness().fitness < 20) {
                mean.totalCost += i.fitness().totalCost;
                mean.energy += i.fitness().energy;
                mean.simLength += i.fitness().simLength;
                mean.fitness += i.fitness().fitness;

//                minFitness.totalCost = Double.min(minFitness.totalCost, i.fitness().totalCost);
//                minFitness.energy = Double.min(minFitness.energy, i.fitness().energy);
//                minFitness.simLength = Double.min(minFitness.simLength, i.fitness().simLength);
//
//                maxFitness.totalCost = Double.max(maxFitness.totalCost, i.fitness().totalCost);
//                maxFitness.energy = Double.max(maxFitness.energy, i.fitness().energy);
//                maxFitness.simLength = Double.max(maxFitness.simLength, i.fitness().simLength);
            }
        }
        mean.totalCost /= result.population().size();
        mean.energy /= result.population().size();
        mean.simLength /= result.population().size();
        mean.fitness /= result.population().size();
        fitnessMean.add(mean);
    }

    public static void evolution() {
        final Codec<SigmoidParams, DoubleGene> codec = Codec.combine(
                ISeq.of(
                        Codecs.ofScalar(DoubleRange.of(-5, 5)), // priceLambda
                        Codecs.ofScalar(DoubleRange.of(0, 8)), // priceShift
                        Codecs.ofScalar(DoubleRange.of(-5, 5)), // loadOfResLambda
                        Codecs.ofScalar(DoubleRange.of(8, 200)), // loadOfResShift
                        Codecs.ofScalar(DoubleRange.of(-5, 5)), // unprocessedLambda
                        Codecs.ofScalar(DoubleRange.of(0, 27_000)) // unprocessedShift
                ), objects -> new SigmoidParams(
                        (Math.pow(2, Math.floor((double) objects[0]))),
                        (double) objects[1],
                        -1 * (Math.pow(2, Math.floor((double) objects[2]))),
                        (double) objects[3],
                        -1 * (Math.pow(2, Math.floor((double) objects[4]))),
                        (double) objects[5]
                )
        );
        final Engine<DoubleGene, FitnessWrapper> engine = Engine.builder(GeneticPliantOptimiser::fitness, codec)
                .populationSize(30)
//                .survivorsSelector(new EliteSelector<>(10))
//                .offspringSelector(new TournamentSelector<>(4))
                .selector(new LinearRankSelector<>())
                .alterers(
                        new GaussianMutator<>(0.2),
                        new MeanAlterer<>(0.2)
                )
                .executor(Runnable::run) // to execute all the fitness functions on the main thread
                .minimizing()
                .build();
//        EvolutionStatistics<FitnessWrapper, MinMax<FitnessWrapper>> statistics = EvolutionStatistics.ofComparable();
        long startTime = System.nanoTime();
        var result =
                RandomRegistry.with(new Random(4444816),
                        r ->
                                engine.stream()
                                        .limit(Limits.bySteadyFitness(15))
                                        .peek(GeneticPliantOptimiser::statistics)
                                        .limit(50)
                                        .collect(EvolutionResult.toBestPhenotype())
                );
        System.out.println("Result parameters: " + codec.decode(result.genotype()));
        System.out.println("Result fitness: " + result.fitness());
        long endTime = System.nanoTime();
        System.out.println("Duration: " + (endTime - startTime));
        System.out.println("Duration: " + TimeUnit.MINUTES.convert(endTime - startTime, TimeUnit.NANOSECONDS));
        System.out.println("Best fitnesses over generations: " + fitnessBest);
        System.out.println("Mean fitnesses over generations: " + fitnessMean);

        save_to_csv(fitnessBest, "linear_fitness_best");
        save_to_csv(fitnessMean, "linear_fitness_mean");

        try (FileWriter fw = new FileWriter("src/main/resources/evo_res/" + "linear_res" + ".csv")) {
            fw.write(codec.decode(result.genotype()).toString());
            fw.append("\n");
            fw.append(result.fitness().toString()).append("\n");
            fw.append("Duration (nanosec): ").append(String.valueOf(endTime - startTime)).append("\n");
            fw.append("Duration (min): " + TimeUnit.MINUTES.convert(endTime - startTime, TimeUnit.NANOSECONDS)).append("\n");

        } catch (Exception e) {
            e.printStackTrace();
        }

//        System.out.println("MaxFitnessThings: " + maxFitness);
//        System.out.println("MinFitnessThings: " + minFitness);
    }
}
