/*
 * Copyright (C) 2019 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.irmodels4contactrec.main;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import es.uam.eps.ir.irmodels4contactrec.data.GraphSimpleFastPreferenceData;
import es.uam.eps.ir.irmodels4contactrec.graph.fast.FastGraph;
import es.uam.eps.ir.irmodels4contactrec.graph.io.GraphReader;
import es.uam.eps.ir.irmodels4contactrec.graph.io.TextGraphReader;
import es.uam.eps.ir.irmodels4contactrec.letor.FeatureInformation;
import es.uam.eps.ir.irmodels4contactrec.letor.FeatureType;
import es.uam.eps.ir.irmodels4contactrec.letor.Instance;
import es.uam.eps.ir.irmodels4contactrec.letor.InstanceSet;
import es.uam.eps.ir.irmodels4contactrec.letor.io.InstanceSetWriter;
import es.uam.eps.ir.irmodels4contactrec.letor.io.LETORInstanceWriter;
import es.uam.eps.ir.irmodels4contactrec.letor.normalization.*;
import es.uam.eps.ir.irmodels4contactrec.letor.sampling.IndividualSampler;
import es.uam.eps.ir.irmodels4contactrec.main.grid.AlgorithmConfigurationReader;
import es.uam.eps.ir.irmodels4contactrec.main.grid.AlgorithmGridSelector;
import es.uam.eps.ir.irmodels4contactrec.main.grid.RecommendationAlgorithmFunction;
import es.uam.eps.ir.irmodels4contactrec.main.grid.sampling.IndividualSamplerFunction;
import es.uam.eps.ir.irmodels4contactrec.main.grid.sampling.IndividualSamplingAlgorithmGridReader;
import es.uam.eps.ir.irmodels4contactrec.main.grid.sampling.IndividualSamplingAlgorithmGridSelector;
import es.uam.eps.ir.irmodels4contactrec.recommender.filler.Filler;
import es.uam.eps.ir.irmodels4contactrec.recommender.filler.RandomFiller;
import es.uam.eps.ir.irmodels4contactrec.utils.Pair;
import es.uam.eps.ir.irmodels4contactrec.utils.Tuple2oo;
import es.uam.eps.ir.ranksys.core.Recommendation;
import es.uam.eps.ir.ranksys.fast.preference.FastPreferenceData;
import es.uam.eps.ir.ranksys.rec.Recommender;
import org.ranksys.formats.parsing.Parsers;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
import org.ranksys.core.util.tuples.Tuple2od;

import static es.uam.eps.ir.irmodels4contactrec.letor.FeatureType.CONTINUOUS;
import static es.uam.eps.ir.irmodels4contactrec.letor.normalization.NormalizationIdentifiers.*;

/**
 * Class for generating Learning to Rank patterns according to the LETOR format.
 * @author Javier Sanz-Cruzado Puig
 */
public class LETORFeatureGenerator
{
    /**
     * Builds a set of learning to rank patterns using similarities between 
     * pairs of users.
     * 
     * @param args Execution arguments:
     * <ul>
     *  <li><b>Train-instance graph:</b> Graph for obtaining the features of the training set</li>
     *  <li><b>Train-class graph:</b> Graph for obtaining the relevance of each training instance</li>
     *  <li><b>Test-instance graph:</b> Graph for obtaining the features of the test set</li>
     *  <li><b>Test-class graph:</b> Graph for obtaining the relevance of each test set example</li>
     *  <li><b>Directed</b> true if the graph is directed, false otherwise</li>
     *  <li><b>Weighted</b> true if the graph is weighted, false otherwise</li>
     *  <li><b>Train sampling:</b> configuration for the individual sampler used in training.</li>
     *  <li><b>Test sampling:</b> configuration for the individual sampler used in test.</li>
     *  <li><b>Recommender conf:</b> XML file containing the configurations we want to use.</li>
     *  <li><b>Train output:</b> File to store the training examples</li>
     *  <li><b>Test output:</b> File to store the test examples</li>
     *  <li><b>Normalization:</b> Score normalization (for each query):
     *      <ul>
     *          <li><u>none:</u> No normalization</li>
     *          <li><u>ranksim:</u> Ranking normalization</li>
     *          <li><u>minmax:</u> Rescale the scores to interval [0,1]</li>
     *          <li><u>z-score:</u> Rescale the query to have 0 mean and 1 variance</li>
     *      </ul>
     * </ul>
     * 
     * @throws IOException if something fails while reading/writing
     */
    public static void main(String[] args) throws IOException
    {
        if(args.length < 15)
        {
            System.err.println("ERROR: Invalid arguments");
            System.err.println("\tTrain-instance graph: Graph for obtaining the features of the training set");
            System.err.println("\tTrain-class graph: Graph for obtaining the relevance of each training instance");
            System.err.println("\tTest-instance graph: Graph for obtaining the features of the test set");
            System.err.println("\tDirected: true if the graph is directed, false otherwise");
            System.err.println("\tWeighted: true if the graph is weighted, false otherwise");
            System.err.println("\tTrain sampling: configuration for the individual sampler used in training");
            System.err.println("\tTest sampling: configuration for the individual sampler used in test");
            System.err.println("\tRecommender conf: XML containing the recommender configurations we want to use");
            System.err.println("\tTrain output: file to store the training examples");
            System.err.println("\tTest output: file to store the test examples");
            System.err.println("\tDescription output: file to store the description of the dataset");
            System.err.println("\tNormalization: the score normalization for each query. Possible values:");
            System.err.println("\t\tnone: no normalization");
            System.err.println("\t\tranksim: ranking normalization");
            System.err.println("\t\tminmax: rescales the scores to interval [0,1]");
            System.err.println("\t\tz-score: rescales the scores to have mean 0 and variance 1");
        }
        
        // Files of the graphs for generating the training set.
        String patternsTrain = args[0];
        String patternsTest = args[1];
        
        // Files of the graphs for generating the test set.
        String train = args[2];
        String test = args[3];
        
        // Values to check whether the graphs are directed and weighted or not.
        boolean directed = args[4].equalsIgnoreCase("true");
        
        // Different possibilities for weighted graphs
        // Use weighted graph for sampling
        boolean weightedSampling = args[5].equalsIgnoreCase("true");
        // Use weighted graph for classes
        boolean weightedClasses = args[6].equalsIgnoreCase("true");
        // Use weighted graph for features
        boolean weightedFeatures = args[7].equalsIgnoreCase("true");
        
        // Individual sampling algorithms for training and test.
        String samplingTrain = args[8];
        String samplingTest = args[9];
        
        // The metrics grid.
        String algorithmGrid = args[10];
        
        // Output files for training and test patterns.
        String outputTrain = args[11];
        String outputTest = args[12];
        String outputDescr = args[13];
        
        String normalization = args[14];
        boolean onlyTest = false;
        if(args.length > 15)
            onlyTest = args[15].equalsIgnoreCase("true");
        
        
        // first, we identify which similarities we are going to use.
        AlgorithmConfigurationReader simConfig = new AlgorithmConfigurationReader(algorithmGrid);
        simConfig.readDocument();
        
        Set<String> sims = simConfig.getAlgorithms();
        AlgorithmGridSelector<Long> selector = new AlgorithmGridSelector<>();
        List<String> descriptions = new ArrayList<>();
        List<FeatureType> types = new ArrayList<>();
        
        List<RecommendationAlgorithmFunction<Long>> similarities = new ArrayList<>();
        
        for(String sim : sims)
        {
            Map<String, RecommendationAlgorithmFunction<Long>> aux = selector.getRecommenders(sim, simConfig.getConfigurations(sim));
            if(aux == null)
            {
                System.err.println(sim + " failed");
                return;
            }
            aux.forEach((key, value) -> 
            {
                descriptions.add(key);
                types.add(CONTINUOUS);
                similarities.add(value);
            });
        }
        long a = System.currentTimeMillis();
        long b;
        FeatureInformation featInfo;
        if(!onlyTest)
        {
            
            featInfo = LETORFeatureGenerator.computeInstances(patternsTrain, patternsTest, directed, weightedSampling, weightedClasses, weightedFeatures, samplingTrain, outputTrain, descriptions,types, similarities, normalization);
            b = System.currentTimeMillis();
            if(featInfo == null)
            {
                System.out.println("ERROR: Training patterns were not generated");
                return;
            }
            else
            {
                InstanceSetWriter<Long> writer = new LETORInstanceWriter<>();
                String feats = writer.writeFeatureInfo(featInfo);
                try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputDescr))))
                {
                    bw.write(feats);
                }
            }
            System.out.println("Finished training patterns (" + (b-a)/1000.0 + " s.)");
        }
        
        featInfo = LETORFeatureGenerator.computeInstances(train, test, directed, weightedSampling, weightedClasses, weightedFeatures, samplingTest, outputTest, descriptions, types, similarities, normalization);
        if(featInfo == null)
        {
            System.out.println("ERROR: Test patterns were not generated");
            return;
        }
        b = System.currentTimeMillis();
        System.out.println("Finished test patterns (" + (b-a)/1000.0 + " s.)");
    }

    /**
     * Computes the instances for a pair of graphs.
     * @param train training graph.
     * @param test validation/test graph.
     * @param directed true if the graph is directed, false otherwise.
     * @param weightedSampling true if the graph is weighted, false otherwise.
     * @param sampling sampling algorithm grid.
     * @param output file in which to output the examples.
     * @param descriptions list of features.
     * @param types list of types.
     * @param similarities list of similarity functions.
     * @throws IOException if something failed while creating the instances.
     */
    private static FeatureInformation computeInstances(String train, String test, boolean directed, boolean weightedSampling, boolean weightedClasses, boolean weightedFeatures, String sampling, String output, List<String> descriptions, List<FeatureType> types, List<RecommendationAlgorithmFunction<Long>> similarities, String normalization) throws IOException 
    {
        FeatureInformation featInfo = new FeatureInformation(descriptions,types);
        InstanceSet<Long> patternSet = new InstanceSet<>(featInfo);
        
        long a = System.currentTimeMillis();
        // First, identify sampling algorithm.
        IndividualSamplingAlgorithmGridReader gridReader = new IndividualSamplingAlgorithmGridReader();
        gridReader.readDocument(sampling);
                
        Set<String> auxalgs = gridReader.getIndividualSamplingAlgorithms();
        if(auxalgs == null || auxalgs.isEmpty())
        {
            System.err.println("ERROR: Not sampling algorithm found");
            return null;
        }
        List<String> algorithms = new ArrayList<>(gridReader.getIndividualSamplingAlgorithms());
        String algorithm = algorithms.get(0);
        
        IndividualSamplingAlgorithmGridSelector<Long> gridSelector = new IndividualSamplingAlgorithmGridSelector<>();
        Tuple2oo<String, IndividualSamplerFunction<Long>> function = gridSelector.getIndividualSamplingAlgorithm(algorithm, gridReader.getParameters(algorithm));
        long b = System.currentTimeMillis();
        System.out.println("Individual sampler: " + function.v1() + " (" + (b-a)/1000.0 + " s.)");

        // First step: read the graph for sampling
        GraphReader<Long> greader = new TextGraphReader<>(directed, weightedSampling, false, "\t", Parsers.lp);
        FastGraph<Long> trainGraph = (FastGraph<Long>) greader.read(train, weightedSampling, false);
        FastPreferenceData<Long, Long> prefData = GraphSimpleFastPreferenceData.load(trainGraph);
        
        greader = new TextGraphReader<>(directed, weightedClasses, false, "\t", Parsers.lp);
        FastGraph<Long> testGraph = (FastGraph<Long>) greader.read(test, weightedClasses, false);
        
        b = System.currentTimeMillis();
        System.out.println("Graphs for sampling read: " + (b-a)/1000.0 + " s.)");
               
        // Obtain the queries.        
        List<Long> queryUsers = testGraph.getAllNodes().filter(u -> testGraph.getAdjacentEdgesCount(u) > 0 && trainGraph.containsVertex(u)).collect(Collectors.toCollection(ArrayList::new));
        // Obtain the documents for the queries:
        IndividualSampler<Long> sampler = function.v2().apply(trainGraph, testGraph, prefData);
        
        Set<Pair<Long>> samples = new HashSet<>();
        Map<Long, Set<Long>> categorizedSamples = new HashMap<>();
        int numSamples = 0;
        for(Long u : queryUsers)
        {
            Set<Long> sample = sampler.sampleUsers(u, v -> !trainGraph.containsEdge(u, v) && !trainGraph.containsEdge(v,u) && !u.equals(v));
            numSamples += sample.stream().mapToInt(v -> 
            {
                samples.add(new Pair<>(u,v));
                return 1;
            }).sum();
            categorizedSamples.put(u, sample);
        }
        
        b = System.currentTimeMillis();
        System.out.println("Queries sampled: " + (b-a)/1000.0 + " s.)");
        System.out.println("Total samples: " + numSamples);
        
        FastGraph<Long> defTrainGraph;
        FastPreferenceData<Long, Long> defPrefData;
        // Then, read the training graph for the features.
        if(weightedSampling != weightedFeatures)
        {
            greader = new TextGraphReader<>(directed, weightedFeatures, false, "\t", Parsers.lp);
            defTrainGraph = (FastGraph<Long>) greader.read(train, weightedFeatures, false);
            defPrefData = GraphSimpleFastPreferenceData.load(defTrainGraph);
        }
        else
        {
            defTrainGraph = trainGraph;
            defPrefData = prefData;
        }
        
        b = System.currentTimeMillis();
        
        System.out.println("Training graph for features read (" + (b-a)/1000.0 + " s.)");
        
        int numUsers = new Long(trainGraph.getVertexCount()).intValue();
        Map<String, Map<Pair<Long>, Double>> simRes = new ConcurrentHashMap<>();
        AtomicInteger atomCounter = new AtomicInteger(0);
        int total = similarities.size();
        IntStream.range(0, total).parallel().forEach(i ->
        {
            Map<Pair<Long>,Double> map = new HashMap<>();
            RecommendationAlgorithmFunction<Long> f = similarities.get(i);
            String name = descriptions.get(i);
            long auxa = System.currentTimeMillis();
            System.out.println("Similarity " + name + " started: (" + (auxa - a) + " ms.");
            Recommender<Long,Long> rec = f.apply(defTrainGraph, defPrefData);
            queryUsers.forEach(u -> 
            {
                Recommendation<Long, Long> recomm = rec.getRecommendation(u, x -> categorizedSamples.get(u).contains(x));
                Filler<Long, Long> filler = new RandomFiller<>(defPrefData, 0);
                
                recomm = filler.fill(recomm,numUsers , v -> x -> categorizedSamples.get(v).contains(x));
                
                Recommendation<Long, Long> normRecomm = LETORFeatureGenerator.normalize(recomm, normalization);
                
                List<Tuple2od<Long>> items = normRecomm.getItems();
                for(Tuple2od<Long> item : items)
                {
                    Pair<Long> pair = new Pair<>(u, item.v1);
                    if(samples.contains(pair))
                    {
                        map.put(pair, item.v2);
                    }
                }
            });
            simRes.put(name, map);
            auxa = System.currentTimeMillis();
            int count  = atomCounter.incrementAndGet();
            System.out.println("Similarity " + name + " finished: (" + (auxa - a) + "ms.) " + count + "/" + total);
        });
        
        AtomicInteger atom = new AtomicInteger(0);
        samples.forEach(pair -> 
        {
            long u = pair.v1();
            long v = pair.v2();
            
            boolean introduce = true;
            List<Double> values = new ArrayList<>();
            for(String descr : descriptions)
            {
                Double val = simRes.get(descr).getOrDefault(pair, Double.NaN);
                if(Double.isNaN(val)) 
                {
                    introduce = false;
                    break;
                }
                values.add(val);
            }
           
            int category = testGraph.containsEdge(u,v) ? 1 : 0;
            
            if(introduce)
            {
                Instance<Long> pattern = new Instance<>(u,v,values,category);
                patternSet.addInstance(pattern);
                
            }
            
            int counter = atom.incrementAndGet();
            if(counter % 1000 == 0)
            {
                long auxb = System.currentTimeMillis();
                System.out.println(counter + " patterns computed: " + (auxb-a)/1000.0 + " s.)");
            }
        });
        
        InstanceSetWriter<Long> writer = new LETORInstanceWriter<>();
        writer.write(output, patternSet);
        
        return featInfo;
    }

    /**
     * Normalizes a recommendation.
     * @param recomm the recommendation.
     * @param normalization the identifier of the normalization algorithm.
     * @return the normalized recommendation.
     */
    private static Recommendation<Long, Long> normalize(Recommendation<Long, Long> recomm, String normalization) 
    {
        Normalizer<Long,Long> norm;
        switch(normalization)
        {
            case RANKSIM:
                norm = new RanksimNormalizer<>();
                break;
            case ZSCORE:
                norm = new ZScoreNormalizer<>();
                break;
            case MINMAX:
                norm = new MinMaxNormalizer<>();
                break;
            default:
                norm = new NoNormalizer<>();
        }
        return norm.normalize(recomm);
    }
}
