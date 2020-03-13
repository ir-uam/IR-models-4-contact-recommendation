/*
 * Copyright (C) 2020 Information Retrieval Group at Universidad Autónoma
 * de Madrid, http://ir.ii.uam.es and Terrier Team at University of Glasgow,
 * http://terrierteam.dcs.gla.ac.uk/.
 *
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.irmodels4contactrec.main;

import es.uam.eps.ir.ranksys.metrics.SystemMetric;
import es.uam.eps.ir.ranksys.rec.Recommender;
import es.uam.eps.ir.ranksys.rec.runner.RecommenderRunner;
import org.jooq.lambda.tuple.Tuple2;
import org.ranksys.formats.parsing.Parsers;
import org.ranksys.formats.rec.RecommendationFormat;
import org.ranksys.formats.rec.SimpleRecommendationFormat;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class containing auxiliar methods for the Main functions.
 *
 * @author Javier Sanz-Cruzado (javier.sanz-cruzado@uam.es)
 * @author Craig Macdonald (craig.macdonald@glasgow.ac.uk)
 * @author Iadh Ounis (iadh.ounis@glasgow.ac.uk)
 * @author Pablo Castells (pablo.castells@uam.es)
 */
public class AuxiliarMethods
{
    /**
     * Computes a recommendation and evaluates it using nDCG metric.
     *
     * @param output      Route of the file in which to store the recommendation.
     * @param recommender The recommender to apply.
     * @param runner      The recommender runner
     * @param metric      The metric.
     *
     * @return the value of the metric.
     *
     * @throws IOException if something fails during the writing / reading of the recommendation file.
     */
    public static double computeAndEvaluate(String output, Recommender<Long, Long> recommender, RecommenderRunner<Long, Long> runner, SystemMetric<Long, Long> metric) throws IOException
    {
        RecommendationFormat<Long, Long> format = new SimpleRecommendationFormat<>(Parsers.lp, Parsers.lp);
        RecommendationFormat.Writer<Long, Long> writer;
        RecommendationFormat.Reader<Long, Long> reader;

        metric.reset();

        writer = format.getWriter(output);
        runner.run(recommender, writer);
        writer.close();

        reader = format.getReader(output);
        reader.readAll().forEach(rec ->
        {
            if (rec != null && rec.getItems() != null && !rec.getItems().isEmpty())
            {
                metric.add(rec);
            }
        });
        return metric.evaluate();
    }

    /**
     * Computes a recommendation and evaluates it using metrics.
     *
     * @param output      Route of the file in which to store the recommendation.
     * @param recommender The recommender to apply.
     * @param runner      The recommender runner
     * @param metrics      The metrics to evaluate
     *
     * @return the value of the metrics.
     *
     * @throws IOException if something fails during the writing / reading of the recommendation file.
     */
    public static Map<String, Double> computeAndEvaluate(String output, Recommender<Long, Long> recommender, RecommenderRunner<Long, Long> runner, Map<String, SystemMetric<Long, Long>> metrics) throws IOException
    {
        RecommendationFormat<Long, Long> format = new SimpleRecommendationFormat<>(Parsers.lp, Parsers.lp);
        RecommendationFormat.Writer<Long, Long> writer;
        RecommendationFormat.Reader<Long, Long> reader;

        Map<String, Double> values = new HashMap<>();


        writer = format.getWriter(output);
        runner.run(recommender, writer);
        writer.close();

        metrics.values().forEach(SystemMetric::reset);

        reader = format.getReader(output);
        reader.readAll().forEach(rec ->
        {
            if (rec != null && rec.getItems() != null && !rec.getItems().isEmpty())
            {
                metrics.values().forEach(metric -> metric.add(rec));
            }
        });

        metrics.forEach((key, value) -> values.put(key, value.evaluate()));
        return values;
    }

    /**
     * Computes a recommendation and evaluates it using nDCG metric. It does not write the recommendation.
     *
     * @param recommender The recommender to apply.
     * @param runner      The recommender runner.
     * @param metric      The metric.
     *
     * @return the value of the metric.
     */
    public static double computeAndEvaluate(Recommender<Long, Long> recommender, RecommenderRunner<Long, Long> runner, SystemMetric<Long, Long> metric)
    {
        EmptyWriter<Long, Long> writer = new EmptyWriter<>();
        runner.run(recommender, writer);

        metric.reset();
        writer.readAll().forEach(rec ->
        {
            if (rec != null && rec.getItems() != null && !rec.getItems().isEmpty())
            {
                metric.add(rec);
            }
        });

        return metric.evaluate();
    }

    /**
     * Computes a recommendation and evaluates it using some metrics. It does not write the recommendation.
     *
     * @param recommender The recommender to apply.
     * @param runner      The recommender runner.
     * @param metrics      The metrics.
     *
     * @return the value of the metrics.
     */
    public static Map<String, Double> computeAndEvaluate(Recommender<Long, Long> recommender, RecommenderRunner<Long, Long> runner, Map<String, SystemMetric<Long,Long>> metrics)
    {
        EmptyWriter<Long, Long> writer = new EmptyWriter<>();
        runner.run(recommender, writer);

        Map<String, Double> values = new HashMap<>();
        metrics.values().forEach(SystemMetric::reset);
        writer.readAll().forEach(rec ->
        {
            if (rec != null && rec.getItems() != null && !rec.getItems().isEmpty())
            {
                metrics.values().forEach(metric -> metric.add(rec));
            }
        });

        metrics.forEach((key, value) -> values.put(key, value.evaluate()));
        return values;
    }

    /**
     * Given two maps with the same keys, generates a new file that prints the nDCG values for both.
     *
     * @param output    The output file.
     * @param first     the first map.
     * @param second    the second map.
     * @param firstId   identifier for the first map.
     * @param secondId  identifier for the second map.
     * @param maxLength maximum length of the recommendation.
     */
    public static void printFile(String output, Map<String, Double> first, Map<String, Double> second, String firstId, String secondId, int maxLength)
    {
        List<Tuple2<String, Double>> list = new ArrayList<>();
        int numVariants = 0;
        for (String variant : first.keySet())
        {
            double val = first.get(variant) - second.get(variant);
            list.add(new Tuple2<>(variant, val));
            ++numVariants;
        }

        list.sort((o1, o2) ->
        {
            int val = Double.compare(o1.v2, o2.v2);
            if (val == 0)
                return o1.v1.compareTo(o2.v1);
            return val;
        });

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output))))
        {
            bw.write("Variant\t%\tnDCG@" + maxLength + "(" + firstId + ")\tnDCG@" + maxLength + "(" + secondId + ")\tDifference");
            int i = 0;
            for (Tuple2<String, Double> tuple : list)
            {
                ++i;
                bw.write("\n" + tuple.v1 + "\t" + i / (numVariants + 0.0) + "\t" + first.get(tuple.v1) + "\t" + second.get(tuple.v1) + "\t" + tuple.v2);
            }
        }
        catch (IOException ioe)
        {
            System.err.println("ERROR: Something failed while writing the output file");
        }
    }


}
