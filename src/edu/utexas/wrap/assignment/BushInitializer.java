package edu.utexas.wrap.assignment;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.assignment.bush.Bush;
import edu.utexas.wrap.demand.ODMatrix;
import edu.utexas.wrap.net.Graph;

public class BushInitializer implements AssignmentInitializer<Bush>{
	
	private Graph network;
	private AssignmentProvider<Bush> provider;
	private AssignmentConsumer<Bush> consumer;
	private AssignmentBuilder<Bush> builder;
	private Stream<Bush> containers;
	
	public BushInitializer(
			AssignmentProvider<Bush> provider, 
			AssignmentConsumer<Bush> consumer, 
			AssignmentBuilder<Bush> builder,
			Graph network) {
		this.provider = provider;
		this.consumer = consumer;
		this.builder = builder;
		this.network = network;
	}
	
	public void add(ODMatrix matrix) {
		Stream<Bush> rawBushes = matrix.getOrigins()
				.parallelStream().map(tsz -> new Bush(
				tsz, 
				matrix.getVOT(), 
				matrix.getMode(),
				matrix.getDemandMap(tsz)));
		
		containers = containers == null? rawBushes :
			Stream.concat(containers, rawBushes);
	}
	
	private void loadContainer(Bush container) {
		boolean needsWriting = false;
		try{
			provider.getStructure(container);
		} catch (IOException e) {
			//TODO this can be wrapped into the same provider inside AssignmentBuilder
			System.err.println("\r\nINFO: Could not find source for "+container+". Building from free-flow network");
			builder.buildStructure(container);
			needsWriting = true;
		}
		
		network.loadDemand(container);
		
		if (needsWriting) try{
			consumer.consumeStructure(container);
		} catch (IOException e) {
			System.err.println("WARN: Could not write structure for "+container+". Source may be corrupted");
		}
	}
	
	public Collection<Bush> initializeContainers() {
		return containers.peek(this::loadContainer).collect(Collectors.toSet());
	}
}
