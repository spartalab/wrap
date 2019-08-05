package edu.utexas.wrap.demand.containers;

import edu.utexas.wrap.demand.PAMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.net.TravelSurveyZone;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
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
    private Map<TravelSurveyZone, Float> attractors;
    private Map<TravelSurveyZone, Float> producers;
    private float vot;

    public AggregatePAHashMap(Graph graph) {
        this.g = graph;
        attractors = Object2FloatMaps.synchronize(new Object2FloatOpenHashMap<TravelSurveyZone>());
        producers = Object2FloatMaps.synchronize(new Object2FloatOpenHashMap<TravelSurveyZone>());
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
    public Float getAttractions(TravelSurveyZone z) {
        return attractors.get(z);
    }

    /* (non-Javadoc)
     * @see edu.utexas.wrap.demand.PAMap#getProductions(edu.utexas.wrap.net.Node)
     */
    @Override
    public Float getProductions(TravelSurveyZone z) {
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
    
	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.PAMap#getVOT()
	 */
	@Override
	public Float getVOT() {
		return vot;
	}
}
