package edu.utexas.wrap.gui;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.utexas.wrap.assignment.AssignmentContainer;
import edu.utexas.wrap.assignment.AssignmentOptimizer;
import edu.utexas.wrap.assignment.AtomicOptimizer;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.SignalizedNode;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;

public class IteratorRunner<T extends AssignmentContainer> extends Task<Void> {

	private AssignmentOptimizer<T> optimizer;
	private Collection<T> containers;
	private Graph network;
	private IntegerProperty completedContainers;

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
			Map<Link, Double> flows = network.getLinks().stream().collect(
					Collectors.toMap(Function.identity(), Link::getFlow)
					);
			// get flows
			Map<Link, Double> greenShares = network.getLinks().stream()
				.collect(Collectors.toMap(
					Function.identity(),
					l -> l.getHead() instanceof SignalizedNode?
						((SignalizedNode) l.getHead())
								.getGreenShare(l).doubleValue()
							: null	)
					);
			// get signal timings
			
			Map<Link,Double> bottleneckDelays = network.getLinks().stream()
				.collect(Collectors.toMap(
					Function.identity(),
					link -> 
						flows.get(link)*(1-greenShares.get(link))*(
								link.getHead() instanceof SignalizedNode?
									((SignalizedNode) 
											link.getHead()).getCycleLength()
									: 0.
								)/(link.getCapacity() - flows.get(link))
					));	
			
			Map<T,Map<Link,Double>> costs = containers.parallelStream().collect(
					Collectors.toMap(
							Function.identity(), 
							container -> network.getLinks().stream()
							.collect(Collectors.toMap(
									Function.identity(),
									link -> link.getPrice(container)
									)))
					);

			containers.parallelStream().forEach(container -> {
				if (isCancelled()) return;
				
				((AtomicOptimizer<T>)optimizer).process(
						container, 
						network, 
						flows, 
						greenShares,
						bottleneckDelays,
						costs.get(container)
						);
				completedContainers.set(completedContainers.get()+1);
			});
			// TODO update signal timings
		} 
		else containers.parallelStream().forEach(container -> {
			if (isCancelled()) return;
			optimizer.process(container, network);
			completedContainers.set(completedContainers.get()+1);
		});
		return null;

	}

}
