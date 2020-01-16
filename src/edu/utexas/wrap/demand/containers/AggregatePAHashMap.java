package edu.utexas.wrap.demand.containers;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Rishabh
 *
 * An aggregate production/attraction map implementation
 * Created in the trip generation process, this object
 * stores the total trips produced and attracted by a specified Node
 */
public class AggregatePAHashMap implements PAMap {

    private Graph g;
    private Map<TravelSurveyZone, Float> attractors;
    private Map<TravelSurveyZone, Float> producers;

    public AggregatePAHashMap(Graph graph) {
        this.g = graph;
        attractors = new ConcurrentHashMap<TravelSurveyZone,Float>();
        producers = new ConcurrentHashMap<TravelSurveyZone,Float>();
    }
    
    /* (non-Javadoc)
     * @see edu.utexas.wrap.demand.PAMap#getProducers()
     */
    @Override
    public Set<TravelSurveyZone> getProducers() {
        return attractors.keySet();
    }

    /* (non-Javadoc)
     * @see edu.utexas.wrap.demand.PAMap#getAttractors()
     */
    @Override
    public Set<TravelSurveyZone> getAttractors() {
        return producers.keySet();
    }

    /* (non-Javadoc)
     * @see edu.utexas.wrap.demand.PAMap#getAttractions(edu.utexas.wrap.net.Node)
     */
    @Override
    public float getAttractions(TravelSurveyZone z) {
        return attractors.getOrDefault(z, 0.0f);
    }

    /* (non-Javadoc)
     * @see edu.utexas.wrap.demand.PAMap#getProductions(edu.utexas.wrap.net.Node)
     */
    @Override
    public float getProductions(TravelSurveyZone z) {
        return producers.getOrDefault(z, 0.0f);
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
    public void putAttractions(TravelSurveyZone z, Float amt) {
        attractors.put(z, amt);
    }

    /* (non-Javadoc)
     * @see edu.utexas.wrap.demand.PAMap#putProductions(edu.utexas.wrap.net.Node, java.lang.Float)
     */
    @Override
    public void putProductions(TravelSurveyZone z, Float amt) {
        producers.put(z, amt);
    }

	@Override
	public DemandMap getProductionMap() {
		// TODO Auto-generated method stub
		throw new RuntimeException();
	}

	@Override
	public DemandMap getAttractionMap() {
		// TODO Auto-generated method stub
		throw new RuntimeException();
	}
}
