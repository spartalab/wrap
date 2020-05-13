package edu.utexas.wrap.assignment.bush;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.utexas.wrap.assignment.AssignmentContainer;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.modechoice.Mode;
import edu.utexas.wrap.net.CentroidConnector;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.net.TravelSurveyZone;

/** An instance of a {@link edu.utexas.wrap.assignment.AssignmentContainer}
 * that is used for bush-based assignment methods.
 * @author William
 *
 */
public class Bush implements AssignmentContainer {

	public static boolean orderCachingEnabled = true;
	public static boolean flowCachingEnabled = false;
	// Bush attributes
	private final TravelSurveyZone origin;
	private final Float vot;
	private final Mode c;
	
	//Underlying problem characteristics
	private final DemandMap demand;

	// Back vector map (i.e. the bush structure)
	private BackVector[] q;

	//Topological order can be cached for expediency
	private Node[] cachedTopoOrder;
	private Map<Link,Double> cachedFlows;
	

	/** Default constructor
	 * @param o the root of the bush
	 * @param g the graph underlying the bush
	 * @param vot the value of time
	 * @param mode the demand to be carried on the bush
	 * @param demandMap the mode of travel for the bush
	 */
	public Bush(TravelSurveyZone origin, Float vot, Mode mode, DemandMap demandMap) {
		this.origin = origin;
		this.vot = vot;
		this.c = mode;
		demand = demandMap;
	}

	/** Add a link to the bush
	 * @param l the link to be added
	 * @return whether the link was successfully added to the bush
	 */
	public boolean add(Link l) {
		Boolean ret = false;
		Node head = l.getHead();
		BackVector prior = getBackVector(head);
		// If there was no backvector (should only happen during bush initialization), add link
		if (prior == null) {
			setBackVector(head, l);
			ret = true;
		}
		// If the link is already the sole backvector, do nothing
		else if (prior.equals(l)) ret = false;
		// If the head node already has a merge backvector, delegate 
		else if (prior instanceof BushMerge) {
			ret = ((BushMerge) prior).add(l);
		}
		// Else, we need to create a new merge backvector to replace the current back link
		else if (prior instanceof Link) { // If we're here, the back link needs to be replaced by a merge
			BushMerge nuevo = new BushMerge(this, (Link) prior, l);
			setBackVector(head, nuevo);
			ret = true;
		}
		if (ret) cachedTopoOrder = null;
		return ret;
	}

	/**Set the backvector of a given node
	 * @param head the node where the BackVector leads
	 * @param backvector the BackVector associated with the node
	 */
	void setBackVector(Node head, BackVector backvector) {
		q[head.getOrder()] = backvector;
	}

	protected void setQ(BackVector[] q) {
		this.q = q;
	}
	
	public Stream<BackVector> getQ(){
		return Stream.of(q);
	}
	
	public int size() {
		return q.length;
	}

	/**Generate a topological ordering from scratch for this bush
	 * @return Nodes in topological order
	 */
	private Node[] generateTopoOrder(boolean toCache) {
		// Start with a set of all bush edges
		Collection<Link> currentLinks = usedLinks();

		Node[] to = new Node[q.length];
		LinkedList<Node> S = new LinkedList<Node>();
		// "start nodes"
		S.add(origin.node());
		Node n;
		int pos = 0;

		while (!S.isEmpty()) {
			n = S.pop();// remove node from S
			to[pos] = n; // append node to L
			pos++;
			
			// for each active edge out of this node
			for (Link l : n.forwardStar()) {
				if (currentLinks.contains(l)) {

					// remove the links from the set
					currentLinks.remove(l);
					// the node on the other end
					Node m = l.getHead();

					// see if this node has no other incoming active links
					boolean mHasIncoming = false;
					for (Link e : m.reverseStar()) {
						if (currentLinks.contains(e)) {
							mHasIncoming = true;
							break;
						}
					}
					// if not, add to the list of start nodes
					if (!mHasIncoming)
						S.add(m);
				}
			}
		}
		if (!currentLinks.isEmpty())
			throw new RuntimeException("Cyclic graph error");
		if (Bush.orderCachingEnabled || toCache) cachedTopoOrder = to;
		return to;
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.AssignmentContainer#getDemand(edu.utexas.wrap.net.Node)
	 */
	public Float getDemand(Node node) {
		return node.getZone() == null ? 0.0f : demand.get(node.getZone());
	}
	
	/**
	 * @return the demand map associated with this bush
	 */
	public DemandMap getDemandMap(){
		return demand;
	}
	
	/**Get the bush's origin
	 * @return the origin of the bush
	 */
	public TravelSurveyZone root() {
		return origin;
	}

	/**Get the backvector at a given node
	 * @param node where the BackVector leads
	 * @return the backvector from this bush leading to this node
	 */
	public BackVector getBackVector(Node node) {
		int index = node.getOrder();
		if (index == -1) 
			throw new RuntimeException();
		return q[index];
	}

	/**
	 * Calculate a topological order using Kahn's algorithm
	 * 
	 * Evaluate the set of bush links, starting from the origin and determine a
	 * topological order for the nodes that they attach
	 * 
	 * @return a topological ordering of this bush's nodes
	 */
	public synchronized Node[] getTopologicalOrder(boolean toCache) {
		return (cachedTopoOrder != null) ? cachedTopoOrder : generateTopoOrder(toCache);
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.AssignmentContainer#getVehicleClass()
	 */
	public Mode vehicleClass() {
		return c;
	}

	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.AssignmentContainer#getVOT()
	 */
	public Float valueOfTime() {
		return vot;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		//origin;vot;c;
		int a = 2017;	//	Year of inception for this project
		int b = 76537;	//	UT 76-5-37 TAMC \m/
		
		return (
				origin.node().getID()*a + vot.intValue()
				)*b + (c == null? 0 : c.hashCode());
	}
	
	/**Determine whether a link can be used in the bush
	 * @param uv the link to determine candidacy
	 * @return true if the link can be used in the bush
	 */
	public boolean canUseLink(Link uv) {
		//If the link is a centroid connector
		return uv instanceof CentroidConnector? 
				//that doesn't lead from the origin and
				uv.getTail().equals(origin.node())? true :
					//leads from a different centroid instead
					(uv.getHead().isCentroid() && !uv.getTail().isCentroid())? true:
						//then we can't use the link in the bush
						false
				//Otherwise, we can
				: true;
	}

	/**
	 * @param l link to be removed
	 * @return whether the link was removed successfully
	 */
	private boolean remove(Link l) {
		BackVector b = getBackVector(l.getHead());
		// If there was a merge present at the head node, attempt to remove the link from it
		if (b instanceof BushMerge) {
			BushMerge bm = (BushMerge) b;
			int size = bm.size();
			if (size > 1) {
				bm.remove(l);

				if (size==2){ //If there is only one link left, replace BushMerge with Link
					setBackVector(l.getHead(),bm.getLinks().findAny().orElseThrow(RuntimeException::new));
					return true;
				} 
			}
			return false;
		}
		else if (b instanceof Link && l.equals((Link) b)) return false;
		// If something unusual happened, throw a Runtime exception
		throw new RuntimeException("A link was removed that wasn't in the bush");
	}

	/**
	 * Remove unused links that aren't needed for connectivity
	 */
	public void prune() {
		Stream.of(q).parallel().filter(v -> v instanceof BushMerge).forEach(v ->{	//For every BushMerge in the bush

			//Duplicate the link list to avoid a ConcurrentModificationException
			BushMerge bm = new BushMerge((BushMerge) v);

			bm.getLinks()
			.filter(l -> bm.getSplit(l) <= Math.ulp(bm.getSplit(l))*20)
			.filter(l -> remove(l))
			.forEach(l -> {
				//If so, try to remove the Link
				cachedTopoOrder = null;
//				numLinks--;
			});
		});
	}
	
	/** Get all Bush flows
	 * @return a Map from a Link to the amount of flow from this Bush on the Link
	 */
	@Override
	public Map<Link, Double> flows(){
		if (cachedFlows != null) return cachedFlows;
		//Get the reverse topological ordering and a place to store node flows
		Map<TravelSurveyZone,Double> tszFlow = demand.doubleClone();
		Map<Node,Double> nodeFlow = tszFlow.keySet().parallelStream().collect(Collectors.toMap(x -> x.node(), x -> tszFlow.get(x)));
		Node[] iter = getTopologicalOrder(false);
		Map<Link,Double> ret = new HashMap<Link,Double>(size(),1.0f);

		//For each node in reverse topological order
		for (int i = iter.length - 1; i >= 0; i--) {
			Node n = iter[i];
			if (n == null) continue;
			
			//Get the node flow and the backvector that feeds it into the node
			BackVector back = getBackVector(n);
			Double downstream = nodeFlow.getOrDefault(n,0.0);

			//If there is only one backvector,all node flow must pass through it
			if (back instanceof Link) {						
				//Add the node flow onto the upstream node flow
				Link b = (Link) back;
				Node tail = b.getTail();
				nodeFlow.put(tail, nodeFlow.getOrDefault(tail,0.0) + downstream);
				ret.put(b, (double) downstream);
			}

			//If there is more than one link flowing into the node
			else if (back instanceof BushMerge) {
				((BushMerge) back).getLinks().forEach(bv ->{
					//Calculate the share of the node flow that uses each link
					Double share = ((BushMerge) back).getSplit(bv)*downstream;

					//Add the node flow onto the upstream node flow
					Node tail = bv.getTail();
					nodeFlow.put(tail, nodeFlow.getOrDefault(tail,0.0) + share.doubleValue());
					ret.put(bv, share);
				});

			}

			//If we've reached a dead end in the topological ordering, throw an exception
			else if (back == null && !n.equals(origin.node())) {
				throw new RuntimeException("Missing backvector for "+n.toString());
			}
			
		}
		if (flowCachingEnabled) cachedFlows = ret;
		return ret;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "ORIG=" + origin.node().getID() + "\tVOT=" + vot + "\tCLASS=" + c;
	}
	
	/**
	 * @return a Stream of all Links used by this bush
	 */
	public Stream<Link> getUsedLinkStream(){
		//Include all standalone Links
		Stream<Link> a = Stream.of(q).parallel().filter(bv -> bv instanceof Link).map(l -> (Link) l);
		//and Links stored inside a BackVector
		Stream<Link> b = Stream.of(q).parallel().filter(bv -> bv instanceof BushMerge).map(bv -> (BushMerge) bv).flatMap(bv -> bv.getLinks().parallel());
		return Stream.concat(a, b);
	}
	
	/* (non-Javadoc)
	 * @see edu.utexas.wrap.assignment.AssignmentContainer#getLinks()
	 */
	@Override
	public Collection<Link> usedLinks() {
		return getUsedLinkStream().collect(Collectors.toSet());
	}
	
	/**
	 * @return a Collection of all Links not used by the bush that could be added
	 */
	public Stream<Link> getUnusedLinks(){
		return Stream.of(q).parallel().flatMap(bv ->{
			if (bv instanceof Link)
				return Stream.of(bv.getHead().reverseStar()).filter(link -> link != bv);
			else if (bv instanceof BushMerge)
				return Stream.of(bv.getHead().reverseStar()).filter(link -> !((BushMerge) bv).contains(link));
			else return Stream.empty();
		});
//		return network.getLinks().parallelStream().filter(l -> 
//			(q[l.getHead().getOrder()] instanceof Link && !q[l.getHead().getOrder()].equals(l))
//			|| (q[l.getHead().getOrder()] instanceof BushMerge && !((BushMerge) q[l.getHead().getOrder()]).contains(l))
//		).collect(Collectors.toSet());
	}
	
	/**Delete any cached topological order to conserve space
	 * 
	 */
	public void clear() {
		cachedTopoOrder = null;
	}

	public boolean cycleCheck() {
		Set<Node> visited = new HashSet<Node>(demand.getGraph().numNodes(),1.0f);
		Set<Node> stack = new HashSet<Node>(demand.getGraph().numNodes(),1.0f);
		for (Node node : getNodes()) {
//			if (node.equals(origin.node())) continue;
			if (cycleCheck(node,visited,stack)) return true;
		}
		return false;
	}

	private boolean cycleCheck(Node node, Set<Node> visited, Set<Node> stack) {
		// TODO explore modifying Set to Deque
		if (stack.contains(node)) 
			return true;
		
		if (visited.contains(node)) 
			return false;
		visited.add(node);
		stack.add(node);

		
		for (Link l : node.forwardStar()) {
			if (contains(l)) {
				if (cycleCheck(l.getHead(),visited,stack)) return true;
			}
		}
		stack.remove(node);
		return false;
//		BackVector bv = getBackVector(node);
//		if (bv instanceof Link) {
//			if (cycleCheck(((Link) bv).getTail(),visited,stack,nextIndex)) return true;
//			stack.remove(node);
//			return false;
//		}
//		else if (bv instanceof BushMerge) {
//			for (Link l : ((BushMerge) bv).getLinks().collect(Collectors.toSet())) {
//				if (cycleCheck(l.getTail(),visited,stack,nextIndex)) return true;
//				stack.remove(node);
//				return false;
//			}
//		}
//		else if (bv == null && node.equals(origin.node())) return false;
//		throw new RuntimeException();
	}

	/**
	 * @return the total generalized cost for trips in this bush
	 */
	@Override
	public double incurredCost() {
		Map<Link,Double> flows = flows();
		return getUsedLinkStream().parallel().mapToDouble(l -> flows.getOrDefault(l, 0.0)*l.getPrice(vot, c)).sum();
	}
//	
//	/**
//	 * @return true if the flow conservation constraint holds
//	 */
//	boolean conservationCheck() {
//		Map<Link,Double> flows = flows();	//retrieve the current bush flows
//		
//		//For each node in the network
//		getNodes().parallelStream().forEach(n->{
//			if (n.equals(getOrigin().getNode())) return; //except the origin
//			float demand = getDemand(n);//determine what the demand is at this node
//			
//			//Look at the incoming link(s) and calculate the total inflow
//			BackVector bv = q[n.getOrder()];
//			double inFlow = bv instanceof Link? flows.get((Link) bv) :
//				bv instanceof BushMerge? ((BushMerge) bv).getLinks().mapToDouble(l -> flows.getOrDefault(l, 0.0)).sum():
//					0.0;
//				
//			//Look at the ougoing links and calculate the total outflow
//			double outFlow = Stream.of(n.forwardStar()).mapToDouble(l -> flows.getOrDefault(l, 0.0)).sum();
//			
//			//Check if the difference between outflow and (inflow + demand) is greater than the machine epsilon
//			if (outFlow - inFlow - demand > 20*Math.max(Math.ulp(outFlow), Math.max(Math.ulp(inFlow), Math.ulp(demand))))
//				throw new RuntimeException("Node flow imbalance - flow conservation violated");
//		});
//		//Confirm that the total demand from the origin is leaving through an outgoing link
//		double outFlow = Stream.of(origin.getNode().forwardStar()).mapToDouble(l -> flows.getOrDefault(l, 0.0)).sum();
//		if (outFlow - totalDemand() > 20*Math.max(Math.ulp(outFlow), Math.ulp(totalDemand()))) 
//			throw new RuntimeException("More flow leaving origin than total demand");
//		
//		return true;
//	}
//	
//	/**
//	 * @return true if no illegal centroid connectors are in the bush
//	 */
//	boolean checkCentroidConnectors() {
//		Map<Link,Double> flows = flows();
//		
//		//If there exists a Link in the bush
//		if (flows.entrySet().parallelStream()
//				//That is a centroid connector
//				.filter(e -> e.getKey() instanceof CentroidConnector)
//				//And which doesn't originate from this node
//				.filter(e -> !e.getKey().getTail().equals(origin.getNode()))
//				//And has more demand on it than the destination demand
//				.filter(e -> e.getValue() > demand.get(e.getKey().getHead().getZone()))
//				//Then something is wrong and this connector is allowing through too much demand
//				.findAny().isPresent()) throw new RuntimeException("Invalid Centroid Connector usage in Bush");
//		
////		for (Entry<Link, Double> e : flows.entrySet()) {
////			if (e.getKey() instanceof CentroidConnector) {
////				if (e.getKey().getTail().equals(origin.getNode())) continue;
////				else if (e.getValue() > demand.getOrDefault(e.getKey().getHead(), 0.0F)) 
////					throw new RuntimeException();
////			}
////		}
//		return true;
//	}

	public BushEvaluator getEvaluator(Class<? extends BushEvaluator> calcClass) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public double demand(Node n) {
		return n.getZone() == null? 0.0 : demand.get(n.getZone());
	}

	public Collection<Node> getNodes(){
		Collection<Node> nodes = getQ().filter(x -> x != null).map(BackVector::getHead).collect(Collectors.toSet());
		nodes.add(origin.node());
		return nodes;
	}
	
	private boolean contains(Link l) {
		BackVector bv =  q[l.getHead().getOrder()];
		
		if (bv == null || 
				((bv instanceof Link) && !((Link)bv).equals(l)) ||
				((bv instanceof BushMerge) && !((BushMerge) bv).contains(l))) return false;
		return true;
	}
	
	Collection<Node> outOnly(){
		return demand.getGraph().getNodes().parallelStream().filter(node -> q[node.getOrder()] == null).collect(Collectors.toSet());
	}
}
