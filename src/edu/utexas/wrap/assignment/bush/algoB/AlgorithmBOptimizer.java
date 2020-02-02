package edu.utexas.wrap.assignment.bush.algoB;

import java.io.IOException;
import java.util.stream.Stream;

import edu.utexas.wrap.assignment.AssignmentOptimizer;
import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.assignment.bush.BushEvaluator;
import edu.utexas.wrap.assignment.bush.BushReader;
import edu.utexas.wrap.assignment.bush.BushWriter;

public class AlgorithmBOptimizer implements AssignmentOptimizer<Bush> {
	private BushReader reader;
	private AlgorithmBUpdater updater;
	private AlgorithmBEquilibrator equilibrator;
	private BushWriter writer;
	
	private Class<? extends BushEvaluator> evaluatorClass;
	private double threshold;

	@Override
	public void optimize(Stream<Bush> bushStream) {
		bushStream.forEach( bush -> {
			try{
				reader.readStructure(bush);
			} catch (IOException e) {
				System.err.println("WARN: Could not find source for "+bush+". Ignoring");
				return;
			}
			synchronized (this) {
				updater.update(bush);
				
				while (bush.getEvaluator(evaluatorClass).getValue() < threshold) {
					equilibrator.equilibrate(bush);
				}
			}
			
			try {
				writer.writeStructure(bush);
			} catch (IOException e) {
				System.err.println("WARN: Could not write structure for "+bush+". Source may be corrupted");
			}
		});
	}
}
