package edu.utexas.wrap.marketsegmentation;

import javafx.concurrent.Task;

public class PurposeRunner extends Task<Double> {

	private final Purpose purpose;
	private MarketRunner parent;
	
	
	public PurposeRunner(Purpose purpose, MarketRunner parent) {
		// TODO Auto-generated constructor stub
		this.purpose = purpose;
		this.parent = parent;
	}
	
	@Override
	public String toString() {
		return purpose.toString();
	}

	@Override
	protected Double call() throws Exception {
		updateProgress(0, 1);
		Thread.sleep(500);
		// TODO Auto-generated method stub
		updateProgress(1,1);
		return null;
	}

	@Override
	protected void succeeded() {
		parent.incrementCompletedPurposes();
	}
}
