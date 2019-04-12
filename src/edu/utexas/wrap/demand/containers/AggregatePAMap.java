package edu.utexas.wrap.demand.containers;

import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Rishabh
 *
 * An aggregate production/attraction map implementation
 * Created in the trip generation process, this object
 * stores the total trips produced and attracted by a specified Node
 */
public class AggregatePAMap implements PAMap {

    private Graph g;
    private Map<Node, Float> attractors;
    private Map<Node, Float> producers;

    public AggregatePAMap(Graph graph) {
        this.g = graph;
        attractors = new HashMap<>();
        producers = new HashMap<>();
    }
    @Override
    public Set<Node> getProducers() {
        return attractors.keySet();
    }

    @Override
    public Set<Node> getAttractors() {
        return producers.keySet();
    }

    @Override
    public Float getAttractions(Node z) {
        return attractors.get(z);
    }

    @Override
    public Float getProductions(Node z) {
        return producers.get(z);
    }

    @Override
    public Graph getGraph() {
        return g;
    }

    @Override
    public void putAttractions(Node z, Float amt) {
        attractors.put(z, amt);
    }

    @Override
    public void putProductions(Node z, Float amt) {
        producers.put(z, amt);
    }
}
