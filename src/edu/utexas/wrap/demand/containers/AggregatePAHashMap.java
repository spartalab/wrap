package edu.utexas.wrap.demand.containers;

import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;

import java.util.Map;
import java.util.Set;

/**
 * @author Rishabh
 *
 * An aggregate production/attraction map implementation
 * Created in the trip generation process, this object
 * stores the total trips produced and attracted by a specified Node
 */
public class AggregatePAHashMap implements PAMap {

    private Graph g;
    private Map<Node, Float> attractors;
    private Map<Node, Float> producers;
    private float vot;

    public AggregatePAHashMap(Graph graph) {
        this.g = graph;
        attractors = new Object2FloatOpenHashMap<Node>();
        producers = new Object2FloatOpenHashMap<Node>();
    }
    
    /* (non-Javadoc)
     * @see edu.utexas.wrap.demand.PAMap#getProducers()
     */
    @Override
    public Set<Node> getProducers() {
        return attractors.keySet();
    }

    /* (non-Javadoc)
     * @see edu.utexas.wrap.demand.PAMap#getAttractors()
     */
    @Override
    public Set<Node> getAttractors() {
        return producers.keySet();
    }

    /* (non-Javadoc)
     * @see edu.utexas.wrap.demand.PAMap#getAttractions(edu.utexas.wrap.net.Node)
     */
    @Override
    public Float getAttractions(Node z) {
        return attractors.get(z);
    }

    /* (non-Javadoc)
     * @see edu.utexas.wrap.demand.PAMap#getProductions(edu.utexas.wrap.net.Node)
     */
    @Override
    public Float getProductions(Node z) {
        return producers.get(z);
    }

    /* (non-Javadoc)
     * @see edu.utexas.wrap.demand.PAMap#getGraph()
     */
    @Override
    public Graph getGraph() {
        return g;
    }

    /* (non-Javadoc)
     * @see edu.utexas.wrap.demand.PAMap#putAttractions(edu.utexas.wrap.net.Node, java.lang.Float)
     */
    @Override
    public void putAttractions(Node z, Float amt) {
        attractors.put(z, amt);
    }

    /* (non-Javadoc)
     * @see edu.utexas.wrap.demand.PAMap#putProductions(edu.utexas.wrap.net.Node, java.lang.Float)
     */
    @Override
    public void putProductions(Node z, Float amt) {
        producers.put(z, amt);
    }
    
	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.PAMap#getVOT()
	 */
	@Override
	public Float getVOT() {
		return vot;
	}
}
