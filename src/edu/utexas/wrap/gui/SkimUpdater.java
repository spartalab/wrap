package edu.utexas.wrap.gui;

import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.Project;
import edu.utexas.wrap.assignment.Assigner;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.NetworkSkim;
import javafx.concurrent.Task;

public class SkimUpdater extends Task<NetworkSkim> {
	private String skimID;
	private Project project;

	public SkimUpdater(String skimID, Project project) {
		this.skimID = skimID;
		this.project = project;
	}

	@Override
	protected NetworkSkim call() {
		// TODO Auto-generated method stub
		try { 
			updateProgress(0,1); 

			System.out.println("Getting assigner for "+skimID);
			Assigner<?> assigner = project.getAssigner(project.getSkimAssigner(skimID));
			System.out.println("Assigner: "+assigner);
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
			System.out.println("Function is null? "+(func == null));
			NetworkSkim skim = assigner.getSkim(skimID, func);
			System.out.println("Got skim "+skimID);
			updateProgress(1,1);
			return skim;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String toString() {
		return skimID;
	}
}
