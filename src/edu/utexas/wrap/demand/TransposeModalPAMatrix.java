package edu.utexas.wrap.demand;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.TravelSurveyZone;

public class TransposeModalPAMatrix implements ModalPAMatrix {
	ModalPAMatrix base;
	
	public TransposeModalPAMatrix(ModalPAMatrix pa) {
		base = pa;
	}

	@Override
	public Graph getGraph() {
		return base.getGraph();
	}

	@Override
	public void put(TravelSurveyZone producer, TravelSurveyZone attractor, Float demand) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Writing to read-only matrix");
	}

	@Override
	public Float getDemand(TravelSurveyZone producer, TravelSurveyZone attractor) {
		return base.getDemand(attractor, producer);
	}

	@Override
	public DemandMap getDemandMap(TravelSurveyZone producer) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public float getVOT() {
		return base.getVOT();
	}

	@Override
	public void toFile(File out) throws IOException {
		// TODO Auto-generated method stub
		throw new RuntimeException();
	}

	@Override
	public Collection<TravelSurveyZone> getProducers() {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public Mode getMode() {
		// TODO Auto-generated method stub
		return base.getMode();
	}

}
