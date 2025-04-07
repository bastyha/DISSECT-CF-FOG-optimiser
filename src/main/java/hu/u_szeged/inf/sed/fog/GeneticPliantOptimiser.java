package hu.u_szeged.inf.sed.fog;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.jenetics.DoubleGene;
import io.jenetics.MeanAlterer;
import io.jenetics.Mutator;
import io.jenetics.TournamentSelector;
import io.jenetics.engine.*;
import io.jenetics.stat.DoubleMomentStatistics;
import io.jenetics.util.DoubleRange;
import io.jenetics.util.ISeq;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;


public class GeneticPliantOptimiser {
    static final Gson gson = new Gson();

    private static double normalizeCost(double price) {
        return (price - 15) / 10;
    }

    public static double fitness(SigmoidParams entity) {
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
                    gson.toJson(entity)
                    // Double.toString(entity.priceLambda()),
                    // Double.toString(entity.priceShift()),
                    // Double.toString(entity.loadOfResourceLambda()),
                    // Double.toString(entity.loadOfResourceShift()),
                    // Double.toString(entity.unprocessedDataLamdba()),
                    // Double.toString(entity.unprocessedDataShift())
            )
                    .directory(new File("src/main/resources"))
                    .start();

            // if i don't read the error stream, the application won't run
            process.getErrorStream().readAllBytes();
            // get output of program az string
            String text = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

            System.out.println(text);
            // get last line of output
            var split_text = text.split("\n");
            var object = split_text[split_text.length - 1];

            // convert last line of output to a *very* generic map
            TypeToken<Map<Object, Object>> mapType = new TypeToken<>() {
            };
            Map<Object, Object> objectObjectMap = new Gson().fromJson(object, mapType);

            // extract fitness from output
            totalCost = (double) ((Map<Object, Object>) objectObjectMap.get("cost")).get("totalCost");
            energy = (double) ((Map<Object, Object>) objectObjectMap.get("architecture")).get("totalEnergyConsumptionOfDevicesInWatt");
            simLength = (double) ((Map<Object, Object>) objectObjectMap.get("architecture")).get("simulationLength");

            // wait for the process to finish
            process.waitFor();

            System.out.printf("%s -> %.4f, %.4f, %.4f\n", entity.toString(), totalCost, energy, simLength);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return normalizeCost(totalCost) + normalizeEnergy(energy) + normalizeSimLength(simLength);
//        return simLength;

    }

    private static double normalizeSimLength(double simLength) {
        return (simLength - 670) / (800 - 670);
    }

    private static double normalizeEnergy(double energy) {
        return (energy - 0.2) / (0.4 - 0.2);
    }


    public static void evolution() {
        final Codec<SigmoidParams, DoubleGene> codec = Codec.combine(
                ISeq.of(
                        Codecs.ofScalar(DoubleRange.of(-5, 5)),
                        Codecs.ofScalar(DoubleRange.of(1, 50)),
                        Codecs.ofScalar(DoubleRange.of(-5, 5)),
                        Codecs.ofScalar(DoubleRange.of(10, 100)),
                        Codecs.ofScalar(DoubleRange.of(-5, 5)),
                        Codecs.ofScalar(DoubleRange.of(10, 100))
                ), objects -> new SigmoidParams(
                        (Math.pow(2, Math.floor((double) objects[0]))),
                        (double) objects[1],
                        -1 * (Math.pow(2, Math.floor((double) objects[2]))),
                        (double) objects[3],
                        -1 * (Math.pow(2, Math.floor((double) objects[4]))),
                        (double) objects[5]
                )
        );
        final Engine<DoubleGene, Double> engine = Engine.builder(GeneticPliantOptimiser::fitness, codec)
                .populationSize(5)
                .survivorsSelector(new TournamentSelector<>(4))
                .offspringSelector(new TournamentSelector<>(5))
                .alterers(
                        new Mutator<>(0.2),
                        new MeanAlterer<>(0.2)
                )
//                .executor(Executors.newSingleThreadExecutor()) // to execute all the fitness functions on the main thread
                .minimizing()
                .build();
        EvolutionStatistics<Double, DoubleMomentStatistics> statistics = EvolutionStatistics.ofNumber();
        var result = engine
                .stream()
                // .peek(statistics)
                // .peek(e -> System.out.println(statistics))
                .limit(10)
                .collect(EvolutionResult.toBestPhenotype());
        System.out.println("Res: " + result);
    }
}
