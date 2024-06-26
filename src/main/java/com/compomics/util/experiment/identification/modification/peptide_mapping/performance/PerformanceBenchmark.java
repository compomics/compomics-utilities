package com.compomics.util.experiment.identification.modification.peptide_mapping.performance;

import com.compomics.util.io.flat.SimpleFileWriter;
import com.compomics.util.waiting.Duration;
import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Performance benchmark for the ModificationPeptideMapping class.
 *
 * @author Marc Vaudel
 */
public class PerformanceBenchmark {

    /**
     * The number of times each analysis needs to be replicated.
     */
    public final static int REPLICATES = 10;

    /**
     * This main method runs different performance benchmarks on the
     * ModificationPeptideMapping class and exports the results.
     *
     * @param args The file where to write the results.
     */
    public static void main(String[] args) {

        PerformanceBenchmark performanceBenchmark = new PerformanceBenchmark();

        String fileStem = args.length > 0 ? args[0] : "/home/marc/Github/papers/peptides-modifications-matching/benchmark/benchmark_23.02.02";

        try {

            performanceBenchmark.run(fileStem);

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    /**
     * Constructor.
     */
    public PerformanceBenchmark() {

    }

    /**
     * Runs the benchmark.
     *
     * @param fileStem The stem of the files to write during the performance
     * benchmark.
     *
     * @throws InterruptedException Exception thrown if a threading exception
     * occurs.
     * @throws TimeoutException Exception thrown if the benchmark times out.
     */
    private void run(
            String fileStem
    ) throws InterruptedException, TimeoutException {

        benchmarkRandom(fileStem);
        benchmarkBySize(fileStem);

    }

    /**
     * Generates random configurations of modifications on peptides and records
     * the time used to find the localization of modifications that maximizes
     * localization scores.
     *
     * @param fileStem The stem of the files to write during the performance
     * benchmark.
     *
     * @throws InterruptedException Exception thrown if a threading exception
     * occurs.
     * @throws TimeoutException Exception thrown if the benchmark times out.
     */
    private void benchmarkRandom(
            String fileStem
    ) throws InterruptedException, TimeoutException {

        try ( SimpleFileWriter writer = new SimpleFileWriter(new File(fileStem + ".benchmark_threads"), false)) {

            writer.writeLine("threads", "peptides", "replicate", "time", "failed");

            for (int i = 1; i <= 6; i++) {

                for (int j = 1; j <= Runtime.getRuntime().availableProcessors() / 2; j++) {

                    int nThreads = 2 * j;

                    int nPeptidesTotal = (int) Math.pow(10, i);
                    int nPeptides = (int) (nPeptidesTotal / nThreads);

                    for (int replicate = 1; replicate <= REPLICATES; replicate++) {

                        System.out.println(Instant.now() + "    Random peptide - " + nPeptidesTotal + " peptides, " + nThreads + " threads (" + replicate + "/" + REPLICATES + ")");

                        ExecutorService pool = Executors.newFixedThreadPool(nThreads);

                        ArrayList<PerformanceBenchmarkRunnable> runnables = new ArrayList<>(nThreads);

                        for (int threadI = 0; threadI < nThreads; threadI++) {

                            PerformanceBenchmarkRunnable runnable = new PerformanceBenchmarkRunnable(nPeptides, null, null, null);

                            pool.submit(runnable);

                            runnables.add(runnable);

                        }

                        Duration duration = new Duration();
                        duration.start();

                        pool.shutdown();

                        if (!pool.awaitTermination(1, TimeUnit.DAYS)) {

                            throw new TimeoutException("Analysis timed out (time out: " + 1 + " days)");

                        }

                        duration.end();

                        int nFailed = runnables.stream()
                                .mapToInt(
                                        runnable -> runnable.failedPeptides
                                )
                                .sum();

                        writer.writeLine(
                                Integer.toString(nThreads),
                                Integer.toString(nPeptidesTotal),
                                Integer.toString(replicate),
                                Long.toString(duration.getDuration()),
                                Integer.toString(nFailed)
                        );

                        System.gc();

                    }
                }
            }
        }
    }

    /**
     * Generates peptides with fixed number of modifications and sites and
     * records the time used to find the localization of modifications that
     * maximizes localization scores.
     *
     * @param fileStem The stem of the files to write during the performance
     * benchmark.
     *
     * @throws InterruptedException Exception thrown if a threading exception
     * occurs.
     * @throws TimeoutException Exception thrown if the benchmark times out.
     */
    private void benchmarkBySize(
            String fileStem
    ) throws InterruptedException, TimeoutException {

        try ( SimpleFileWriter writer = new SimpleFileWriter(new File(fileStem + ".benchmark_size"), false)) {

            writer.writeLine("modifications", "sites", "occupancy", "replicate", "time", "peptides", "failed");

            int nPeptides = 1000;

            for (int i = 1; i <= 10; i++) {

                int nSites = 2 * i;

                for (int occupancy = 1; occupancy <= nSites; occupancy++) {

                    for (int k = 1; k <= 5; k++) {

                        int nMods = 2 * k;

                        for (int replicate = 1; replicate <= REPLICATES; replicate++) {

                            System.out.println(Instant.now() + "    Size benchmark - " + occupancy + " in " + nSites + " sites, " + nMods + " modifications (" + replicate + "/" + REPLICATES + ")");

                            PerformanceBenchmarkRunnable runnable = new PerformanceBenchmarkRunnable(nPeptides, nMods, nSites, occupancy);

                            Duration duration = new Duration();
                            duration.start();

                            runnable.run();

                            duration.end();

                            writer.writeLine(
                                    Integer.toString(nMods),
                                    Integer.toString(nSites),
                                    Integer.toString(occupancy),
                                    Integer.toString(replicate),
                                    Long.toString(duration.getDuration()),
                                    Integer.toString(nPeptides),
                                    Integer.toString(runnable.failedPeptides)
                            );

                            System.gc();

                        }
                    }
                }
            }
        }
    }
}
