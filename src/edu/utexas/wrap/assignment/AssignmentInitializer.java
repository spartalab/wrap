package edu.utexas.wrap.assignment;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.net.Graph;

public class AssignmentInitializer<C extends AssignmentContainer> {
	
	private Graph network;
	private AssignmentReader<C> reader;
	private AssignmentWriter<C> writer;
	private AssignmentBuilder<C> builder;
	
	public AssignmentInitializer(AssignmentReader<C> reader, AssignmentWriter<C> writer, AssignmentBuilder<C> builder) {
		this.reader = reader;
		this.writer = writer;
		this.builder = builder;
	}
	
	private void loadContainer(C container) {
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
	
	public Collection<C> initialize(Stream<C> containers) {
		return containers.peek(this::loadContainer).collect(Collectors.toSet());
	}
}
