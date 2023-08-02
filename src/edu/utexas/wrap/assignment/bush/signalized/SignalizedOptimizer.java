package edu.utexas.wrap.assignment.bush.signalized;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.assignment.AssignmentConsumer;
import edu.utexas.wrap.assignment.AssignmentProvider;
import edu.utexas.wrap.assignment.AtomicOptimizer;
import edu.utexas.wrap.assignment.PressureFunction;
import edu.utexas.wrap.assignment.bush.BackVector;
import edu.utexas.wrap.assignment.bush.BushMerge;
import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.assignment.bush.BushEvaluator;
import edu.utexas.wrap.assignment.bush.algoB.AlgorithmBOptimizer;
import edu.utexas.wrap.gui.IteratorRunner;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.SignalGroup;
import edu.utexas.wrap.net.SignalizedNode;
import edu.utexas.wrap.net.TurningMovement;

public class SignalizedOptimizer 
		implements AtomicOptimizer<Bush> {

	private final PressureFunction pressureFunction;
	private int completedIterations = 0;
	private double scalingFactor = 0.000001;
	private double maxPressure;
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
		scalingFactor = 2/((completedIterations+2)*maxPressure);
		// get flows

		//combine into a green shift map
//		Map<SignalGroup,Double> deltaG = getGreenShareChange(network, 
//				scalingFactor);
//		
//		Map<TurningMovement,Double> turningFlow = getTurningFlow(
//				network,
//				containers);
//		
		containers.parallelStream().forEach(container -> {
			if (runner.isCancelled()) return;

			optimizer.process(container, network);
			runner.completedContainers.set(runner.completedContainers.get()+1);
		});
		
		
		// update signal timings
//		network.getNodes().parallelStream()
//		.filter(
//				node -> node instanceof SignalizedNode
//				)
//		.map(
//				node-> (SignalizedNode) node
//				)
//		.forEach(
//				node ->{
//					node.updateGroupGreenShares(deltaG);
//					
//				}
//				);

	}

	private Map<TurningMovement, Double> getTurningFlow(
				Graph network,
				Collection<Bush> containers) {
		// TODO Auto-generated method stub
		
		throw new RuntimeException("not yet implemented");
	}

//	private Map<SignalGroup,Double> getGreenShareChange(
//			Graph network, 
//			
//			Double scalingFactor
//			) {
//		Map<SignalGroup,Double> deltaG = new HashMap<SignalGroup,Double>();
//		network.getNodes().stream()
//			.filter(node -> node instanceof SignalizedNode)
//			.map(node -> (SignalizedNode) node)
//			.forEach(node -> {
//					
//					for (SignalGroup sg_a : node.getSignalGroups()) {
//						for (SignalGroup sg_b : node.getSignalGroups()) {
//							if (sg_a.getID() >= sg_b.getID()) continue;
//							else {
//								
//								double pressureDiff = pressureFunction.stagePressure(
//											sg_a
//										) - pressureFunction.stagePressure(
//											sg_b
//										);
//								deltaG.put(sg_a, 
//										deltaG.getOrDefault(sg_a, 0.)
//										+ scalingFactor*pressureDiff*node.getGreenShare(sg_a));
//								deltaG.put(sg_b, 
//										deltaG.getOrDefault(sg_b, 0.)
//										- scalingFactor*pressureDiff*node.getGreenShare(sg_b));
//							}
//						}
//					}
//					
//			});
//		return deltaG;
//	}
	


	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setCompletedIterations(int i) {
		// TODO Auto-generated method stub
		completedIterations = i;
	}

	public void setMaxPressure(double maxPressure) {
		// TODO Auto-generated method stub
		this.maxPressure = maxPressure;
	}

}
