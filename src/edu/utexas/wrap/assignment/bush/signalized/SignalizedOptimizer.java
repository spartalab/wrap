package edu.utexas.wrap.assignment.bush.signalized;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import edu.utexas.wrap.assignment.AssignmentConsumer;
import edu.utexas.wrap.assignment.AssignmentProvider;
import edu.utexas.wrap.assignment.AtomicOptimizer;
import edu.utexas.wrap.assignment.PressureFunction;
import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.assignment.bush.BushEvaluator;
import edu.utexas.wrap.assignment.bush.algoB.AlgorithmBOptimizer;
import edu.utexas.wrap.gui.IteratorRunner;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.SignalizedNode;

public class SignalizedOptimizer 
		implements AtomicOptimizer<Bush> {

	private final PressureFunction pressureFunction;
	
	private double scalingFactor = 0.000001;
	private final AlgorithmBOptimizer optimizer;


	public SignalizedOptimizer(
			AssignmentProvider<Bush> provider, 
			AssignmentConsumer<Bush> writer,
			BushEvaluator iterEvaluator, 
			double iterThreshold,
			PressureFunction pressureFunction) {
		// TODO Auto-generated constructor stub
		this.pressureFunction = pressureFunction;
		optimizer = new AlgorithmBOptimizer(provider, writer, 
				iterEvaluator, iterThreshold);
	}
	
	@Override
	public void iterate(
			Collection<Bush> containers,
			Graph network,
			IteratorRunner<Bush> runner
			) {

		// get flows
		Map<Link, Double> greenShares = getGreenShares(network);
		// get signal timings
		
		
		//combine into a green shift map
		Map<Link,Double> deltaG = getGreenShareChange(network, 
				greenShares, 
				scalingFactor);
		
		containers.parallelStream().forEach(container -> {
			if (runner.isCancelled()) return;

			optimizer.process(container, network);
			runner.completedContainers.set(runner.completedContainers.get()+1);
		});
		
		
		// TODO update signal timings
		network.getNodes().parallelStream()
		.filter(node -> node instanceof SignalizedNode)
		.map(node-> (SignalizedNode) node)
		.forEach(node ->{
			node.updateGreenShares(deltaG);
		});
		
		
		// TODO update scaling factor
		scalingFactor = scalingFactor / 2;
	}

	private Map<Link, Double> getGreenShares(Graph network) {
		return network.getLinks().stream()
			.collect(Collectors.toMap(
				Function.identity(),
				link -> link.getHead() instanceof SignalizedNode?
					((SignalizedNode) link.getHead())
							.getGreenShare(link).doubleValue()
						: null	)
				);
	}

	private Map<Link,Double> getGreenShareChange(
			Graph network, 
			Map<Link, Double> greenShares,
			Double scalingFactor
			) {
		Map<Link,Double> deltaG = new HashMap<Link,Double>();
		network.getNodes().stream()
			.filter(node -> node instanceof SignalizedNode)
			.map(node -> (SignalizedNode) node)
			.forEach(node -> {
					Link[] inputs = node.reverseStar();
					
					for (int i = 0; i < inputs.length; i++) {
						for (int j = i+1; j < inputs.length; j++) {
//							if (node.compatiblePhases(inputs[i],inputs[j])) continue;
							double pressureDiff = pressureFunction.stagePressure(
									inputs[i]
									
								) - pressureFunction.stagePressure(
									
									inputs[j]
										);
							deltaG.put(inputs[i],
									deltaG.getOrDefault(inputs[i], 0.) 
									+ scalingFactor*pressureDiff*greenShares.get(inputs[i]));
							deltaG.put(inputs[j],
									deltaG.getOrDefault(inputs[j],0.)
									- scalingFactor*pressureDiff*greenShares.get(inputs[j]));
						}	
					}
			});
		return deltaG;
	}
	


	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

}
