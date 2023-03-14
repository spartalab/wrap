package edu.utexas.wrap.assignment.bush.signalized;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import edu.utexas.wrap.assignment.AssignmentConsumer;
import edu.utexas.wrap.assignment.AssignmentOptimizer;
import edu.utexas.wrap.assignment.AssignmentProvider;
import edu.utexas.wrap.assignment.AtomicOptimizer;
import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.assignment.bush.BushEvaluator;
import edu.utexas.wrap.assignment.bush.PathCostCalculator;
import edu.utexas.wrap.assignment.bush.algoB.AlgorithmBUpdater;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;

public class SignalizedOptimizer 
		implements AtomicOptimizer<Bush> {
	private final AssignmentProvider<Bush> provider;
	private final AssignmentConsumer<Bush> consumer;;

	private final BushEvaluator evaluator;
	private double threshold;
	private int maxIterations = 25;
	
	private final AlgorithmBUpdater updater;


	public SignalizedOptimizer(
			AssignmentProvider<Bush> provider, 
			AssignmentConsumer<Bush> writer,
			BushEvaluator iterEvaluator, 
			double iterThreshold) {
		// TODO Auto-generated constructor stub
		this.provider = provider;
		this.consumer = writer;
		this.evaluator = iterEvaluator;
		this.threshold = iterThreshold;
		updater = new AlgorithmBUpdater();
	}

	@Override
	public void process(Bush bush, 
			Graph network,
			Map<Link,Double> flows,
			Map<Link,Double> greenShares,
			Map<Link,Double> bottleneckDelays,
			Map<Link,Double> costs) {
		// TODO Auto-generated method stub

		try{
			provider.getStructure(bush, network);
		} catch (IOException e) {
			System.err.println("WARN: Could not find source for "+bush+". Ignoring");
			return;
		}
//
//		PathCostCalculator pcc = new PathCostCalculator(bush);
//		updater.update(bush, pcc);
//		
//		for (int i = 0; i < maxIterations;i++) {
//			try {
//				equilibrator.equilibrate(bush, pcc);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (ExecutionException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			Double val = evaluator.getValue(bush, network);
//			//							System.out.println(val);
//			if (val < threshold) break;
//			pcc.clear();
//		}
		

		try {
			consumer.consumeStructure(bush, network);
		} catch (IOException e) {
			System.err.println("WARN: Could not write structure for "+bush+". Source may be corrupted");
		}
	}

	@Override
	public void initialize() {
		// TODO Auto-generated method stub

	}

	@Override
	public void process(Bush container, Graph network) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}

}
