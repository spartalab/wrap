package edu.utexas.wrap.gui;

import java.util.Collection;

import edu.utexas.wrap.assignment.AssignmentContainer;
import edu.utexas.wrap.assignment.AssignmentOptimizer;
import edu.utexas.wrap.assignment.AtomicOptimizer;
import edu.utexas.wrap.assignment.bush.algoB.ParallelizedOptimizer;
import edu.utexas.wrap.net.Graph;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;

public class IteratorRunner<T extends AssignmentContainer> extends Task<Void> {

	private AssignmentOptimizer<T> optimizer;
	private Collection<T> containers;
	private Graph network;
	public IntegerProperty completedContainers;

	public IteratorRunner(AssignmentOptimizer<T> optimizer, 
			Collection<T> containers, Graph network, AssignerRunner<T> parent) {
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
		updateProgress(0., containers.size());
		optimizer.initialize();
		if (isCancelled()) return null;



		if (optimizer instanceof AtomicOptimizer<?>) {
			if (isCancelled()) return null;

			AtomicOptimizer<T> optimizer = (AtomicOptimizer<T>) this.optimizer;
			optimizer.iterate(containers, network, this);

			
		} 
		
		else if (optimizer instanceof ParallelizedOptimizer<?>){
			containers.parallelStream().forEach(container -> {
				if (isCancelled()) return;
				ParallelizedOptimizer<T> optimizer = 
						(ParallelizedOptimizer<T>) this.optimizer;
				optimizer.process(container, network);
				completedContainers.set(completedContainers.get()+1);
			});

		}
		
		return null;

	}

	public void setCompletedIterations(int i) {
		// TODO Auto-generated method stub
		if (optimizer instanceof AtomicOptimizer<?>) {
			((AtomicOptimizer<?>) optimizer).setCompletedIterations(i);
		}
	}

}
