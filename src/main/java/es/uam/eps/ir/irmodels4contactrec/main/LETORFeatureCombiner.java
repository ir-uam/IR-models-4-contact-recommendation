/*
 * Copyright (C) 2019 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.irmodels4contactrec.main;

import es.uam.eps.ir.irmodels4contactrec.letor.InstanceSet;
import es.uam.eps.ir.irmodels4contactrec.letor.InstanceSetCombiner;
import es.uam.eps.ir.irmodels4contactrec.letor.io.InstanceSetReader;
import es.uam.eps.ir.irmodels4contactrec.letor.io.InstanceSetWriter;
import es.uam.eps.ir.irmodels4contactrec.letor.io.LETORInstanceReader;
import es.uam.eps.ir.irmodels4contactrec.letor.io.LETORInstanceWriter;
import org.ranksys.formats.parsing.Parsers;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for normalizing the elements in an instance set.
 * @author Javier Sanz-Cruzado
 */
public class LETORFeatureCombiner
{
    /**
     * Program that combines two different instance sets into one.
     * @param args Execution arguments:
     * <ol>
     *   <li><b>First:</b>The first instance set</li>
     *   <li><b>Second:</b>The second instance set</li>
     *   <li><b>First indexes:</b> Comma separated list of the indexes to keep from the first instance set</li>
     *   <li><b>Second indexes:</b> Comma separated list of the indexes to keep from the second instance set</li>
     *   <li><b>Output:</b> File in which to store the combined set</li>
     *   <li><b>Descr. output:</b> File in which to store the description</li>
     * </ol>
     * @throws IOException if some error occurs while reading/writing
     */
    public static void main(String[] args) throws IOException
    {
        if (args.length < 5)
        {
            System.err.println("ERROR: Invalid arguments");
            System.err.println("\tfirst: the first instance set");
            System.err.println("\tsecond: the second instance set");
            System.err.println("\tfirst indexes: comma separated list of the indexes to keep from the first instance set");
            System.err.println("\tsecond indexes: comma separated list of the indexes to keep from the second instance set");
            System.err.println("\toutput: file in which to store the combined set");
        }

        String first = args[0];
        String second = args[1];

        // Retrieve the indexes to use for the first set.
        List<Integer> firstIndexes = new ArrayList<>();
        String firstList = args[2];
        String[] split = firstList.split(",");
        for (String idx : split) firstIndexes.add(Parsers.ip.parse(idx));

        // Retrieve the indexes to use for the second set.
        List<Integer> secondIndexes = new ArrayList<>();
        String secondList = args[3];
        split = secondList.split(",");
        for (String idx : split) secondIndexes.add(Parsers.ip.parse(idx));

        boolean printdescr = false;
        String descr = "";
        String output = args[4];
        if (args.length > 5)
        {
            printdescr = true;
            descr = args[5];
        }

        long a = System.currentTimeMillis();

        // First, read both datasets:
        InstanceSetReader<Long> reader = new LETORInstanceReader<>(Parsers.lp);
        InstanceSet<Long> firstSet = reader.read(first);
        long b = System.currentTimeMillis();
        System.out.println("First dataset read (" + (b - a) / 1000.0 + " s.)");
        if(firstIndexes.get(0).equals(-1))
        {
            firstIndexes.clear();
            for(int i = 0; i < firstSet.getFeatInfo().numFeats(); ++i)
            {
                firstIndexes.add(i);
            }
        }



        InstanceSet<Long> secondSet = reader.read(second);
        b = System.currentTimeMillis();
        System.out.println("Second dataset read (" + (b - a) / 1000.0 + " s.)");
        if(secondIndexes.get(0).equals(-1))
        {
            secondIndexes.clear();
            for(int i = 0; i < secondSet.getFeatInfo().numFeats(); ++i)
            {
                secondIndexes.add(i);
            }
        }
        // Combines the datasets.
        InstanceSetCombiner<Long> combiner = new InstanceSetCombiner<>();
        InstanceSet<Long> combined = combiner.combine(firstSet, firstIndexes, secondSet, secondIndexes);
        b = System.currentTimeMillis();
        System.out.println("Combined datasets (" + (b - a) / 1000.0 + " s.)");

        // Writes the dataset.
        InstanceSetWriter<Long> writer = new LETORInstanceWriter<>();
        writer.write(output, combined);

        // Prints the description of the dataset.
        if (printdescr)
        {
            String featInfoString = writer.writeFeatureInfo(combined.getFeatInfo());
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(descr))))
            {
                bw.write("First dataset:" + first);
                bw.write("\nSecond dataset: " + second);
                bw.write("\n" + featInfoString);
            }
        }

        b = System.currentTimeMillis();
        System.out.println("Finished (" + (b - a) / 1000.0 + " s.)");
    }
}
