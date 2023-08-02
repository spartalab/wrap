package edu.utexas.wrap.gui;

import java.util.Collection;

import edu.utexas.wrap.assignment.Assigner;
import edu.utexas.wrap.assignment.AssignmentContainer;
import edu.utexas.wrap.assignment.bush.signalized.SignalizedOptimizer;
import edu.utexas.wrap.demand.ODProfile;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.SignalizedNode;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;

public class AssignerRunner<C extends AssignmentContainer> extends Task<Graph> {
	private final Assigner<C> assigner;
	private final Collection<ODProfile> profiles;
	private Task<Double> evaluator;
	private IteratorRunner<C> iterator;
	public ReadOnlyDoubleWrapper subtaskProgress;
	private RunnerController parent;

	public AssignerRunner(Assigner<C> assigner, Collection<ODProfile> profiles, RunnerController parent) {
		// TODO Auto-generated constructor stub
		this.assigner = assigner;
		this.profiles = profiles;
		this.parent = parent;
		updateProgress(0.,1.);

		subtaskProgress = new ReadOnlyDoubleWrapper();
		setOnCancelled(new EventHandler<WorkerStateEvent>() {

			@Override
			public void handle(WorkerStateEvent arg0) {
				// TODO Auto-generated method stub
//				if (initializer != null) initializer.cancel();
				if (evaluator != null) evaluator.cancel();
				if (iterator != null) iterator.cancel();
			}
			
		});
	}
	

	@Override
	protected Graph call() throws Exception {
		
		updateMessage("Initializing");
		assigner.initialize(profiles);

		updateMessage("Evaluating");
		evaluator = new EvaluatorRunner<C>(
				assigner.getEvaluator(),
				assigner.getContainers(), 
				assigner.getNetwork(),
				this);

		evaluator.run();
		int numIterations = 0;
		
		Double progress = assigner.getProgress(evaluator.get(), numIterations);
		updateProgress(progress,1);
		
		if (assigner.getOptimizer() instanceof SignalizedOptimizer) {
			double maxPressure = this.assigner.getNetwork()
				.getLinks().parallelStream().mapToDouble(
					link -> 100*link.getCapacity()*(
						link.getHead() instanceof SignalizedNode? 
							((SignalizedNode) link.getHead()).getCycleLength()
							: 0.))
				.max().getAsDouble();
			((SignalizedOptimizer) assigner.getOptimizer())
				.setMaxPressure(maxPressure);
		}
		
		while (!isCancelled() && progress < 1) {
			updateMessage("Iterating");
			iterator = new IteratorRunner<C>(
					assigner.getOptimizer(), 
					assigner.getContainers(),
					assigner.getNetwork(),
					this);
			iterator.setCompletedIterations(numIterations++);
			iterator.run();
			if (isCancelled()) break;
			
			updateMessage("Evaluating");
			evaluator = new EvaluatorRunner<C>(
					assigner.getEvaluator(),
					assigner.getContainers(), 
					assigner.getNetwork(),
					this);

			evaluator.run();
			progress = assigner.getProgress(evaluator.get(),numIterations);
			updateProgress(progress,1);
		}
		updateMessage("Done");
		updateProgress(1,1);
		System.err.println("Final gap: "+evaluator.get());
		System.err.println("Num iterations: "+numIterations);
		updateSubtaskProgress(1,1);
		return assigner.getNetwork();
		
	}
	
	@Override
	public String toString() {
		return assigner.toString();
	}

	public Assigner<C> getAssigner() {
		// TODO Auto-generated method stub
		return assigner;
	}
	
	public ReadOnlyDoubleProperty subtaskProgressProperty() {
		return subtaskProgress;
	}


	public void updateSubtaskProgress(int newVal, int size) {
		// TODO Auto-generated method stub
		subtaskProgress.set(newVal/size);
	}

	@Override
	protected void succeeded() {
		parent.increaseCompletedAssigners();
	}
}
