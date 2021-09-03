package edu.utexas.wrap.marketsegmentation;

import java.util.Collection;
import java.util.HashSet;

import edu.utexas.wrap.gui.RunnerController;
import javafx.concurrent.Task;

public class MarketRunner extends Task<Double> {
	
	private final Market market;
	private Collection<PurposeRunner> purposeRunners;
	private RunnerController parent;

	public MarketRunner(Market market, RunnerController parent) {
		// TODO Auto-generated constructor stub
		this.market = market;
		this.parent = parent;
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
		updateProgress(0,1);
		// TODO Auto-generated method stub
		Thread.sleep(3000);
		updateProgress(1,1);
		return 0.;
	}
	
	@Override
	protected void succeeded() {
		parent.increaseCompletedMarkets();
	}

}
