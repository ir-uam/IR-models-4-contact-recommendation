/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.irmodels4contactrec.recommender.standalone.randomwalk;

import es.uam.eps.ir.irmodels4contactrec.graph.fast.FastGraph;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.util.HashMap;
import java.util.Map;

/**
 * General personalized SALSA recommender.
 * 
 * Goel, A. et al. The who-to-follow system at Twitter: Strategy, algorithms and revenue impact. Interfaces 45(1), 2015, pp. 98-107
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class Money<U> extends BipartiteRecommender<U>
{

    /**
     * Teleport rate.
     */
    private final double alpha;
    /**
     * Convergence threshold.
     */
    private final static double THRESHOLD = 0.01;

    /**
     * Constructor.
     * @param graph User graph.
     * @param mode True if the recommendation scores are the authorities scores, false if the recommendation scores are the hubs scores
     * @param alpha Teleport probability.
     */
    public Money(FastGraph<U> graph, boolean mode, double alpha)
    {
        super(graph, mode);
        this.alpha = alpha;

    }

    @Override
    public Int2DoubleMap getScoresMap(int i) 
    {
        U u = uIndex.uidx2user(i);
        Int2DoubleMap scores = new Int2DoubleOpenHashMap();
        Map<U, Double> hubsMap = new HashMap<>();
        Map<U, Double> authMap = new HashMap<>();
        
        this.hubs.forEach((key, value) -> {
            if (value.equals(u))
                hubsMap.put(value, 1.0);
            else
                hubsMap.put(value, 0.0);
        });
        
        this.authorities.forEach((key, value) -> authMap.put(value, 1.0 / (this.authorities.size())));
        
        double diff;
        do // Compute Personalized SALSA
        {
            diff = this.authorities.entrySet().stream().mapToDouble(entry -> 
            {
                long bIdx = entry.getKey();
                U user = entry.getValue();
                
                double newAuthScore = this.bipartiteGraph.getIncidentNodes(bIdx)
                        .mapToDouble(wIdx -> hubsMap.get(this.hubs.get(wIdx))/this.bipartiteGraph.outDegree(wIdx))
                        .sum();
                double old = authMap.get(user);
                authMap.put(user, newAuthScore);
                return Math.abs(old - newAuthScore);
            }).sum();
            
            diff += this.hubs.entrySet().stream().mapToDouble(entry -> 
            {
                long bIdx = entry.getKey();
                U user = entry.getValue();
                
                double newHubScore = this.bipartiteGraph.getAdjacentNodes(bIdx)
                        .mapToDouble(wIdx -> authMap.get(this.authorities.get(wIdx))/this.bipartiteGraph.inDegree(wIdx))
                        .sum();
                newHubScore *= (1-this.alpha);
                
                if(user.equals(u))
                {
                    newHubScore += this.alpha;
                }
                
                double old = hubsMap.get(user);
                hubsMap.put(user, newHubScore);
                
                return Math.abs(old - newHubScore);
            }).sum();
        }
        while(diff > THRESHOLD);
        
        if(this.mode) //Authorities
        {
            this.uIndex.getAllUsers().forEach(v -> 
            {
                int vIdx = this.uIndex.user2uidx(v);
                if(authMap.containsKey(v))
                {
                    scores.put(vIdx, authMap.get(v).doubleValue());
                }
                else
                {
                    scores.put(vIdx,0.0);
                }
            });
        }
        else // Hubs
        {
            this.uIndex.getAllUsers().forEach(v -> 
            {
                int vIdx = this.uIndex.user2uidx(v);
                if(hubsMap.containsKey(v))
                {
                    scores.put(vIdx, hubsMap.get(v).doubleValue());
                }
                else
                {
                    scores.put(vIdx,0.0);
                }
            });
        }
            
            
        return scores;
        
    }    
}
