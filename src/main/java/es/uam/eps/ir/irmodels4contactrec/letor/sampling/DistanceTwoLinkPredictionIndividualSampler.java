/*
 * Copyright (C) 2019 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.irmodels4contactrec.letor.sampling;

import es.uam.eps.ir.irmodels4contactrec.graph.Graph;
import es.uam.eps.ir.irmodels4contactrec.graph.edges.EdgeOrientation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Samples all the links created at distance two from the user in a test graph,
 * and the same amount of links at distance two which have not been created.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class DistanceTwoLinkPredictionIndividualSampler<U> extends AbstractIndividualSampler<U>
{
    /**
     * Edge orientation for the neighbors of the origin node.
     */
    private final EdgeOrientation uSel;
    /**
     * Edge orientation for the neighbors of the destination nodes.
     */
    private final EdgeOrientation vSel;
    /**
     * The graph to check the positive / negative examples
     */
    private final Graph<U> testGraph;
    
    /**
     * Constructor.
     * @param graph the graph.
     * @param uSel edge orientation for the neighbors of the origin node.
     * @param vSel edge orientation for the neighbors of the selected nodes.
     */
    public DistanceTwoLinkPredictionIndividualSampler(Graph<U> graph, Graph<U> testGraph, EdgeOrientation uSel, EdgeOrientation vSel)
    {
        super(graph);
        this.uSel = uSel;
        this.vSel = vSel;
        this.testGraph = testGraph;
    }
    
    @Override
    public Set<U> sampleUsers(U u, Predicate<U> filter) 
    {
        Set<U> sample = new HashSet<>();
        Set<U> negativeSamples = new HashSet<>();
        this.graph.getNeighbourhood(u, uSel).forEach(v -> 
        {
            if(!u.equals(v))
                this.graph.getNeighbourhood(v, vSel.invertSelection()).forEach(w -> 
                {
                    if(!u.equals(w) && testGraph.containsEdge(u,w) && filter.test(w))
                        sample.add(w);
                    else if(!u.equals(w) && filter.test(w))
                        negativeSamples.add(w);
                });
        });
        
        int k = Math.min(sample.size(), negativeSamples.size());
        List<U> neg = new ArrayList<>(negativeSamples);
        Collections.shuffle(neg);
        
        for(int i = 0; i < k; ++i)
        {
            sample.add(neg.get(i));
        }
        return sample;
    }
    
}
