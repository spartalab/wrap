package edu.utexas.wrap.marketsegmentation;

import javafx.concurrent.Task;

public class PurposeRunner extends Task<Double> {

	private final Purpose purpose;
	
	
	
	public PurposeRunner(BasicPurpose purpose) {
		// TODO Auto-generated constructor stub
		this.purpose = purpose;
	}
	
	@Override
	public String toString() {
		return purpose.toString();
	}

	@Override
	protected Double call() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
