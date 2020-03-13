/*
 * Copyright (C) 2020 Information Retrieval Group at Universidad Autónoma
 * de Madrid, http://ir.ii.uam.es and Terrier Team at University of Glasgow,
 * http://terrierteam.dcs.gla.ac.uk/.
 *
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.irmodels4contactrec.main.grid.standalone.baselines;

import es.uam.eps.ir.irmodels4contactrec.graph.edges.EdgeOrientation;
import es.uam.eps.ir.irmodels4contactrec.graph.fast.FastGraph;
import es.uam.eps.ir.irmodels4contactrec.recommender.standalone.basic.Popularity;
import es.uam.eps.ir.irmodels4contactrec.main.grid.AlgorithmGridSearch;
import es.uam.eps.ir.irmodels4contactrec.main.grid.Grid;
import es.uam.eps.ir.irmodels4contactrec.main.grid.RecommendationAlgorithmFunction;
import es.uam.eps.ir.ranksys.fast.preference.FastPreferenceData;
import es.uam.eps.ir.ranksys.rec.Recommender;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static es.uam.eps.ir.irmodels4contactrec.main.grid.AlgorithmIdentifiers.POP;

/**
 * Grid search generator for Popularity algorithm.
 *
 * @see es.uam.eps.ir.irmodels4contactrec.recommender.standalone.basic.Popularity
 *
 * @param <U> Type of the users.
 *
 * @author Javier Sanz-Cruzado (javier.sanz-cruzado@uam.es)
 * @author Craig Macdonald (craig.macdonald@glasgow.ac.uk)
 * @author Iadh Ounis (iadh.ounis@glasgow.ac.uk)
 * @author Pablo Castells (pablo.castells@uam.es)
 */
public class PopularityGridSearch<U> implements AlgorithmGridSearch<U>
{
    @Override
    public Map<String, RecommendationAlgorithmFunction<U>> grid(Grid grid)
    {
        Map<String, RecommendationAlgorithmFunction<U>> recs = new HashMap<>();
        RecommendationAlgorithmFunction<U> bifunction = (graph, prefdata) -> new Popularity<>(graph, EdgeOrientation.IN);
        recs.put(POP, bifunction);
        return recs;
    }

    @Override
    public Map<String, Supplier<Recommender<U, U>>> grid(Grid grid, FastGraph<U> graph, FastPreferenceData<U, U> prefData)
    {
        Map<String, Supplier<Recommender<U, U>>> recs = new HashMap<>();
        recs.put(POP, () -> new Popularity<>(graph, EdgeOrientation.IN));
        return recs;
    }
}
