package edu.utexas.wrap.gui;

import edu.utexas.wrap.assignment.Assigner;
import edu.utexas.wrap.net.Graph;
import javafx.concurrent.Task;

public class AssignerRunner extends Task<Graph> {
	private Assigner assigner;

	public AssignerRunner(Assigner assigner) {
		// TODO Auto-generated constructor stub
		this.assigner = assigner;
	}

	@Override
	protected Graph call() throws Exception {
		// TODO Auto-generated method stub
		updateProgress(0.,1.);
		updateMessage("Initializing");
		assigner.initialize();
		updateMessage("Evaluating");
		updateProgress(assigner.getProgress(),1);
		
		while (!isCancelled() && !assigner.isTerminated()) {
			updateMessage("Iterating");
			assigner.iterate();
			if (isCancelled()) break;
			updateMessage("Evaluating");
			updateProgress(assigner.getProgress(),1);
		}
		
		
		return null;
	}
	
	@Override
	public String toString() {
		return assigner.toString();
	}

}
