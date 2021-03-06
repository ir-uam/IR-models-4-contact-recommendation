/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.ir.irmodels4contactrec.main.grid.sampling;

import es.uam.eps.ir.irmodels4contactrec.main.grid.Parameters;
import es.uam.eps.ir.irmodels4contactrec.main.grid.ParametersReader;
import java.util.*;
import java.io.*;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Reads the grids for several partition algorithms.
 * @author Javier Sanz-Cruzado Puig
 */
public class IndividualSamplingAlgorithmGridReader extends ParametersReader
{
    /**
     * Algorithms grid. Uses a grid for each algorithm.
     */
    private final Map<String, Parameters> partitionsGrid;
    
    /**
     * Constructor 
     */
    public IndividualSamplingAlgorithmGridReader()
    {
        this.partitionsGrid = new HashMap<>();
    }
    
    /**
     * Reads a XML document containing a grid
     * @param file the XML file
     */
    public void readDocument(String file)
    {
        try
        {
            this.partitionsGrid.clear();
            // First of all, obtain the XML Document
            File inputFile = new File(file);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            
            Element parent = doc.getDocumentElement();
            parent.normalize();
            
            NodeList nodeList = parent.getChildNodes();
            for(int i = 0; i < nodeList.getLength(); ++i)
            {
                Node node = nodeList.item(i);
                if(node.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element element = (Element) node;
                    this.readIndividualSamplingAlgorithm(element);
                }
            }
            
        } catch (ParserConfigurationException | SAXException | IOException ex)
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * Reads the grid for a single algorithm.
     * @param element The XML Element containing the algorithm information
     */
    private void readIndividualSamplingAlgorithm(Element element)
    {
        String algorithmName = element.getElementsByTagName("name").item(0).getTextContent();
        NodeList parametersNodes = element.getElementsByTagName("params");
        if(parametersNodes == null || parametersNodes.getLength() == 0)
        {
            this.partitionsGrid.put(algorithmName, new Parameters());
        }
        else
        {
            Element parametersNode = (Element) parametersNodes.item(0);
            NodeList parameters = parametersNode.getElementsByTagName("param");
            Parameters g = readParameterGrid(parameters);
            this.partitionsGrid.put(algorithmName, g);
        }
    }

    /**
     * Gets the set of algorithms previously read.
     * @return The set of algorithms previously read from the grid file.
     */
    public Set<String> getIndividualSamplingAlgorithms()
    {
        return this.partitionsGrid.keySet();
    }
    
    /**
     * Gets the grid for a given algorithm
     * @param algorithm The algorithm to search
     * @return The grid if exists, an empty grid if not.
     */
    public Parameters getParameters(String algorithm)
    {
        return this.partitionsGrid.getOrDefault(algorithm, new Parameters());
    }
}
