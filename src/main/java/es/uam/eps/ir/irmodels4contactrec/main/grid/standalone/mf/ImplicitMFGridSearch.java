/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.irmodels4contactrec.main.grid.standalone.mf;

import es.uam.eps.ir.irmodels4contactrec.graph.fast.FastGraph;
import es.uam.eps.ir.irmodels4contactrec.main.grid.AlgorithmGridSearch;
import es.uam.eps.ir.irmodels4contactrec.main.grid.Grid;
import es.uam.eps.ir.irmodels4contactrec.main.grid.RecommendationAlgorithmFunction;
import es.uam.eps.ir.ranksys.fast.preference.FastPreferenceData;
import es.uam.eps.ir.ranksys.mf.Factorization;
import es.uam.eps.ir.ranksys.mf.Factorizer;
import es.uam.eps.ir.ranksys.mf.als.HKVFactorizer;
import es.uam.eps.ir.ranksys.mf.rec.MFRecommender;
import es.uam.eps.ir.ranksys.rec.Recommender;

import java.util.HashMap;
import java.util.List;

import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Supplier;

import static es.uam.eps.ir.irmodels4contactrec.main.grid.AlgorithmIdentifiers.IMF;


/**
 * Grid search generator for the Implicit Matrix Factorization algorithm by 
 * Hu, Koren and Volinsky (HKV) algorithm.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class ImplicitMFGridSearch<U> implements AlgorithmGridSearch<U>
{   
    /**
     * Identifier fort the parameter that regulates the importance of the error and the norm of the latent vectors.
     */
    private final static String LAMBDA = "lambda";
    /**
     * Identifier for the rate of increase for the confidence
     */
    private final static String ALPHA = "alpha";
    /**
     * Identifier for indicating if teleport always goes to the origin node.
     */
    private final static String K = "k";
    /**
     * Number of iterations for the algorithm
     */
    private final static int NUMITER = 20;

    @Override
    public Map<String, RecommendationAlgorithmFunction<U>> grid(Grid grid)
    {
        Map<String, RecommendationAlgorithmFunction<U>> recs = new HashMap<>();
        
        List<Double> lambdas = grid.getDoubleValues(LAMBDA);
        List<Double> alphas = grid.getDoubleValues(ALPHA);
        List<Integer> ks = grid.getIntegerValues(K);
        
        alphas.forEach(alpha ->
        {
            DoubleUnaryOperator confidence = (double x) -> 1 + alpha*x;
            ks.forEach(k ->
                lambdas.forEach(lambda ->
                    recs.put(IMF + "_" + k + "_" + lambda + "_" + alpha, (graph, prefData) ->
                    {
                       Factorizer<U, U> factorizer = new HKVFactorizer<>(lambda, confidence, NUMITER);
                       Factorization<U, U> factorization = factorizer.factorize(k, prefData);
                       return new MFRecommender<>(prefData, prefData, factorization);
                    })));
        });
        return recs;
    }

    @Override
    public Map<String, Supplier<Recommender<U, U>>> grid(Grid grid, FastGraph<U> graph, FastPreferenceData<U, U> prefData)
    {
        Map<String, Supplier<Recommender<U,U>>> recs = new HashMap<>();
        
        List<Double> lambdas = grid.getDoubleValues(LAMBDA);
        List<Double> alphas = grid.getDoubleValues(ALPHA);
        List<Integer> ks = grid.getIntegerValues(K);

        alphas.forEach(alpha ->
        {
            DoubleUnaryOperator confidence = (double x) -> 1 + alpha*x;
            ks.forEach(k ->
                lambdas.forEach(lambda ->
                    recs.put(IMF + "_" + k + "_" + lambda + "_" + alpha, () ->
                    {
                        Factorizer<U, U> factorizer = new HKVFactorizer<>(lambda, confidence, NUMITER);
                        Factorization<U, U> factorization = factorizer.factorize(k, prefData);
                        return new MFRecommender<>(prefData, prefData, factorization);
                    })));
        });
        return recs;
    }
    
}
