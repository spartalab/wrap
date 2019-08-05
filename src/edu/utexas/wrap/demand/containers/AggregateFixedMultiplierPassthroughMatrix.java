package edu.utexas.wrap.demand.containers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class AggregateFixedMultiplierPassthroughMatrix implements AggregatePAMatrix {
	
	private final AggregatePAMatrix base;
	private final float multip;

	public AggregateFixedMultiplierPassthroughMatrix(AggregatePAMatrix baseMatrix, float multiplier) {
		base = baseMatrix;
		multip = multiplier;
	}
	
	@Override
	public Graph getGraph() {
		return base.getGraph();
	}

	@Override
	public void put(TravelSurveyZone producer, TravelSurveyZone attractor, Float demand) {
		throw new RuntimeException("Writing to a read-only matrix");
	}

	@Override
	public Float getDemand(TravelSurveyZone producer, TravelSurveyZone attractor) {
		return multip*base.getDemand(producer, attractor);
	}

	@Override
	public DemandMap getDemandMap(TravelSurveyZone producer) {
		return new FixedMultiplierPassthroughDemandMap(base.getDemandMap(producer),multip);
	}

	@Override
	public float getVOT() {
		return base.getVOT();
	}

	@Override
	public void toFile(File out) throws IOException {
		FileWriter o = null;
		try{
			o = new FileWriter(out);

			for (TravelSurveyZone orig : base.getProducers()) {
				DemandMap demand = base.getDemandMap(orig);
				for (TravelSurveyZone dest : demand.getZones()) {
					o.write(""+orig.getNode().getID()+","+dest.getNode().getID()+","+demand.get(dest)+"\n");
				}
			}
		} finally {
			if (o != null) o.close();
		}
	}

	@Override
	public Collection<TravelSurveyZone> getProducers() {
		return multip <= 0? new HashSet<TravelSurveyZone>() : base.getProducers();
	}

}
