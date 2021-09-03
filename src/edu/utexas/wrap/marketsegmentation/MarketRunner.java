package edu.utexas.wrap.marketsegmentation;

import java.util.Collection;
import java.util.HashSet;

import javafx.concurrent.Task;

public class MarketRunner extends Task<Double> {
	
	private final Market market;
	private Collection<PurposeRunner> purposeRunners;

	public MarketRunner(Market market) {
		// TODO Auto-generated constructor stub
		this.market = market;
		purposeRunners = new HashSet<PurposeRunner>();
	}

	
	@Override
	public String toString() {
		return market.toString();
	}
	
	public void attach(PurposeRunner runner) {
		//TODO
		purposeRunners.add(runner);
	}

	@Override
	protected Double call() throws Exception {
		// TODO Auto-generated method stub
		return 0.;
	}

}
