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
	
	private AssignmentProvider<Bush> provider;
	private AssignmentConsumer<Bush> writer, forgetter;
	private AssignmentBuilder<Bush> builder;
	private Stream<Bush> containers;
	
	public BushInitializer(
			AssignmentProvider<Bush> provider, 
			AssignmentConsumer<Bush> writer,
			AssignmentConsumer<Bush> forgetter,
			AssignmentBuilder<Bush> builder) {
		this.provider = provider;
		this.writer = writer;
		this.forgetter = forgetter;
		this.builder = builder;
	}
	
	public void add(Graph network, ODMatrix matrix, Float vot) {
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
	
	private void loadContainer(Graph network, Bush bush) {
		boolean needsWriting = false;
		try{
			provider.getStructure(bush, network);
		} catch (IOException e) {
			//TODO this can be wrapped into the same provider inside AssignmentBuilder
			System.err.println("INFO: Could not find source for "+bush+". Building from free-flow network");
			builder.buildStructure(bush, network);
			needsWriting = true;
		}
		
		network.loadDemand(bush);
		
		try{
			if (needsWriting) writer.consumeStructure(bush, network);
			else forgetter.consumeStructure(bush, network);
		} catch (IOException e) {
			System.err.println("WARN: Could not write structure for "+bush+". Source may be corrupted");
		
		}
	}
	
	public Collection<Bush> initializeContainers(Graph network) {
		Stream<Bush> tmp = containers;
		containers = null;
		return tmp.peek(bush -> loadContainer(network, bush)).collect(Collectors.toSet());
	}
}
