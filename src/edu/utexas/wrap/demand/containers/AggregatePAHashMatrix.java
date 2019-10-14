package edu.utexas.wrap.demand.containers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.PAMatrix;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class AggregatePAHashMatrix implements AggregatePAMatrix {

	private Graph g;
	private Map<TravelSurveyZone,DemandMap> matrix;

	public AggregatePAHashMatrix(Graph g) {
		this.g = g;
		matrix = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<TravelSurveyZone,DemandMap>(g.numZones(),1.0f));
	}

	public AggregatePAHashMatrix(PAMatrix hbwSum, Map<TravelSurveyZone, Float> map) {
		// TODO Auto-generated constructor stub
		g = hbwSum.getGraph();
		matrix = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<TravelSurveyZone,DemandMap>(g.numZones(),1.0f));
		hbwSum.getProducers().parallelStream().forEach(prod ->{
			matrix.put(prod, new FixedMultiplierPassthroughDemandMap(hbwSum.getDemandMap(prod),map.getOrDefault(prod,0.0f)));
		});
	}

	/**
	 * Insert the demand map for a given node
	 * @param i the Node from which there is demand
	 * @param d the map of demand from the given Node to other Nodes
	 */
	public void putDemandMap(TravelSurveyZone i, DemandMap d) {
		matrix.put(i, d);
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.PAMatrix#getDemand(edu.utexas.wrap.net.Node, edu.utexas.wrap.net.Node)
	 */
	@Override
	public Float getDemand(TravelSurveyZone producer, TravelSurveyZone attractor) {
		// TODO Auto-generated method stub
		return matrix.get(producer) == null ? 0.0F : matrix.get(producer).getOrDefault(attractor, 0.0).floatValue();
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.PAMatrix#getVOT()
	 */
 	public float getVOT() {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.PAMatrix#getGraph()
	 */
	@Override
	public Graph getGraph() {
		return g;
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.demand.PAMatrix#put(edu.utexas.wrap.net.Node, edu.utexas.wrap.net.Node, java.lang.Float)
	 */
	@Override
	public void put(TravelSurveyZone producer, TravelSurveyZone attractor, Float demand) {
		matrix.putIfAbsent(producer,new DemandHashMap(g));
		((DemandMap) matrix.get(producer)).put(attractor,demand.doubleValue());
		
	}

	@Override
	public void toFile(File out) throws IOException {
		FileWriter o = null;
		try{
			o = new FileWriter(out);

			for (TravelSurveyZone prod : matrix.keySet()) {
				DemandMap demand = matrix.get(prod);
				for (TravelSurveyZone attr : demand.getZones()) {
					o.write(""+prod.getNode().getID()+","+attr.getNode().getID()+","+demand.get(attr)+"\n");
				}
			}
		} finally {
			if (o != null) o.close();
		}
	}

	@Override
	public DemandMap getDemandMap(TravelSurveyZone producer) {
		// TODO Auto-generated method stub
		return matrix.get(producer);
	}

	@Override
	public Collection<TravelSurveyZone> getProducers() {
		// TODO Auto-generated method stub
		return matrix.keySet();
	}

}
