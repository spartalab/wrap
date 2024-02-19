package edu.utexas.wrap.assignment.bush.signalized;

import java.util.Collection;
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
	private double scalingFactor;
	private double maxPressure;
	private final AlgorithmBOptimizer optimizer;
	private Map<SignalGroup,Double> deltaG;
	private Map<TurningMovement,Double> deltaM;

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
		deltaG = pressureFunction.getGreenShareChange(network, 
				scalingFactor);
		deltaM = pressureFunction.getGreenSplitChange(
				network,scalingFactor);

		containers.parallelStream().forEach(container -> {
			if (runner.isCancelled()) return;

			optimizer.process(container, network);
			runner.completedContainers.set(runner.completedContainers.get()+1);
		});


		// update signal timings
//		throw new RuntimeException("Not yet implemented");
		network.getNodes().parallelStream()
		.filter(
				node -> node instanceof SignalizedNode
				)
		.map(
				node-> (SignalizedNode) node
				)
		.forEach(
				node ->{
					node.updateGroupGreenShares(deltaG);
					node.updateMovementGreenShares(deltaM);
				}
				);
		deltaG = null;
		deltaM = null;

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
				SignalGroup sg = ((SignalizedNode) l.getHead()).getSignalGroup(l);
				for (TurningMovement mvmt : 
					sg.getMovements(l)) {
					if (mvmt.getHead() == nextLink) {
						sum += l.pricePrime(asp.getBush().valueOfTime()) 
								+ pressureFunction.delayPrime(mvmt,
										// TODO handle linked movements
										deltaG.get(sg)*deltaM.get(mvmt),
										0.);
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
				SignalGroup sg = ((SignalizedNode) l.getHead()).getSignalGroup(l);
				for (TurningMovement mvmt : sg.getMovements(l)) {
					
					if (mvmt.getHead() == nextLink) {
						sum += l.pricePrime(asp.getBush().valueOfTime()) 
								+ pressureFunction.delayPrime(mvmt,
										// TODO handle linked movements
										deltaG.get(sg)*deltaM.get(mvmt),
										0.);
						break;
					}
				}

			}
			nextLink = l;
		}
		return sum;
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
