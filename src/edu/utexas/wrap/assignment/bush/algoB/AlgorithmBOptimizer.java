/*
    wrap - free and open-source urban transportation modeling software
    Copyright (C) 2017 the wrap project, The University of Texas at Austin

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package edu.utexas.wrap.assignment.bush.algoB;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.function.ToDoubleFunction;

import edu.utexas.wrap.assignment.AssignmentProvider;
import edu.utexas.wrap.assignment.AssignmentConsumer;
import edu.utexas.wrap.assignment.bush.AlternateSegmentPair;
import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.assignment.bush.BushEvaluator;
import edu.utexas.wrap.assignment.bush.PathCostCalculator;
import edu.utexas.wrap.net.Graph;

public class AlgorithmBOptimizer implements ParallelizedOptimizer<Bush> {
	private final AssignmentProvider<Bush> provider;
	private final AssignmentConsumer<Bush> consumer; 
	private final ToDoubleFunction<AlternateSegmentPair> stepSizeFunc;

	private final AlgorithmBUpdater updater;
	private final AlgorithmBEquilibrator equilibrator;

	private final BushEvaluator evaluator;
	private double threshold;
	private int maxIterations = 25;

	public AlgorithmBOptimizer(
			AssignmentProvider<Bush> provider, 
			AssignmentConsumer<Bush> consumer, 
			BushEvaluator evaluator,
			double threshold,
			ToDoubleFunction<AlternateSegmentPair> stepSizeFunc) {

		this.provider = provider;
		this.consumer = consumer;
		this.evaluator = evaluator;
		this.threshold = threshold;

		updater = new AlgorithmBUpdater();
		equilibrator = new AlgorithmBEquilibrator();
		this.stepSizeFunc = stepSizeFunc;
	}

	@Override
	public void process(Bush bush, Graph network) {
		try{
			provider.getStructure(bush, network);
		} catch (IOException e) {
			System.err.println("WARN: Could not find source for "+bush+". Ignoring");
			return;
		}

		PathCostCalculator pcc = new PathCostCalculator(bush);
		updater.update(bush, pcc);
		
		for (int i = 0; i < maxIterations;i++) {
			try {
				equilibrator.equilibrate(bush, pcc, stepSizeFunc);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Double val = evaluator.getValue(bush, network);
			//							System.out.println(val);
			if (val < threshold) break;
			pcc.clear();
		}
		

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


}
