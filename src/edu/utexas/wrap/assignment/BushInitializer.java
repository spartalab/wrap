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
	private AssignmentReader<Bush> reader;
	private AssignmentWriter<Bush> writer;
	private AssignmentBuilder<Bush> builder;
	private Stream<Bush> containers;
	
	public BushInitializer(
			AssignmentReader<Bush> reader, 
			AssignmentWriter<Bush> writer, 
			AssignmentBuilder<Bush> builder) {
		this.reader = reader;
		this.writer = writer;
		this.builder = builder;
	}
	
	public void add(ODMatrix matrix) {
		Stream<Bush> rawBushes = matrix.getOrigins().parallelStream().map(tsz -> new Bush(
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
			reader.readStructure(container);
		} catch (IOException e) {
			System.err.println("INFO: Could not find source for "+container+". Building from free-flow network");
			builder.buildStructure(container);
			needsWriting = true;
		}
		
		network.loadDemand(container);
		
		if (needsWriting) try{
			writer.writeStructure(container);
		} catch (IOException e) {
			System.err.println("WARN: Could not write structure for "+container+". Source may be corrupted");
		}
	}
	
	public Collection<Bush> initializeContainers() {
		return containers.peek(this::loadContainer).collect(Collectors.toSet());
	}
}
