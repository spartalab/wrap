package edu.utexas.wrap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

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
				Link shortLink = b.getqShort(cur);
				Link longLink = b.getqLong(cur);
				if (longLink.equals(shortLink)) {
					// If there is no divergence node
					continue;
				}
				else {
					//TODO calculate divergence node
					LinkedList<Link> uPath = new LinkedList<Link>();
					LinkedList<Link> lPath = new LinkedList<Link>();
					
					Integer lenLong = 2;
					Integer lenShort = 2;
					
					Node longNode = longLink.getTail();
					Node shortNode = shortLink.getTail();
					Node divergence;
					while (!longNode.equals(o)) {
						lenLong++;
						longNode = b.getqLong(longNode).getTail();
					}
					while (!shortNode.equals(o)) {
						lenShort++;
						shortNode = b.getqShort(shortNode).getTail();
					}
					
					Node u = cur, l = cur;
					if (lenLong > lenShort) {
						
						for (int i = 0; i < lenLong - lenShort; i++) {
							u = b.getqLong(u).getTail();
						}
					} else {
						
						for (int i = 0; i < lenShort - lenLong; i++) {
							l = b.getqShort(l).getTail();
						}
					}
					
					shortLink = b.getqShort(l);
					longLink = b.getqLong(u);
					
					while (longLink != null && shortLink != null) {
						u = longLink.getTail();
						l = shortLink.getTail();
						if (!u.equals(l)){
							longLink = b.getqLong(u);
							shortLink = b.getqShort(u);
						}
						else break;
					}
					divergence = l;
					
				}
				//TODO build pair of alternate segments pi_L and pi_U
				
				//TODO calculate delta h, capping at zero
				
				//TODO add delta h to all x values in pi_L
				
				//TODO subtract delta h from all x values in pi_U
				
				
				
			}
		}
		
	}
	
}