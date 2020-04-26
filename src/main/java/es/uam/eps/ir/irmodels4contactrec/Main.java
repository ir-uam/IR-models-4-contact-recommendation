/*
 * Copyright (C) 2020 Information Retrieval Group at Universidad Autónoma
 * de Madrid, http://ir.ii.uam.es and Terrier Team at University of Glasgow,
 * http://terrierteam.dcs.gla.ac.uk/.
 *
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.irmodels4contactrec;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

/**
 * Main class for running experiments.
 *
 * @author Javier Sanz-Cruzado (javier.sanz-cruzado@uam.es)
 * @author Craig Macdonald (craig.macdonald@glasgow.ac.uk)
 * @author Iadh Ounis (iadh.ounis@glasgow.ac.uk)
 * @author Pablo Castells (pablo.castells@uam.es)
 */
public class Main
{
    /**
     * Name for the program used for validation
     */
    private final static String VALID = "validation";
    /**
     * Name for the program used for comparing accuracy vs. degree.
     */
    private final static String EVAL = "evaluation";
    /**
     * Name for the program used for running statistical tests:
     */
    private final static String STATISTICS = "stats";

    private final static String TUKEY = "tukey";

    /**
     * Main method. Executes the main method in the class specified by the first
     * argument with the rest of run time arguments.
     *
     * @param args Arguments to select the class to run and arguments for its main method
     */
    public static void main(String[] args)
    {
        try
        {
            String main = args[0];
            String className;
            int from = 1;
            switch (main)
            {
                case VALID:
                    className = "es.uam.eps.ir.irmodels4contactrec.main.Validation";
                    break;
                case EVAL:
                    className = "es.uam.eps.ir.irmodels4contactrec.main.Evaluation";
                    break;
                case STATISTICS:
                    className = "es.uam.eps.ir.irmodels4contactrec.main.StatisticalSignificance";
                    break;
                case TUKEY:
                    className = "es.uam.eps.ir.irmodels4contactrec.main.TukeyTestFileGenerator";
                    break;
                default:
                    System.err.println("ERROR: Unknown program.");
                    return;
            }

            String[] executionArgs = Arrays.copyOfRange(args, from, args.length);
            @SuppressWarnings("rawtypes") Class[] argTypes = {executionArgs.getClass()};
            Object[] passedArgs = {executionArgs};
            Class.forName(className).getMethod("main", argTypes).invoke(null, passedArgs);
        }
        catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex)
        {
            System.err.println("The run time arguments were not correct");
            ex.printStackTrace();
        }
    }
}
