package edu.utexas.wrap.marketsegmentation;

import javafx.concurrent.Task;

public class PurposeRunner extends Task<Double> {

	private final Purpose purpose;
	private MarketRunner parent;
	
	
	public PurposeRunner(BasicPurpose purpose, MarketRunner parent) {
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
		Thread.sleep(500);
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void succeeded() {
		parent.incrementCompletedPurposes();
	}
}
