/*
 * Copyright (C) 2019 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.irmodels4contactrec.letor.normalization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import es.uam.eps.ir.ranksys.core.Recommendation;
import es.uam.eps.ir.ranksys.fast.FastRecommendation;
import org.ranksys.core.util.tuples.Tuple2id;
import org.ranksys.core.util.tuples.Tuple2od;

/**
 * Applies the ranksim normalization: ranking-based normalization.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the items.
 */
public class RanksimNormalizer<U,I> implements FastNormalizer<U,I> 
{
    @Override
    public List<Double> normalize(List<Double> values)
    {
        List<Integer> indexes = new ArrayList<>();
        List<Double> newValues = new ArrayList<>();
        int length = values.size();
        for (int i = 0; i < length; ++i) {
            indexes.add(i);
            newValues.add(0.0);
        }

        indexes.sort(Comparator.comparing(values::get));
        for (int i = 0; i < length; ++i) {
            int j = indexes.get(i);
            newValues.set(j, (length - i + 0.0) / (length + 0.0));
        }

        return newValues;
    }

    @Override
    public FastRecommendation normalize(FastRecommendation recommendation)
    {
        int uidx = recommendation.getUidx();
        List<Tuple2id> iidxs = recommendation.getIidxs();
        int length = iidxs.size();
        List<Tuple2id> newIidxs = new ArrayList<>();
        
        for(int i = 0; i < length; ++i)
        {
            newIidxs.add(new Tuple2id(iidxs.get(i).v1(), (length - i + 0.0)/(length + 0.0)));
        }

        return new FastRecommendation(uidx, newIidxs);
    }

    @Override
    public Recommendation<U,I> normalize(Recommendation<U,I> recommendation)
    {
        U u = recommendation.getUser();
        List<Tuple2od<I>> items = recommendation.getItems();
        int length = items.size();
        List<Tuple2od<I>> newItems = new ArrayList<>();

        for (int i = 0; i < length; ++i) {
            newItems.add(new Tuple2od<>(items.get(i).v1(), (length - i + 0.0) / (length + 0.0)));
        }
        return new Recommendation<>(u, items);
    }
}
