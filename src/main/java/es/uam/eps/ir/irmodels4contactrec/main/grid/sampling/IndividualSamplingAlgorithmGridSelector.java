/*
 *  Copyright (C) 2019 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.irmodels4contactrec.main.grid.sampling;


import es.uam.eps.ir.irmodels4contactrec.graph.fast.FastGraph;
import es.uam.eps.ir.irmodels4contactrec.letor.sampling.IndividualSampler;
import es.uam.eps.ir.irmodels4contactrec.main.grid.Parameters;
import es.uam.eps.ir.irmodels4contactrec.utils.Tuple2oo;
import es.uam.eps.ir.ranksys.fast.preference.FastPreferenceData;

/**
 * Class that translates from a grid to the different train/test partition algorithns.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class IndividualSamplingAlgorithmGridSelector<U>
{   
    /**
     * Obtains a configured individual sampling algorithm.
     * @param name the name of the algorithm.
     * @param param the parameters of the algorithm.
     * @param graph the graph to sample.
     * @param extraEdges a graph useful for the sample.
     * @param prefData preference data representing the graph.
     * @return a pair containing the name of the algorithm and its configured object.
     */
    public Tuple2oo<String, IndividualSampler<U>> getIndividualSamplingAlgorithm(String name, Parameters param, FastGraph<U> graph, FastGraph<U> extraEdges, FastPreferenceData<U,U> prefData)
    {
        IndividualSamplingAlgorithmConfigurator<U> configurator = this.getConfigurator(name);
        if(configurator != null)
        {
            return configurator.grid(param, graph, graph, prefData);
        }
        return null;
    }
    
    /**
     * Obtains a function for obtaining a configured individual sampling algorithm.
     * @param name the name of the algorithm.
     * @param param the parameters of the algorithm.
     * @return a pair containing the name of the algorithm and a function to retrieve
     * a configured sampler.
     */
    public Tuple2oo<String, IndividualSamplerFunction<U>> getIndividualSamplingAlgorithm(String name, Parameters param)
    {
        IndividualSamplingAlgorithmConfigurator<U> configurator = this.getConfigurator(name);
        if(configurator != null)
        {
            return configurator.grid(param);
        }
        return null;
    }
    
    /**
     * Obtains a configurator for an individual sampling algorithm.
     * @param name the name of the algorithm.
     * @return the configurator if exists, null otherwise.
     */
    public IndividualSamplingAlgorithmConfigurator<U> getConfigurator(String name)
    {
        IndividualSamplingAlgorithmConfigurator<U> gridSearch;
        
        switch(name)
        {
            case IndividualSamplingAlgorithmIdentifiers.DISTANCETWO:
                gridSearch = new DistanceTwoIndividualSamplerConfigurator<>();
                break;
            case IndividualSamplingAlgorithmIdentifiers.DISTANCETWOLP:
                gridSearch = new DistanceTwoLinkPredictionIndividualSamplerConfigurator<>();
                break;
            case IndividualSamplingAlgorithmIdentifiers.RECOMMENDER:
                gridSearch = new RecommenderIndividualSamplerConfigurator<>();
                break;
            default:
                gridSearch = null;
        }
        
        return gridSearch;
    }
}
