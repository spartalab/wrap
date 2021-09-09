package edu.utexas.wrap.gui;

import edu.utexas.wrap.assignment.Assigner;
import edu.utexas.wrap.net.Graph;
import javafx.concurrent.Task;

public class AssignerRunner extends Task<Graph> {
	private Assigner assigner;
	private RunnerController parent;

	public AssignerRunner(Assigner assigner, RunnerController runnerController) {
		// TODO Auto-generated constructor stub
		this.assigner = assigner;
		this.parent = runnerController;
	}

	@Override
	protected Graph call() throws Exception {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String toString() {
		return assigner.toString();
	}

}
