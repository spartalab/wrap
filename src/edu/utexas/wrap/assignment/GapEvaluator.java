package edu.utexas.wrap.assignment;

import java.io.IOException;
import java.util.stream.Stream;

import edu.utexas.wrap.net.Graph;

public class GapEvaluator<T extends AssignmentContainer> implements AssignmentEvaluator<T> {
	
	private Graph network;
	private AssignmentProvider<T> provider;
	private AssignmentConsumer<T> consumer;
	private Double systemIncurredCost, cheapestPossibleCost; //total system general cost, total cheapest path general cost
	
	public GapEvaluator(Graph network,
			AssignmentProvider<T> provider,
			AssignmentConsumer<T> consumer) {
		this.network = network;
		this.provider = provider;
		this.consumer = consumer;
	}
	

	@Override
	public double getValue(Stream<T> containerStream) {
		systemIncurredCost = 0d;
		cheapestPossibleCost = 0d;
		
		containerStream.forEach(this::process);
		
		return (systemIncurredCost - cheapestPossibleCost)/cheapestPossibleCost;
	}

	private void process(T container) {
		
		try{
			provider.getStructure(container);
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
			consumer.consumeStructure(container);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("WARN: Could not consume "+container+".");
			e.printStackTrace();
		}
	}

}
