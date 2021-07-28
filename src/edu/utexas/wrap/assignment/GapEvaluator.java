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
package edu.utexas.wrap.assignment;

import java.io.IOException;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.stream.Stream;

import edu.utexas.wrap.net.Graph;

public class GapEvaluator<T extends AssignmentContainer> implements AssignmentEvaluator<T> {
	
	private Graph network;
	private AssignmentProvider<T> provider;
	private AssignmentConsumer<T> consumer;
//	private Double systemIncurredCost, cheapestPossibleCost; //total system general cost, total cheapest path general cost
	private DoubleAdder incurredCost, cheapestCost;
	
	public GapEvaluator(Graph network,
			AssignmentProvider<T> provider,
			AssignmentConsumer<T> consumer) {
		this.network = network;
		this.provider = provider;
		this.consumer = consumer;
	}
	

	@Override
	public double getValue(Stream<T> containerStream) {
//		systemIncurredCost = 0d;
//		cheapestPossibleCost = 0d;
		
		containerStream.forEach(this::process);
		
		return (incurredCost.sum() - cheapestCost.sum())/cheapestCost.sum();
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
		 
		
//		synchronized (this.incurredCost) {
			this.incurredCost.add(incurredCost);
//			systemIncurredCost += incurredCost;
//		}
//		synchronized (this.cheapestCost) {
			this.cheapestCost.add(cheapestContainerCost);
//			cheapestPossibleCost += cheapestContainerCost;
			
//		}
		
		try {
			consumer.consumeStructure(container);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("WARN: Could not consume "+container+".");
			e.printStackTrace();
		}
	}

}
