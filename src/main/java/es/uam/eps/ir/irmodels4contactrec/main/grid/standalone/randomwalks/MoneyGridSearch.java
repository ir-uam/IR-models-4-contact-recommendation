/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.irmodels4contactrec.main.grid.standalone.randomwalks;

import es.uam.eps.ir.irmodels4contactrec.graph.fast.FastGraph;
import es.uam.eps.ir.irmodels4contactrec.main.grid.AlgorithmGridSearch;
import es.uam.eps.ir.irmodels4contactrec.main.grid.Grid;
import es.uam.eps.ir.irmodels4contactrec.main.grid.RecommendationAlgorithmFunction;
import es.uam.eps.ir.irmodels4contactrec.recommender.standalone.randomwalk.Money;
import es.uam.eps.ir.ranksys.fast.preference.FastPreferenceData;
import es.uam.eps.ir.ranksys.rec.Recommender;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static es.uam.eps.ir.irmodels4contactrec.main.grid.AlgorithmIdentifiers.MONEY;


/**
 * Grid search generator for Personalized SALSA algorithm.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class MoneyGridSearch<U> implements AlgorithmGridSearch<U>
{   
    /**
     * Identifier for the mode of the algorithm
     */
    private final static String MODE = "mode";
    
    /**
     * Teleport rate for the HITS algorithm
     */
    private final static String ALPHA = "alpha";

    @Override
    public Map<String, RecommendationAlgorithmFunction<U>> grid(Grid grid)
    {
        Map<String, RecommendationAlgorithmFunction<U>> recs = new HashMap<>();
        
        List<Boolean> modes = grid.getBooleanValues(MODE);
        List<Double> alphas = grid.getDoubleValues(ALPHA);
        alphas.forEach(alpha ->
            modes.forEach(mode ->
                recs.put(MONEY + "_" + (mode ? "auth" : "hubs") + "_" + alpha, (graph, prefData) -> new Money<>(graph, mode, alpha))));
        return recs;
    }

    @Override
    public Map<String, Supplier<Recommender<U, U>>> grid(Grid grid, FastGraph<U> graph, FastPreferenceData<U, U> prefData)
    {
        Map<String, Supplier<Recommender<U,U>>> recs = new HashMap<>();
        
        List<Boolean> modes = grid.getBooleanValues(MODE);
        List<Double> alphas = grid.getDoubleValues(ALPHA);

        alphas.forEach(alpha ->
                modes.forEach(mode ->
                        recs.put(MONEY + "_" + (mode ? "auth" : "hubs") + "_" + alpha, () -> new Money<>(graph, mode, alpha))));
        return recs;
    }
    
}
