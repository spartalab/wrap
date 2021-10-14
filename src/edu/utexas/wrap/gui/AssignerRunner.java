package edu.utexas.wrap.gui;

import java.util.Collection;

import edu.utexas.wrap.assignment.Assigner;
import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.net.Graph;
import javafx.concurrent.Task;

public class AssignerRunner extends Task<Graph> {
	private final Assigner assigner;
	private final Collection<ODProfile> profiles;

	public AssignerRunner(Assigner assigner, Collection<ODProfile> profiles) {
		// TODO Auto-generated constructor stub
		this.assigner = assigner;
		this.profiles = profiles;
	}

	@Override
	protected Graph call() throws Exception {
		// TODO Auto-generated method stub
		updateProgress(0.,1.);
		updateMessage("Initializing");
		assigner.initialize(profiles);

		updateMessage("Evaluating");
		updateProgress(assigner.getProgress(),1);
		
		while (!isCancelled() && !assigner.isTerminated()) {
			updateMessage("Iterating");
			assigner.iterate();
			if (isCancelled()) break;
			updateMessage("Evaluating");
			updateProgress(assigner.getProgress(),1);
		}
		
		updateMessage("Done");
		return assigner.getNetwork();
	}
	
	@Override
	public String toString() {
		return assigner.toString();
	}

	public Assigner getAssigner() {
		// TODO Auto-generated method stub
		return assigner;
	}

}
