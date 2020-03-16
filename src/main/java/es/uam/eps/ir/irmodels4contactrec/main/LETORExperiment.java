/*
 * Copyright (C) 2019 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 *
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.irmodels4contactrec.main;


import es.uam.eps.ir.irmodels4contactrec.data.FastGraphIndex;
import es.uam.eps.ir.irmodels4contactrec.data.GraphIndex;
import es.uam.eps.ir.irmodels4contactrec.data.GraphSimpleFastPreferenceData;
import es.uam.eps.ir.irmodels4contactrec.graph.Adapters;
import es.uam.eps.ir.irmodels4contactrec.graph.Graph;
import es.uam.eps.ir.irmodels4contactrec.graph.fast.FastGraph;
import es.uam.eps.ir.irmodels4contactrec.graph.io.TextGraphReader;
import es.uam.eps.ir.irmodels4contactrec.letor.FeatureInformation;
import es.uam.eps.ir.irmodels4contactrec.letor.FeatureType;
import es.uam.eps.ir.irmodels4contactrec.letor.Instance;
import es.uam.eps.ir.irmodels4contactrec.letor.InstanceSet;
import es.uam.eps.ir.irmodels4contactrec.letor.io.InstanceSetReader;
import es.uam.eps.ir.irmodels4contactrec.letor.io.LETORInstanceReader;
import es.uam.eps.ir.irmodels4contactrec.recommender.SocialFastFilters;
import es.uam.eps.ir.irmodels4contactrec.recommender.letor.LambdaMARTRecommender;
import es.uam.eps.ir.irmodels4contactrec.utils.generator.Generators;
import es.uam.eps.ir.ranksys.fast.preference.FastPreferenceData;
import es.uam.eps.ir.ranksys.rec.Recommender;
import es.uam.eps.ir.ranksys.rec.runner.RecommenderRunner;
import es.uam.eps.ir.ranksys.rec.runner.fast.FastFilterRecommenderRunner;
import es.uam.eps.ir.ranksys.rec.runner.fast.FastFilters;
import org.ranksys.formats.parsing.Parsers;
import org.ranksys.formats.rec.RecommendationFormat;
import org.ranksys.formats.rec.TRECRecommendationFormat;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static es.uam.eps.ir.irmodels4contactrec.letor.io.LETORFormatConstants.*;
import static org.ranksys.formats.parsing.Parsers.lp;

/**
 * Learning to rank experiment. Given a set of features, and a ranking of those features,
 * it builds the rankings
 * @author Javier Sanz-Cruzado Puig
 */
public class LETORExperiment
{
    /**
     *
     * @param args Execution arguments
     * @throws IOException if something fails while reading/writing
     */
    public static void main(String[] args) throws IOException, InterruptedException
    {
        if(args.length < 13)
        {
            System.err.println("ERROR: Invalid arguments");
            System.err.println("\ttrain file: full set of train instances");
            System.err.println("\tvalid file: full set of validation instances");
            System.err.println("\ttest file: full set of test instances");
            System.err.println("\tnumFeats: the number of features");
            System.err.println("\texp. directory: directory in which the experiment will be executed");
            System.err.println("\tterrier: route where the terrier binary is");
            System.err.println("\tjforest prop.: route for the properties file of jforest");
            System.err.println("\trec train data: training data for the recommender");
            System.err.println("\trec test data: test data for the recommender");
            System.err.println("\tdirected: true if the graph is directed, false otherwise");
            System.err.println("\tweighted: true if the graph is weighted, false otherwise");
            System.err.println("\trec output: folder in which to store the outcome of the recommendation");
            System.err.println("\tcutoff: cutoff of the recommendation");
        }

        // Read the parameters

        // First, read the instance set parameters
        String trainFile = args[0];
        String validFile = args[1];
        String testFile = args[2];
        int numFeats = Parsers.ip.parse(args[3]);

        // Terrier execution parameters
        String expDirectory = args[4];
        String terrier = args[5];
        String jforestprop = args[6];

        // Ranksys execution parameters.
        String recTrain = args[7];
        String recTest = args[8];
        boolean directed = args[9].equalsIgnoreCase("true");
        boolean weighted = args[10].equalsIgnoreCase("true");
        String recOutput = args[11];
        int maxLength = Parsers.ip.parse(args[12]);

        boolean sample = args[13].equalsIgnoreCase("true");
        boolean copy = args[14].equalsIgnoreCase("true");

        // Then, generate the jforests-discrete files for the whole collection:
        String parent = expDirectory;
        if (!parent.endsWith(File.separator)) parent += File.separator;

        String trainFileRaw = new File(trainFile).getName();
        String validFileRaw = new File(validFile).getName();
        String testFileRaw = new File(testFile).getName();
        long bb;
        long aa = System.currentTimeMillis();
        if(sample)
        {

            // First, create the directories to store all the datasets.
            for (int i = 0; i < numFeats; ++i)
            {
                String directoryName = expDirectory + i + File.separator;
                File dir = new File(directoryName);
                boolean done = dir.mkdir();
            }

            // Read and generate the training features.
            FeatureInformation trainFeatInfo = LETORExperiment.readAndSample(trainFile, expDirectory, numFeats, "train.letor");
            bb = System.currentTimeMillis();
            System.out.println("Train features computed (" + (bb - aa) / 1000.0 + " s.)");

            // Read and generate the validation features.
            FeatureInformation validFeatInfo = LETORExperiment.readAndSample(validFile, expDirectory, numFeats, "valid.letor");
            bb = System.currentTimeMillis();
            System.out.println("Validation features computed (" + (bb - aa) / 1000.0 + " s.)");

            // Read and generate the test features.
            FeatureInformation testFeatInfo = LETORExperiment.readAndSample(testFile, expDirectory, numFeats, "test.letor");
            bb = System.currentTimeMillis();
            System.out.println("Test features computed (" + (bb - aa) / 1000.0 + " s.)");


            StringBuilder builder = new StringBuilder();
            builder.append("FeatureIndex\tname\tMin\tMax");
            for (int i = 0; i < numFeats; ++i)
            {
                builder.append("\n");
                builder.append((i + 1));
                builder.append("\tnull\t");
                builder.append(Math.min(trainFeatInfo.getStats(i).getMin(), validFeatInfo.getStats(i).getMin()));
                builder.append("\t");
                builder.append(Math.max(trainFeatInfo.getStats(i).getMax(), validFeatInfo.getStats(i).getMax()));

                if (i > 0)
                {
                    try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(expDirectory + i + File.separator + "jforests-feature-stats.txt"))))
                    {
                        bw.write(builder.toString());
                    }
                }
            }

            try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(parent + "jforests-feature-stats.txt"))))
            {
                bw.write(builder.toString());
            }


        }
        else
        {
            // Read the jforests-feature-stats.txt
            try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(parent + "jforests-feature-stats.txt"))))
            {
                StringBuilder strBuilder = new StringBuilder();
                // Read the header
                String line = br.readLine();
                strBuilder.append(line);
                int i = 0;
                while((line = br.readLine()) != null)
                {
                    strBuilder.append("\n");
                    strBuilder.append(line);
                    if(i > 0)
                    {
                        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(expDirectory + i + File.separator + "jforests-feature-stats.txt"))))
                        {
                            bw.write(strBuilder.toString());
                        }
                    }
                    ++i;
                }
            }
        }

        if(copy)
        {
            // Then, generate the bin files for the whole data set:
            String cmda = terrier + " --config-file " + jforestprop + " --cmd=generate-bin --ranking --folder " + parent + " --file " + trainFileRaw + " --file " + validFileRaw;
            Process processa = Runtime.getRuntime().exec(cmda);
            processa.waitFor();
            bb = System.currentTimeMillis();
            System.out.println("Generated bins for train and validation with all features (" + (bb - aa) / 1000.0 + " s.)");

            // Then, generate the bin files for test
            cmda = terrier + " --config-file " + jforestprop + " --cmd=generate-bin --ranking --folder " + parent + " --file " + testFileRaw;
            processa = Runtime.getRuntime().exec(cmda);
            processa.waitFor();
            bb = System.currentTimeMillis();
            System.out.println("Generated bins for test with all features (" + (bb - aa) / 1000.0 + " s.)");

            // And read and split in the same way as above (read this only once):
            trainFileRaw = "jforests-discrete-" + trainFileRaw;
            validFileRaw = "jforests-discrete-" + validFileRaw;
            testFileRaw = "jforests-discrete-" + testFileRaw;

            LETORExperiment.readAndSampleReducedDiscrete(parent + trainFileRaw, expDirectory, numFeats, "train.letor");
            bb = System.currentTimeMillis();
            System.out.println("Train reduced features computed (" + (bb - aa) / 1000.0 + " s.)");
            LETORExperiment.readAndSampleReducedDiscrete(parent + validFileRaw, expDirectory, numFeats, "valid.letor");
            bb = System.currentTimeMillis();
            System.out.println("Validation reduced features computed (" + (bb - aa) / 1000.0 + " s.)");
            LETORExperiment.readAndSampleReducedDiscrete(parent + testFileRaw, expDirectory, numFeats, "test.letor");
            bb = System.currentTimeMillis();
            System.out.println("Test reduced features computed (" + (bb - aa) / 1000.0 + " s.)");
        }
        // Read the user and item indexes
        TextGraphReader<Long> greader = new TextGraphReader<>(directed, weighted, true, "\t", Parsers.lp);
        Graph<Long> auxgraph = greader.read(recTrain, weighted, false);
        if(auxgraph == null)
        {
            System.err.println("ERROR: Problems while reading the training graph");
            return;
        }

        FastGraph<Long> graph = (FastGraph<Long>) Adapters.removeAutoloops(auxgraph);
        if(graph == null)
        {
            System.err.println("ERROR: Problems while removing auto-loops from the training graph");
            return;
        }

        //FastGraph<Long> complgraph = (FastGraph<Long>) Adapters.addAllAutoloops(graph);
        auxgraph = greader.read(recTest, weighted, false);
        if(auxgraph == null)
        {
            System.err.println("ERROR: Problems while reading the test graph");
            return;
        }

        FastGraph<Long> testGraph = (FastGraph<Long>) Adapters.onlyTrainUsers(auxgraph, graph);
        if(testGraph == null)
        {
            System.err.println("ERROR: Problems while filtering the test graph");
            return;
        }
        // Read the training and test data
        FastPreferenceData<Long, Long> trainData;
        trainData = GraphSimpleFastPreferenceData.load(graph);

        FastPreferenceData<Long, Long> testData;
        testData = GraphSimpleFastPreferenceData.load(testGraph);
        GraphIndex<Long> index = new FastGraphIndex<>(graph);

        bb = System.currentTimeMillis();
        System.out.println("Recommendation data read(" + (bb-aa)/1000.0 + " s.)");

        // Execute the recommender
        // Select the set of users to be recommended, the format, and the filters to apply to the recommendation
        Set<Long> targetUsers = testData.getUsersWithPreferences().collect(Collectors.toCollection(HashSet::new));
        System.out.println("Num. target users: " + targetUsers.size());
        RecommendationFormat<Long, Long> format = new TRECRecommendationFormat<>(lp,lp);
        Function<Long,IntPredicate> filter = FastFilters.and(FastFilters.notInTrain(trainData), FastFilters.notSelf(index), SocialFastFilters.notReciprocal(graph,index));

        // Now, start the experiment.
        IntStream.range(1, numFeats).forEach(i ->
        {
            try
            {
                long a = System.currentTimeMillis();
                System.out.println("Starting experiment with the top " + (i+1) + " features");
                // First, we create a directory for executing this:

                String directoryName = expDirectory + i + File.separator;
                File dir = new File(directoryName);

                // Then, generate the bin files for train and validation
                // Generate the bins file for the corresponding file.
                String cmd = terrier + " --config-file " + jforestprop + " --cmd=generate-bin --ranking --folder " + directoryName + " --file train.letor --file valid.letor > " + directoryName + "bins-train.txt";
                System.out.println(cmd);
                Process process = Runtime.getRuntime().exec(cmd);
                process.waitFor();
                long b = System.currentTimeMillis();
                System.out.println("Generated bins for train and validation with " + i +" features (" + (b-a)/1000.0 + " s.)");


                // Then, generate the bin files for test
                cmd = terrier + " --config-file " + jforestprop + " --cmd=generate-bin --ranking --folder " + directoryName + " --file test.letor > " + directoryName + "bins-test.txt";
                System.out.println(cmd);
                process = Runtime.getRuntime().exec(cmd);
                process.waitFor();
                b = System.currentTimeMillis();
                System.out.println("Generated bins for test with " + i +" features (" + (b-a)/1000.0 + " s.)");

                // Then, train the model
                cmd = terrier + " --config-file " + jforestprop + " --cmd=train --ranking --train-file " + directoryName + "train.bin --validation-file " + directoryName + "valid.bin --output-model " + directoryName + "model.txt";
                System.out.println(cmd);
                process = Runtime.getRuntime().exec(cmd);
                process.waitFor();
                b = System.currentTimeMillis();
                System.out.println("Model trained for experiment with " + i + " features (" + (b-a)/1000.0 + " s.)");

                // Execute the predictions
                cmd = terrier + " --config-file " + jforestprop + " --cmd=predict --ranking --model " + directoryName + "model.txt --tree-type RegressionTree --test-file " + directoryName + "test.bin --output-file " + directoryName + "pred.txt";
                System.out.println(cmd);
                process = Runtime.getRuntime().exec(cmd);
                process.waitFor();
                b = System.currentTimeMillis();
                System.out.println("Model predictions done for experiment with " + i + " features (" + (b-a)/1000.0 + " s.)");

                RecommenderRunner<Long,Long> runner = new FastFilterRecommenderRunner<>(index, index, targetUsers.stream(), filter, maxLength);
                Recommender<Long, Long> rec = new LambdaMARTRecommender<>(graph, directoryName + "test.letor", directoryName + "pred.txt", Parsers.lp);
                b = System.currentTimeMillis();
                System.out.println("Recommendation with " + i + " features prepared (" + (b-a)/1000.0 + " s.)");

                RecommendationFormat.Writer<Long,Long> recWriter = format.getWriter(recOutput + i + ".txt");

                runner.run(rec, recWriter);
                recWriter.close();
                b = System.currentTimeMillis();
                System.out.println("Recommendation with " + i + " features done (" + (b-a)/1000.0 + " s.)");

                /*for(File f : dir.listFiles())
                {
                    f.delete();
                }
                dir.delete();*/
                System.out.println("Experiment with " + i + " features done (" + (b-a)/1000.0 + " s.)");
            }
            catch (IOException ex)
            {
                System.err.println("ERROR: Something failed while executing exp. for " + i + " features");
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        });
    }


    /**
     * Generates the samples for a collection of datasets.
     * @param file the file.
     * @param expDirectory the directory in which to store the file.
     * @param numFeats the number of features.
     * @param filename the name of the file.
     * @return the read instance set.
     * @throws IOException if something fails while reading / writing.
     */
    private static FeatureInformation readAndSample(String file, String expDirectory, int numFeats, String filename) throws IOException
    {
        long a = System.currentTimeMillis();
        long b;
        List<Writer> writers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file))))
        {
            InstanceSetReader<Long> reader = new LETORInstanceReader<>(Parsers.lp);


            for (int i = 1; i < numFeats; ++i)
            {
                writers.add(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(expDirectory + i + File.separator + filename))));
            }

            List<String> featureDescr = new ArrayList<>();
            List<FeatureType> featureTypes = new ArrayList<>();

            // First, read the headers containing the feature information.
            String line;
            FeatureInformation featInfo;

            List<String> header = new ArrayList<>();
            int i = 0;
            // Write the headers for each file.
            StringBuilder builder = new StringBuilder();
            while ((line = br.readLine()).startsWith(COMMENT))
            {
                header.add(line);
                builder.append(line);
                builder.append("\n");
                if (i > 0)
                {
                    writers.get(i-1).write(builder.toString());
                }
                ++i;
            }

            featInfo = reader.readHeader(header);

            b = System.currentTimeMillis();
            System.out.println("Read header (" + (b-a)/1000.0 + " s.)");

            AtomicInteger atom = new AtomicInteger(0);
            // Then, read each instance.
            do
            {
                Instance<Long> pattern = reader.readInstance(line, numFeats);
                featInfo.updateStats(pattern);

                builder = new StringBuilder();
                builder.append(pattern.getCategory());
                builder.append(SEPARATOR);
                builder.append(QID);
                builder.append(pattern.getOrigin());

                String dest = COMMENT + DOCID + pattern.getDest();

                List<Double> values = pattern.getValues();
                i = 0;
                for (double value : values)
                {
                    builder.append(SEPARATOR);
                    builder.append((i+1));
                    builder.append(IDSEP);
                    builder.append(value);

                    if (i > 0)
                    {
                        writers.get(i-1).write(builder.toString() + SEPARATOR + dest + "\n");
                    }
                    ++i;
                }

                int count = atom.incrementAndGet();
                if(count%10000 == 0)
                {
                    b = System.currentTimeMillis();
                    System.out.println("Computed " + count + " instances (" + (b-a)/1000.0 + " s.)");
                }

            }
            while ((line = br.readLine()) != null);

            return featInfo;

        }
        finally
        {
            for (Writer writer : writers) writer.close();
        }
    }


    /**
     * Generates the samples for a collection of datasets.
     * @param file the file.
     * @param expDirectory the directory in which to store the file.
     * @param numFeats the number of features.
     * @param filename the name of the file.
     * @throws IOException if something fails while reading / writing.
     */
    private static void readAndSampleReducedDiscrete(String file, String expDirectory, int numFeats, String filename) throws IOException
    {
        long a = System.currentTimeMillis();
        long b;
        List<Writer> writers = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file))))
        {
            InstanceSet<Long> instanceSet;
            InstanceSetReader<Long> reader = new LETORInstanceReader<>(Parsers.lp, numFeats, Generators.longgen);

            for (int i = 1; i < numFeats; ++i)
            {
                writers.add(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(expDirectory + i + File.separator + "jforests-discrete-" + filename))));
            }

            List<String> featureDescr = new ArrayList<>();
            List<FeatureType> featureTypes = new ArrayList<>();

            // In this case, we have no headers:
            String line;
            int counter = 0;
            // Then, read each instance.
            while((line = br.readLine()) != null)
            {
                Instance<Long> pattern = reader.readInstance(line, numFeats);

                StringBuilder builder = new StringBuilder();
                builder.append(pattern.getCategory());
                builder.append(SEPARATOR);
                builder.append(QID);
                builder.append(pattern.getOrigin());

                String dest = COMMENT + DOCID + pattern.getDest();

                List<Double> values = pattern.getValues();
                int i = 0;
                for (double value : values)
                {
                    if(value > 0.0)
                    {
                        builder.append(SEPARATOR);
                        builder.append((i+1));
                        builder.append(IDSEP);
                        builder.append(value);
                    }

                    if (i > 0)
                    {
                        writers.get(i-1).write(builder.toString() + dest + "\n");
                    }

                    ++i;
                }

                counter++;
                if(counter % 10000 == 0)
                {
                    b = System.currentTimeMillis();
                    System.out.println("Computed " + counter + " instances (" + (b-a)/1000.0 + " s.)");
                }
            }
        }
        finally
        {
            for (Writer writer : writers) writer.close();
        }
    }
}
