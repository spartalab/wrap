package edu.utexas.wrap.assignment.bush;

import java.io.IOException;
import java.util.stream.Stream;

import edu.utexas.wrap.assignment.AssignmentEvaluator;
import edu.utexas.wrap.assignment.AssignmentContainer;
import edu.utexas.wrap.assignment.AssignmentReader;
import edu.utexas.wrap.assignment.AssignmentWriter;
import edu.utexas.wrap.net.Graph;

public class AssignmentGapEvaluator<T extends AssignmentContainer> implements AssignmentEvaluator<T> {
	
	private Graph network;
	private AssignmentReader<T> reader;
	private AssignmentWriter<T> writer;
	private Double systemIncurredCost, cheapestPossibleCost; //total system general cost, total cheapest path general cost

	@Override
	public double getValue(Stream<T> containerStream) {
		systemIncurredCost = 0d;
		cheapestPossibleCost = 0d;
		
		containerStream.forEach(this::process);
		
		return (systemIncurredCost - cheapestPossibleCost)/cheapestPossibleCost;
	}

	private void process(T container) {
		
		try{
			reader.readStructure(container);
		} catch (IOException e) {
			System.err.println("WARN: Could not read source for "+container+". Ignoring");
			return;
		}
		
		double incurredCost = container.incurredCost();
		double cheapestContainerCost = network.cheapestCostPossible(container);
		 
		
		synchronized (systemIncurredCost) {
			systemIncurredCost += incurredCost;
		}
		synchronized (cheapestPossibleCost) {
			cheapestPossibleCost += cheapestContainerCost;
		}
		
		try {
			writer.writeStructure(container);
		} catch (IOException e) {
			System.err.println("WARN: Could not write structure for "+container+". Source may be corrupted");
		}
	}

}
