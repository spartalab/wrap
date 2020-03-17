package edu.utexas.wrap.assignment.bush;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;

public class PathCostCalculator {
	Double[] shortCache, longCache;
	Bush bush;

	public PathCostCalculator(Bush bush) {
		this.bush = bush;
	}
	
	public boolean checkForShortcut(Link l) {
		synchronized (this) {
			if (longCache == null) getLongestPathCosts(true);
		}

		Double uTail = longCache[l.getTail().getOrder()];
		Double uHead = longCache[l.getHead().getOrder()];
		
		if (uTail == null || uHead == null) return false;
		
		return uTail + l.getPrice(bush) < uHead;
	}
	
	public Link getShortestPathLink(BushMerge bm) {
		synchronized (this) {
			if (shortCache == null) getShortestPathCosts();
		}

		return bm.getLinks().parallel().min(Comparator.comparing( (Link x) -> 
		shortCache[x.getTail().getOrder()]+x.getPrice(bush)
				)).orElseThrow(RuntimeException::new);
	}

	private void getShortestPathCosts() {
		// TODO Auto-generated method stub
		bush.clear();
		Node[] to = bush.getTopologicalOrder(false);
		shortCache = new Double[bush.size()];

		//In topological order,
		for (Node d : to) {
			if (d == null) continue;
			else if (d == bush.root().node()) shortCache[d.getOrder()] = 0.;

			BackVector bv = bush.getBackVector(d);
			if (bv instanceof Link) shortRelax((Link) bv);
			else if (bv instanceof BushMerge) 
				((BushMerge) bv).getLinks().sequential().forEach( l ->
				//Try to relax all incoming links
				shortRelax(l)
						);

		}
	}
	
	/** Relax the shortest path while doing topological shortest path search
	 * @param l the link to examine as  a candidate for shortest path
	 * @param cache a cache of shortest path costs
	 * @throws UnreachableException if a node can't be reached
	 */
	private void shortRelax(Link l) {
		//Calculate the cost of adding this link to the shortest path
		Double Licij = l.getPrice(bush) + shortCache[l.getTail().getOrder()];
		Node head = l.getHead();
		BackVector back = bush.getBackVector(head);
		
		//If the shortest path doesn't exist, already flows through the link, or this has a lower cost,
		if (shortCache[head.getOrder()] == null || Licij < shortCache[head.getOrder()]) {
			//Update the BushMerge, if applicable
			if (back instanceof BushMerge) ((BushMerge) back).setShortLink(l);
			//Store this cost in the cache
			shortCache[head.getOrder()] = Licij;
		}
	}

	public Link getLongestPathLink(BushMerge bm) {
		synchronized (this) {
			if (longCache == null) getLongestPathCosts(true);
		}
		
		return bm.getLinks().parallel().max(Comparator.comparing((Link x) ->
		longCache[x.getTail().getOrder()]+x.getPrice(bush)
				)).orElseThrow(RuntimeException::new);
	}
	
	private void getLongestPathCosts(boolean longestUsed) {
		bush.clear();
		Node[] to = bush.getTopologicalOrder(false);
		longCache = new Double[bush.size()];

		//In topological order,
		for (Node d : to) {
			if (d == null) continue;
			else if (d == bush.root().node()) longCache[d.getOrder()] = 0.;
			
			//Try to relax the backvector (all links in the BushMerge, if applicable)
			BackVector bv = bush.getBackVector(d);
			if (bv instanceof Link) longRelax((Link) bv);
			else if (bv instanceof BushMerge) {
				BushMerge bm = (BushMerge) bv;
				for (Link l : bm.getLinks()
						.filter(l -> !longestUsed || bm.getSplit(l) > 0)
						.collect(Collectors.toSet())) 
					longRelax(l);
			}

		}		
		return;
	}
	
	private void longRelax(Link l) {
		//Calculate the cost of adding the link to the longest path
		Double Uicij = l.getPrice(bush) 
				+ longCache[l.getTail().getOrder()];
		Node head = l.getHead();

		BackVector back = bush.getBackVector(head);
		//If the longest path doesn't exist, is already through the proposed link, or the cost is longer,
		if (longCache[head.getOrder()] == null || Uicij > longCache[head.getOrder()]) {
			//Update the BushMerge if need be
			if (back instanceof BushMerge) ((BushMerge) back).setLongLink(l);
			//Store the cost in the cache
			longCache[head.getOrder()] = Uicij;
		}
	}

	public Link getqShort(Node node) {
		BackVector bv = bush.getBackVector(node);
		return bv instanceof Link? (Link) bv : 
			bv instanceof BushMerge? getShortestPathLink((BushMerge) bv) : 
				null;
	}
	
	public Link getqLong(Node node) {
		BackVector bv = bush.getBackVector(node);
		return bv instanceof Link? (Link) bv :
			bv instanceof BushMerge? getLongestPathLink((BushMerge) bv) :
				null;
	}

	/** Calculates the divergence between the shortest and longest paths from two nodes
	 * @param l the node from which the shortest path should trace back
	 * @param u the node from which the longest path should trace back
	 * @return the diverge node
	 */
	public Node divergeNode(Node start) {
		// If the given node is the origin or it has only one backvector, there is no diverge node
		if (start.equals(bush.root().node()) || !(bush.getBackVector(start) instanceof BushMerge))
			return start;

		//Store the nodes seen on the paths in reverse order
		List<Node> lNodes = new ArrayList<Node>();
		List<Node> uNodes = new ArrayList<Node>();

		//Iterate backwards through the shortest and longest paths until one runs out of links
		Link uLink = getqLong(start);
		Link lLink = getqShort(start);
		while (lLink != null && uLink != null) {

			Node lNode = lLink.getTail();
			Node uNode = uLink.getTail();

			//If the next node in both is the same, return that node
			if (lNode.equals(uNode)) return lNode;
			//If the shortest path just got to a node that was already seen in the longest path, return it
			else if (uNodes.contains(lNode)) return lNode;
			//If the longest path just got to a node that was already seen in the shortest path, return it
			else if (lNodes.contains(uNode)) return uNode;
			//Otherwise, add both to the lists of seen nodes
			else {
				lNodes.add(lLink.getTail());
				uNodes.add(uLink.getTail());
			}
			
			lLink = getqShort(lNode);
			uLink = getqLong(uNode);
		}
		//If there are still more links to examine on the longest path, do so
		while (uLink != null) {
			Node uNode = uLink.getTail();
			//If the longest path just got to a node that was already seen in the shortest path, return it
			if (lNodes.contains(uNode)) return uNode;
			uLink = getqLong(uNode);
		}
		//If there are still more links to examine on the shortest path, do so
		while (lLink != null) {
			Node lNode = lLink.getTail();
			//If the shortest path just got to a node that was already seen in the longest path, return it
			if (uNodes.contains(lNode))	return lNode;
			lLink = getqShort(lNode);
		}
		//Something went wrong - the two paths never intersected
		throw new RuntimeException("No diverge node found");
	}

	public Bush getBush() {
		return bush;
	}
}
