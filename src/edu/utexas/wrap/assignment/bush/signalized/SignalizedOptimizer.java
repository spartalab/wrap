package edu.utexas.wrap.assignment.bush.signalized;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.utexas.wrap.assignment.AssignmentConsumer;
import edu.utexas.wrap.assignment.AssignmentProvider;
import edu.utexas.wrap.assignment.AtomicOptimizer;
import edu.utexas.wrap.assignment.PressureFunction;
import edu.utexas.wrap.assignment.bush.AlternateSegmentPair;

import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.assignment.bush.BushEvaluator;
import edu.utexas.wrap.assignment.bush.algoB.AlgorithmBOptimizer;
import edu.utexas.wrap.gui.IteratorRunner;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;
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
				iterEvaluator, iterThreshold, 
				this::getStepSize);
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
		Map<SignalGroup,Double> deltaG = getGreenShareChange(network, 
				scalingFactor);
		Map<TurningMovement,Double> deltaM = getGreenSplitChange(
				network,scalingFactor);

		containers.parallelStream().forEach(container -> {
			if (runner.isCancelled()) return;

			optimizer.process(container, network);
			runner.completedContainers.set(runner.completedContainers.get()+1);
		});


		// update signal timings
		throw new RuntimeException("Not yet implemented");
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

	public Double getStepSize(AlternateSegmentPair asp) {

		Iterable<Link> lpi = asp.longPath();
		Iterable<Link> spi = asp.shortPath();
		Double sum = 0.;
		Link nextLink = null;
		Node merge = asp.merge();

		for (Link l : lpi) {
			if (l.getHead() == merge && merge instanceof SignalizedNode) {
				sum += l.pricePrime(asp.getBush().valueOfTime()) + 1;

			} else if (l.getHead() instanceof SignalizedNode){
				// TODO get derivative for turning movement
				for (TurningMovement mvmt : 
					((SignalizedNode) l.getHead()).getMovements(l)) {
					if (mvmt.getHead() == nextLink) {
						sum += l.pricePrime(asp.getBush().valueOfTime()) 
								+ pressureFunction.delayPrime(mvmt);
						break;
					}
				}

			}
			nextLink = l;
		}

		nextLink = null;
		for (Link l : spi) {
			if (l.getHead() == merge && merge instanceof SignalizedNode) {
				sum += l.pricePrime(asp.getBush().valueOfTime()) + 1;
			} else if (l.getHead() instanceof SignalizedNode){
				// TODO get derivative for turning movement
				for (TurningMovement mvmt : ((SignalizedNode) l.getHead()).getMovements(l)) {
					if (mvmt.getHead() == nextLink) {
						sum += l.pricePrime(asp.getBush().valueOfTime()) 
								+ pressureFunction.delayPrime(mvmt);
						break;
					}
				}
			}
			nextLink = l;
		}
		return sum;
	}


	private Map<SignalGroup,Double> getGreenShareChange(
			Graph network, 

			Double scalingFactor
			) {
		Map<SignalGroup,Double> deltaG = new HashMap<SignalGroup,Double>();
		network.getNodes().stream()
		.filter(node -> node instanceof SignalizedNode)
		.map(node -> (SignalizedNode) node)
		.forEach(node -> {

			for (SignalGroup sg_a : node.getSignalGroups()) {
				for (SignalGroup sg_b : node.getSignalGroups()) {
					if (sg_a.getID() >= sg_b.getID()) continue;
					else {

						double pressureDiff = pressureFunction.signalGroupPressure(
								sg_a
								) - pressureFunction.signalGroupPressure(
										sg_b
										);
						deltaG.put(sg_a, 
								deltaG.getOrDefault(sg_a, 0.)
								+ scalingFactor*pressureDiff*node.getGreenShare(sg_a));
						deltaG.put(sg_b, 
								deltaG.getOrDefault(sg_b, 0.)
								- scalingFactor*pressureDiff*node.getGreenShare(sg_b));
					}
				}
			}

		});
		return deltaG;
	}

	private Map<TurningMovement,Double> getGreenSplitChange(
			Graph network, double scalingFactor){
		Map<TurningMovement,Double> deltaM = new HashMap<TurningMovement,Double>();
		
		network.getNodes().stream()
		.filter(node -> node instanceof SignalizedNode)
		.map(node -> (SignalizedNode) node)
		.flatMap(node -> node.getSignalGroups().stream())
		.flatMap(sg -> sg.getRings())
		.forEach(ring -> {
			for (TurningMovement tm_a : ring.getTurningMovements()) {
				for (TurningMovement tm_b : ring.getTurningMovements()) {
					if (tm_a.getID() >= tm_b.getID()) continue;
					double pressureDiff = pressureFunction.turningMovementPressure(
							tm_a
							) - pressureFunction.turningMovementPressure(tm_b);
					
					deltaM.put(tm_a, deltaM.getOrDefault(tm_a, 0.)
							+ scalingFactor * pressureDiff*ring.getGreenShare(tm_a));
					deltaM.put(tm_b, deltaM.getOrDefault(tm_b, 0.)
							- scalingFactor*pressureDiff*ring.getGreenShare(tm_b));
				}
			}
		});
		;
		
		return deltaM;
	}

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
