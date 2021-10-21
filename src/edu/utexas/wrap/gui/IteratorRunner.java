package edu.utexas.wrap.gui;

import java.util.Collection;

import edu.utexas.wrap.assignment.AssignmentContainer;
import edu.utexas.wrap.assignment.AssignmentOptimizer;
import edu.utexas.wrap.net.Graph;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;

public class IteratorRunner<T extends AssignmentContainer> extends Task<Void> {

	private AssignmentOptimizer<T> optimizer;
	private Collection<T> containers;
	private Graph network;
	private IntegerProperty completedContainers;
	
	public IteratorRunner(AssignmentOptimizer<T> optimizer, Collection<T> containers, Graph network, AssignerRunner<T> parent) {
		this.optimizer = optimizer;
		this.containers = containers;
		this.network = network;
		
		completedContainers = new SimpleIntegerProperty();
		completedContainers.addListener((obs, oldVal, newVal)->{
			updateProgress((int) newVal, containers.size());
			parent.updateSubtaskProgress((int) newVal, containers.size());
		});
	}
	
	@Override
	protected Void call() {
		// TODO Auto-generated method stub
		updateProgress(0., containers.size());
		optimizer.initialize();
		if (isCancelled()) return null;
		
		containers.parallelStream().forEach(container -> {
			if (isCancelled()) return;
			optimizer.process(container, network);
			completedContainers.set(completedContainers.get()+1);
		});
		return null;
		
	}

}
