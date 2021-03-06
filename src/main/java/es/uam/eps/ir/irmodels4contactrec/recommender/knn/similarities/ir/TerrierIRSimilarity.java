package es.uam.eps.ir.irmodels4contactrec.recommender.knn.similarities.ir;

import es.uam.eps.ir.irmodels4contactrec.graph.edges.EdgeOrientation;
import es.uam.eps.ir.irmodels4contactrec.graph.fast.FastGraph;
import java.util.function.IntToDoubleFunction;
import java.util.stream.Stream;


import es.uam.eps.ir.irmodels4contactrec.recommender.knn.similarities.GraphSimilarity;
import es.uam.eps.ir.irmodels4contactrec.utils.Tuple2oo;
import es.uam.eps.ir.irmodels4contactrec.data.TerrierIndex;
import es.uam.eps.ir.irmodels4contactrec.data.TerrierStructure;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.ranksys.core.util.tuples.Tuple2id;
import org.ranksys.formats.parsing.Parsers;
import org.terrier.querying.ScoredDocList;
import org.terrier.realtime.memory.MemoryIndex;

/**
 * Class that uses the Terrier IR engine to generate similarities between elements.
 * @author Javier Sanz-Cruzado Puig
 */
public abstract class TerrierIRSimilarity extends GraphSimilarity 
{
    /**
     * Terrier RAM index.
     */
    private final TerrierIndex index;
    /**
     * Edge orientation for the target users.
     */
    private final EdgeOrientation uSel;
    /**
     * Edge orientation for the candidate users.
     */
    private final EdgeOrientation vSel;  
    
    /**
     * Constructor.
     * @param graph the graph.
     * @param uSel orientation for the first user.
     * @param vSel orientation for the second user.
     */
    public TerrierIRSimilarity(FastGraph<?> graph, EdgeOrientation uSel, EdgeOrientation vSel) 
    {
        super(graph);
        this.uSel = uSel;
        this.vSel = vSel;
        this.index = new TerrierIndex(graph, uSel, vSel);
    }
    /**
     * Constructor.
     * @param graph the training graph.
     * @param uSel orientation selection for the target user.
     * @param vSel orientation selection for the candidate user.
     * @param structure Terrier basic structures for the algorithm.
     */
    public TerrierIRSimilarity(FastGraph<?> graph, EdgeOrientation uSel, EdgeOrientation vSel, TerrierStructure structure)
    {
        super(graph);
        this.uSel = uSel;
        this.vSel = vSel;
        Tuple2oo<MemoryIndex,Map<Integer,String>> pair = structure.get(uSel, vSel);
        this.index = new TerrierIndex(graph, uSel, vSel, pair.v1(), pair.v2());
    }

    @Override
    public IntToDoubleFunction similarity(int idx) 
    {
        Int2DoubleMap map = new Int2DoubleOpenHashMap();
        String weightingModel = this.getModel();
        Optional<Double> cvalue = this.getCValue();
        ScoredDocList results = this.index.query(idx, weightingModel, cvalue);
        if(results == null)
            return (int idx2) -> 0.0;
        else
        {
            results.forEach(r -> 
            {
                int idx2 = Parsers.ip.parse(r.getMetadata(TerrierIndex.NODEID));
                double sim = r.getScore();
                map.put(idx2, sim);
            });
        }
        
        return (int idx2) -> map.getOrDefault(idx2, 0.0);
    }

    @Override
    public Stream<Tuple2id> similarElems(int idx) 
    {
        String weightingModel = this.getModel();
        Optional<Double> cvalue = this.getCValue();
        ScoredDocList results = this.index.query(idx, weightingModel, cvalue);
        
        if(results == null) return Stream.empty();
        
        List<Tuple2id> tuples = new ArrayList<>();
        results.forEach(r -> 
        {
            String meta = r.getMetadata(TerrierIndex.NODEID);
            int idx2 = Parsers.ip.parse(meta);
            if(idx != idx2)
            {
                tuples.add(new Tuple2id(idx2, r.getScore()));
            }
        });
        
        return tuples.stream();
    }
    
    /**
     * Obtains the name of the weighting model from Terrier.
     * @return the name of the weighting model.
     */
    protected abstract String getModel();

    /**
     * Obtains the name of the c value for the model. If it is null,
     * it will take just the default parameter.
     * @return the value if it exists, or an empty object otherwise.
     */
    protected abstract Optional<Double> getCValue();
    
}
