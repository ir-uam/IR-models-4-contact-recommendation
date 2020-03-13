/*
 * Copyright (C) 2020 Information Retrieval Group at Universidad Autónoma
 * de Madrid, http://ir.ii.uam.es and Terrier Team at University of Glasgow,
 * http://terrierteam.dcs.gla.ac.uk/.
 *
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.irmodels4contactrec.main.grid;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.List;

/**
 * Reads configurations.
 *
 * @author Javier Sanz-Cruzado (javier.sanz-cruzado@uam.es)
 * @author Craig Macdonald (craig.macdonald@glasgow.ac.uk)
 * @author Iadh Ounis (iadh.ounis@glasgow.ac.uk)
 * @author Pablo Castells (pablo.castells@uam.es)
 */
public abstract class ConfigurationsReader
{
    /**
     * Identifier for the configuration.
     */
    private final static String CONFIGURATION = "configuration";
    /**
     * Identifier for the parameters.
     */
    private final static String PARAMS = "params";
    /**
     * Identifier for the individual parameters.
     */
    private final static String PARAM = "param";

    /**
     * Reads the possible values for the parameters of an algorithm.
     *
     * @param configurations XML nodes containing the configurations information
     * @param paramReader    a parameter reader.
     *
     * @return the list of configurations.
     */
    protected Configurations readConfigurationGrid(NodeList configurations, ParametersReader paramReader)
    {
        List<Parameters> configs = new ArrayList<>();

        for (int i = 0; i < configurations.getLength(); ++i)
        {
            Element element = (Element) configurations.item(i);
            NodeList parametersNodes = element.getElementsByTagName(PARAMS);
            if (parametersNodes == null || parametersNodes.getLength() == 0)
            {
                configs.add(new Parameters());
            }
            else
            {
                Element parametersNode = (Element) parametersNodes.item(0);
                NodeList parameters = parametersNodes.item(0).getChildNodes();


                Parameters params = paramReader.readParameterGrid(parameters);
                configs.add(params);
            }
        }

        return new Configurations(configs);
    }
}
