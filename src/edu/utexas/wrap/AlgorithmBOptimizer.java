package edu.utexas.wrap;

import java.util.Iterator;

public class AlgorithmBOptimizer extends Optimizer{

	public AlgorithmBOptimizer(Network network) {
		super(network);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void optimize() {
		// TODO Auto-generated method stub
		
		for (Origin o : getNetwork().getOrigins()) {
			Bush b = o.getBush();
			Iterator<Node> topOrder = b.getTopologicalOrder().descendingIterator();
			
			
			while (topOrder.hasNext()) {
				Node cur = topOrder.next();

				if (b.getqLong(cur).equals(b.getqShort(cur))) {
					// If there is no divergence node
					continue;
				}
				else {
					//TODO calculate divergence node
				}
				//TODO build pair of alternate segments pi_L and pi_U
				
				//TODO calculate delta h, capping at zero
				
				//TODO add delta h to all x values in pi_L
				
				//TODO subtract delta h from all x values in pi_U
				
				
				
			}
		}
		
	}
	
}