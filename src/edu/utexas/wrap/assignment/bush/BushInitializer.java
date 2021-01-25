package edu.utexas.wrap.assignment.bush;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.assignment.AssignmentBuilder;
import edu.utexas.wrap.assignment.AssignmentConsumer;
import edu.utexas.wrap.assignment.AssignmentInitializer;
import edu.utexas.wrap.assignment.AssignmentProvider;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.net.Graph;

public class BushInitializer implements AssignmentInitializer<Bush>{
	
	private Graph network;
	private AssignmentProvider<Bush> provider;
	private AssignmentConsumer<Bush> consumer,altConsumer;
	private AssignmentBuilder<Bush> builder;
	private Stream<Bush> containers;
	
	public BushInitializer(
			AssignmentProvider<Bush> provider, 
			AssignmentConsumer<Bush> consumer, 
			AssignmentConsumer<Bush> altConsumer,
			AssignmentBuilder<Bush> builder,
			Graph network) {
		this.provider = provider;
		this.consumer = consumer;
		this.builder = builder;
		this.network = network;
		this.altConsumer = altConsumer;
	}
	
	public void add(ODMatrix matrix, Float vot) {
		Stream<Bush> rawBushes = matrix.getZones()
				.parallelStream()
				.filter(tsz -> !matrix.getDemandMap(tsz).isEmpty())
				.map(tsz -> new Bush(
						network.getNode(tsz.getID()), 
						vot, 
						matrix.getMode(),
						matrix.getDemandMap(tsz))
						);

		containers = containers == null? rawBushes :
			Stream.concat(containers, rawBushes);
	}
	
	private void loadContainer(Bush bush) {
		boolean needsWriting = false;
		try{
			provider.getStructure(bush);
		} catch (IOException e) {
			//TODO this can be wrapped into the same provider inside AssignmentBuilder
			System.err.println("INFO: Could not find source for "+bush+". Building from free-flow network");
			builder.buildStructure(bush);
			needsWriting = true;
		}
		
		network.loadDemand(bush);
		
		if (needsWriting) try{
			consumer.consumeStructure(bush);
		} catch (IOException e) {
			System.err.println("WARN: Could not write structure for "+bush+". Source may be corrupted");
		} else try {
			altConsumer.consumeStructure(bush);
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public Collection<Bush> initializeContainers() {
		return containers.peek(this::loadContainer).collect(Collectors.toSet());
	}
}
