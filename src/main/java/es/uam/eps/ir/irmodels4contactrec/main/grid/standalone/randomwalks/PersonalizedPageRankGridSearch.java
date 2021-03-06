/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.irmodels4contactrec.main.grid.standalone.randomwalks;

import static es.uam.eps.ir.irmodels4contactrec.main.grid.AlgorithmIdentifiers.PERSPAGERANK;

import es.uam.eps.ir.irmodels4contactrec.main.grid.Grid;
import es.uam.eps.ir.irmodels4contactrec.main.grid.AlgorithmGridSearch;
import es.uam.eps.ir.irmodels4contactrec.graph.fast.FastGraph;
import es.uam.eps.ir.irmodels4contactrec.recommender.standalone.randomwalk.PersonalizedPageRank;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.uam.eps.ir.irmodels4contactrec.main.grid.RecommendationAlgorithmFunction;
import java.util.function.Supplier;
import es.uam.eps.ir.ranksys.fast.preference.FastPreferenceData;
import es.uam.eps.ir.ranksys.rec.Recommender;

/**
 * Grid search generator for Personalized PageRank algorithm.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class PersonalizedPageRankGridSearch<U> implements AlgorithmGridSearch<U> 
{   
    /**
     * Identifier for the teleport parameter
     */
    private final static String R = "r";

    @Override
    public Map<String, RecommendationAlgorithmFunction<U>> grid(Grid grid)
    {
        Map<String, RecommendationAlgorithmFunction<U>> recs = new HashMap<>();
        
        List<Double> rs = grid.getDoubleValues(R);
        rs.forEach(r -> recs.put(PERSPAGERANK + "_" + r, (graph, prefData) -> new PersonalizedPageRank<>(graph, r)));
        return recs;   
    }

    @Override
    public Map<String, Supplier<Recommender<U, U>>> grid(Grid grid, FastGraph<U> graph, FastPreferenceData<U, U> prefData)
    {
        Map<String, Supplier<Recommender<U,U>>> recs = new HashMap<>();
        
        List<Double> rs = grid.getDoubleValues(R);
        rs.forEach(r -> recs.put(PERSPAGERANK + "_" + r, () -> new PersonalizedPageRank<>(graph, r)));
        return recs;
    }
    
}
