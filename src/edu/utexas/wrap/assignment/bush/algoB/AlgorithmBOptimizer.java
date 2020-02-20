package edu.utexas.wrap.assignment.bush.algoB;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import edu.utexas.wrap.assignment.AssignmentOptimizer;
import edu.utexas.wrap.assignment.AssignmentReader;
import edu.utexas.wrap.assignment.AssignmentWriter;
import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.assignment.bush.BushEvaluator;

public class AlgorithmBOptimizer implements AssignmentOptimizer<Bush> {
	private final AssignmentReader<Bush> reader;
	private final AssignmentWriter<Bush> writer;
	
	private final AlgorithmBUpdater updater;
	private final AlgorithmBEquilibrator equilibrator;

	private Collection<Bush> queue;
	
	private final BushEvaluator evaluator;
	private double threshold;
	
	public AlgorithmBOptimizer(
			AssignmentReader<Bush> reader, 
			AssignmentWriter<Bush> writer, 
			BushEvaluator evaluator,
			double threshold) {
		
		this.reader = reader;
		this.writer = writer;
		this.evaluator = evaluator;
		
		updater = new AlgorithmBUpdater();
		equilibrator = new AlgorithmBEquilibrator();
	}

	@Override
	public void optimize(Stream<Bush> bushStream) {
		queue = new ConcurrentLinkedQueue<Bush>();
		
		bushStream.forEach( bush -> {
			try{
				reader.readStructure(bush);
			} catch (IOException e) {
				System.err.println("WARN: Could not find source for "+bush+". Ignoring");
				return;
			}
			
			updater.update(bush);
			
			try {
				writer.writeStructure(bush);
			} catch (IOException e) {
				System.err.println("WARN: Could not write structure for "+bush+". Source may be corrupted");
			}
			
			queue.add(bush);
		});
		
		while (!queue.isEmpty()) {
			Bush[] q = new Bush[queue.size()];
	
			Stream.of(queue.toArray(q)).parallel()
			.forEach(bush -> {
	
				try{
					reader.readStructure(bush);
				} catch (IOException e) {
					System.err.println("WARN: Could not find source for "+bush+". Ignoring");
					return;
				}
				
				synchronized (this) {
						equilibrator.equilibrate(bush);
				}
				if (evaluator.getValue(bush) < threshold) queue.remove(bush);
				
				try {
					writer.writeStructure(bush);
				} catch (IOException e) {
					System.err.println("WARN: Could not write structure for "+bush+". Source may be corrupted");
				}
			});
		}
	}
}
