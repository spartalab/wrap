package edu.utexas.wrap.gui;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.Project;
import edu.utexas.wrap.assignment.Assigner;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Link;
import javafx.concurrent.Task;

public class SkimUpdater extends Task<Void> {
	private String skimID;
	private Project project;

	public SkimUpdater(String skimID, Project project) {
		// TODO Auto-generated constructor stub
		this.skimID = skimID;
		this.project = project;
	}

	@Override
	protected Void call() throws Exception {
		// TODO Auto-generated method stub
		Assigner assigner = project.getAssigner(project.getSkimAssigner(skimID));
		ToDoubleFunction<Link> func;
		switch (project.getSkimFunction(skimID)) {
		case "travelTimeSingleOcc":
			func = (Link x) -> 
				x.allowsClass(Mode.SINGLE_OCC)? x.getTravelTime() : Double.MAX_VALUE;
				break;
		default:
			System.err.println("Skim funciton not yet implemented. Reverting to travel time");
		case "travelTime":
			func = Link::getTravelTime;
		}
		project.updateSkim(skimID, assigner.getSkim(skimID, func));
		return null;
	}

}
