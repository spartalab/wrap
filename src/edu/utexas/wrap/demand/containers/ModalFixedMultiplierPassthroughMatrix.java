package edu.utexas.wrap.demand.containers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.demand.PAMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class ModalFixedMultiplierPassthroughMatrix implements ModalPAMatrix {
	private float percent;
	private Mode mode;
	private PAMatrix aggregate;
	public ModalFixedMultiplierPassthroughMatrix(Mode m, float pct, PAMatrix agg) {
		percent = pct;
		mode = m;
		aggregate = agg;
	}
	
	@Override
	public Graph getGraph() {
		return aggregate.getGraph();
	}

	@Override
	public void put(TravelSurveyZone producer, TravelSurveyZone attractor, Float demand) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Writing to a read-only matrix");
	}

	@Override
	public Float getDemand(TravelSurveyZone producer, TravelSurveyZone attractor) {
		return percent*aggregate.getDemand(producer, attractor);
	}

	@Override
	public DemandMap getDemandMap(TravelSurveyZone producer) {
		return new FixedMultiplierPassthroughDemandMap(aggregate.getDemandMap(producer),percent);
	}

	@Override
	public float getVOT() {
		return aggregate.getVOT();
	}

	@Override
	public void toFile(File out) throws IOException {
		FileWriter o = null;
		try{
			o = new FileWriter(out);

			for (TravelSurveyZone orig : aggregate.getProducers()) {
				DemandMap demand = aggregate.getDemandMap(orig);
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
		return percent <= 0? new HashSet<TravelSurveyZone>() : aggregate.getProducers();
	}

	@Override
	public Mode getMode() {
		return mode;
	}

}
