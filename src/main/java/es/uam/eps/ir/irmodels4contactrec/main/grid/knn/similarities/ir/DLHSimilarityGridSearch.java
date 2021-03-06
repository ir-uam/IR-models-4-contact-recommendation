/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package es.uam.eps.ir.irmodels4contactrec.main.grid.knn.similarities.ir;

import es.uam.eps.ir.irmodels4contactrec.graph.edges.EdgeOrientation;
import es.uam.eps.ir.irmodels4contactrec.graph.fast.FastGraph;
import es.uam.eps.ir.irmodels4contactrec.main.grid.Grid;
import es.uam.eps.ir.irmodels4contactrec.main.grid.knn.similarities.SimilarityFunction;
import es.uam.eps.ir.irmodels4contactrec.main.grid.knn.similarities.SimilarityGridSearch;
import es.uam.eps.ir.irmodels4contactrec.recommender.knn.similarities.ir.DLHSimilarity;
import es.uam.eps.ir.ranksys.fast.preference.FastPreferenceData;
import es.uam.eps.ir.ranksys.nn.sim.Similarity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static es.uam.eps.ir.irmodels4contactrec.main.grid.knn.similarities.SimilarityIdentifiers.DLH;

/**
 * Grid search for the vector cosine similarity.
 * @author Javier Sanz-Cruzado Puig (javier.sanz-cruzado@uam.es)
 * @param <U> Type of the users.
 */
public class DLHSimilarityGridSearch<U> implements SimilarityGridSearch<U>
{
    /**
     * Identifier for the selection of neighbors for the target user
     */
    private final String USEL = "uSel";
    /**
     * Identifier for the selection of neighbors for the neighbor user.
     */
    private final String VSEL = "vSel";
    
    @Override
    public Map<String, SimilarityFunction<U>> grid(Grid grid)
    {
        Map<String, SimilarityFunction<U>> sims = new HashMap<>();
        
        List<EdgeOrientation> uSels = grid.getOrientationValues(USEL);
        List<EdgeOrientation> vSels = grid.getOrientationValues(VSEL);
        if(uSels == null || uSels.isEmpty() || vSels == null || vSels.isEmpty())
        {
            return sims;
        }
        
        uSels.forEach(uSel ->
            vSels.forEach(vSel ->
                sims.put(DLH + "_" + uSel + "_" + vSel, (FastGraph<U> graph, FastPreferenceData<U,U> prefData) ->
                   new DLHSimilarity(graph, uSel, vSel))));
        
        return sims;
    }

    @Override
    public Map<String, Supplier<Similarity>> grid(Grid grid, FastGraph<U> graph, FastPreferenceData<U, U> prefData)
    {
        Map<String, Supplier<Similarity>> sims = new HashMap<>();
        
        List<EdgeOrientation> uSels = grid.getOrientationValues(USEL);
        List<EdgeOrientation> vSels = grid.getOrientationValues(VSEL);
        if(uSels == null || uSels.isEmpty() || vSels == null || vSels.isEmpty())
        {
            return sims;
        }

        uSels.forEach(uSel ->
            vSels.forEach(vSel ->
                sims.put(DLH + "_" + uSel + "_" + vSel, () ->
                    new DLHSimilarity(graph, uSel, vSel))));
        
        return sims;
    }
    
}
