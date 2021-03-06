/*
 * Copyright (C) 2019 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.irmodels4contactrec.recommender.knn.similarities.ir;

import es.uam.eps.ir.irmodels4contactrec.graph.edges.EdgeOrientation;
import es.uam.eps.ir.irmodels4contactrec.graph.fast.FastGraph;
import es.uam.eps.ir.irmodels4contactrec.data.TerrierStructure;
import java.util.Optional;

/**
 *
 * @author Javier
 */
public class DPHSimilarity extends TerrierIRSimilarity
{    
    public DPHSimilarity(FastGraph<?> graph, EdgeOrientation uSel, EdgeOrientation vSel)
    {
        super(graph, uSel, vSel);
    }

    /**
     * Constructor.
     * @param graph the training graph.
     * @param uSel orientation selection for the target user.
     * @param vSel orientation selection for the candidate user.
     * @param structure Terrier basic structures for the algorithm.
     */
    public DPHSimilarity(FastGraph<?> graph, EdgeOrientation uSel, EdgeOrientation vSel, TerrierStructure structure)
    {
        super(graph,uSel,vSel,structure);
    }
    
    @Override
    protected String getModel() {
        return "DPH";
    }

    @Override
    protected Optional<Double> getCValue() 
    {
        return Optional.empty();
    }
    
}
