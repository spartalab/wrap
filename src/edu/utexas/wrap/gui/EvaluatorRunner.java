package edu.utexas.wrap.gui;

import java.util.Collection;

import edu.utexas.wrap.assignment.AssignmentContainer;
import edu.utexas.wrap.assignment.AssignmentEvaluator;
import edu.utexas.wrap.net.Graph;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;

public class EvaluatorRunner<T extends AssignmentContainer> extends Task<Double> {

	private AssignmentEvaluator<T> evaluator;
	private Collection<T> containers;
	private Graph network;
	private IntegerProperty completedContainers;
	
	public EvaluatorRunner(AssignmentEvaluator<T> evaluator, Collection<T> containers, Graph network, AssignerRunner<T> parent) {
		this.evaluator = evaluator;
		this.containers = containers;
		this.network = network;
		
		completedContainers = new SimpleIntegerProperty();
		completedContainers.addListener((obs,oldVal,newVal)->{
			updateProgress((int) newVal, containers.size());
			parent.updateSubtaskProgress((int) newVal,containers.size());
		});
		
	}
	
	@Override
	protected Double call() throws Exception {
		// TODO Auto-generated method stub
		updateProgress(0.,containers.size());
		evaluator.initialize();
		if (isCancelled()) return null;
		
		containers.parallelStream().forEach(container -> {
			if (isCancelled()) return;
			evaluator.process(container, network);
			completedContainers.set(completedContainers.get()+1);
		});
		if (isCancelled()) return null;
		return evaluator.getValue();
	}

}
