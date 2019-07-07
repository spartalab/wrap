package edu.utexas.wrap.demand.containers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import edu.utexas.wrap.demand.AggregatePAMatrix;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.demand.ModalPAMatrix;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

public class ModalFixedMultiplierPassthroughMatrix implements ModalPAMatrix {
	private float percent;
	private Mode mode;
	private AggregatePAMatrix aggregate;
	public ModalFixedMultiplierPassthroughMatrix(Mode m, float pct, AggregatePAMatrix agg) {
		percent = pct;
		mode = m;
		aggregate = agg;
	}
	
	@Override
	public Graph getGraph() {
		return aggregate.getGraph();
	}

	@Override
	public void put(Node producer, Node attractor, Float demand) {
		// TODO Auto-generated method stub
		throw new RuntimeException();
	}

	@Override
	public Float getDemand(Node producer, Node attractor) {
		return percent*aggregate.getDemand(producer, attractor);
	}

	@Override
	public DemandMap getDemandMap(Node producer) {
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

			for (Node orig : aggregate.getProducers()) {
				DemandMap demand = aggregate.getDemandMap(orig);
				for (Node dest : demand.getNodes()) {
					o.write(""+orig.getID()+","+dest.getID()+","+demand.get(dest)+"\n");
				}
			}
		} finally {
			if (o != null) o.close();
		}
	}

	@Override
	public Collection<Node> getProducers() {
		return aggregate.getProducers();
	}

	@Override
	public Mode getMode() {
		return mode;
	}

}
