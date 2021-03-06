/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package es.uam.eps.ir.irmodels4contactrec.recommender.knn.similarities.ir;

import es.uam.eps.ir.irmodels4contactrec.graph.edges.EdgeOrientation;
import es.uam.eps.ir.irmodels4contactrec.graph.fast.FastGraph;
import es.uam.eps.ir.irmodels4contactrec.recommender.knn.similarities.GraphSimilarity;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.function.IntToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.ranksys.core.util.tuples.Tuple2id;

/**
 *
 * @author Javier Sanz-Cruzado Puig (javier.sanz-cruzado@uam.es)
 */
public class BM25Similarity extends GraphSimilarity
{
    /**
     * Parameter that tunes the effect of the neighborhood size. Between 0 and 1.
     */
    private final double b;
    /**
     * Parameter of the algorithm that tunes the effect of the weight
     */
    private final double k;
    /**
     * Neighborhood selection for the target user.
     */
    private final EdgeOrientation uSel;
    /**
     * Neighborhood selection for the candidate user.
     */
    private final EdgeOrientation vSel;
    /**
     * Neighborhood selection for the document length.
     */
    private final EdgeOrientation dlSel;
    /**
     * Average neighborhood size.
     */
    private final double avgSize;
    /**
     * Number of users.
     */
    private final long numUsers;
    /**
     * Robertson-Sparck-Jones values for the different users.
     */
    private final Int2DoubleMap rsj;
    /**
     * Individual neighborhood sizes.
     */
    private final Int2DoubleMap size;
    
    /**
     * Constructor.
     * @param graph the training graph.
     * @param uSel neighborhood selection for the target user.
     * @param vSel neighborhood selection for the candidate user.
     * @param dlSel neighborhood selection for the document length
     * @param b parameter that tunes the effect of the neighborhood size. Between 0 and 1.
     * @param k parameter of the algorithm that tunes the effect of the weight.
     */
    public BM25Similarity(FastGraph<?> graph, EdgeOrientation uSel, EdgeOrientation vSel, EdgeOrientation dlSel, double b, double k)
    {
        super(graph);
        this.b = b;
        this.k = k;
        this.uSel = uSel;
        this.vSel = vSel.invertSelection();
        this.dlSel = dlSel;
        this.numUsers = this.graph.getVertexCount();
        
        this.rsj = new Int2DoubleOpenHashMap();
        this.size = new Int2DoubleOpenHashMap();
        this.avgSize = this.graph.getAllNodesIds().mapToDouble(uidx -> {
           double rsjV = graph.getNeighborhood(uidx, this.vSel).count();
           rsjV = Math.log((numUsers - rsjV + 0.5)/(rsjV + 0.5));
           this.rsj.put(uidx, rsjV);
           
           double val = graph.getNeighborhoodWeights(uidx, dlSel).mapToDouble(widx -> widx.v2).sum();
           this.size.put(uidx, val);
           return val;
        }).average().getAsDouble();
        
        
    }
    @Override
    public IntToDoubleFunction similarity(int idx)
    {
        IntSet uNeigh = this.graph.getNeighborhood(idx, uSel).collect(Collectors.toCollection(IntOpenHashSet::new));
        
        return (idx2) ->
        {
            double s = this.size.get(idx2);
            double den = ((Double.isFinite(k)) ? this.k : 1.0)*(1-b + (b*s/avgSize));
            return graph.getNeighborhoodWeights(idx, vSel.invertSelection()).filter(widx -> uNeigh.contains(widx.v1)).mapToDouble(widx ->
            {
                double weight = widx.v2;
                double rsjW = this.rsj.get(widx.v1);
                double num = ((Double.isFinite(k)) ? this.k + 1.0 : 1.0)*weight*rsjW;
                
                return num/(den + (Double.isFinite(k) ? weight : 0.0));
            }).sum();
        };
    }

    @Override
    public Stream<Tuple2id> similarElems(int idx)
    {
        Int2DoubleOpenHashMap sims = new Int2DoubleOpenHashMap();
        sims.defaultReturnValue(0.0);
        
        if(Double.isFinite(this.k))
        {
            graph.getNeighborhood(idx, uSel).forEach(widx -> 
            {
                double rsjW = this.rsj.get(widx.intValue());
                graph.getNeighborhoodWeights(widx, vSel).filter(vidx -> vidx.v1 != idx).forEach(vidx -> 
                {
                    double weight = vidx.v2;
                    double s = this.size.get(vidx.v1);
                    double num = (this.k + 1.0)*weight*rsjW;
                    double den = this.k*(1-b+(b*s/avgSize)) + weight;
                    
                    sims.addTo(vidx.v1, num/den);
                });
            });
        }
        else
        {
            graph.getNeighborhood(idx, uSel).forEach(widx -> 
            {
                double rsjW = this.rsj.get(widx.intValue());
                graph.getNeighborhoodWeights(widx, vSel).filter(vidx -> vidx.v1 != idx).forEach(vidx -> 
                {
                    double weight = vidx.v2;
                    double s = this.size.get(vidx.v1);
                    
                    double num = weight*rsjW;
                    double den = 1-b+(b*s/avgSize);
                    
                    sims.addTo(vidx.v1, num/den);
                });
            });
        }
        
        return sims.int2DoubleEntrySet().stream().map(x -> new Tuple2id(x.getIntKey(), x.getDoubleValue()));
    }

}
