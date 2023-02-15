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
package edu.utexas.wrap.net;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import edu.utexas.wrap.assignment.AssignmentContainer;
import edu.utexas.wrap.assignment.bush.BackVector;
import edu.utexas.wrap.assignment.bush.Bush;
//import edu.utexas.wrap.assignment.sensitivity.DerivativeLink;
import edu.utexas.wrap.util.FibonacciHeap;
import edu.utexas.wrap.util.FibonacciLeaf;

/**A representation of links and nodes in an interconnected directed graph
 * 
 * @author William
 *
 */
public class Graph {
	
	private Map<Node, Set<Link>> outLinks;
	private Map<Node, Set<Link>> inLinks;
	private Map<Integer, Node> nodeMap;
	private List<Node> order;
//	private Map<Node,Integer> nodeOrder;
	private Set<Link> links;
	private final Map<Integer,TravelSurveyZone> zones;
	private int numNodes;
	private int numLinks;
	private byte[] md5;
	
	private Link[][] forwardStar;
	private Link[][] reverseStar;
	
	private Map<Integer,Link> hashes = new HashMap<Integer,Link>();
	
	
	public Graph(Map<Integer,TravelSurveyZone> zones) {
		outLinks = (new HashMap<Node, Set<Link>>());
		inLinks = (new HashMap<Node, Set<Link>>());
		nodeMap = (new HashMap<Integer,Node>());
		order = (new ArrayList<Node>());
		links = new HashSet<Link>();
		numNodes = 0;
		numLinks = 0;
		this.zones = zones;
	}
	
	public Graph(Graph g) {
		nodeMap = g.nodeMap;
		order = Collections.unmodifiableList(g.order);
		links = g.links;
		numNodes = g.numNodes;
		numLinks = g.numLinks;
		forwardStar = g.forwardStar;
		reverseStar = g.reverseStar;
		throw new RuntimeException("Incomplete");
		//TODO: duplicate zones, nodes, and links
	}
	
	//TODO improve concurrency availability here
	public synchronized Boolean add(Link link) {
		
		if (hashes.containsKey(link.hashCode())) 
			throw new RuntimeException("Hash collision");
		else hashes.put(link.hashCode(),link);
		
		numLinks++;
		Boolean altered = links.add(link);
		Node head = link.getHead();
		Node tail = link.getTail();

		Set<Link> headIns = inLinks.getOrDefault(head, (new HashSet<Link>()));
		Set<Link> tailOuts= outLinks.getOrDefault(tail,  (new HashSet<Link>()));

		altered |= headIns.add(link);
		altered |= tailOuts.add(link);
		if (altered) {
			inLinks.put(head, headIns);
			outLinks.put(tail, tailOuts);
			nodeMap.put(link.getHead().getID(), head);
			nodeMap.put(link.getTail().getID(), tail);
		}

		if (!order.contains(tail)) {
			order.add(tail);
			numNodes++;
		}
		if (!order.contains(head)) {
			order.add(head);
			numNodes++;
		}
		return altered;
	}
	
	public void addAll(Collection<Link> links) {
		for (Link l : links) add(l);
	}
	
//	public Boolean contains(Link l) {
//		for (Link m : inLinks(l.getHead())) {
//			if (m.equals(l)) return true;
//		}
//		for (Link m : outLinks(l.getHead())) {
//			if (m.equals(l)) return true;
//		}
//		return false;
//	}

	public Set<Link> getLinks(){
		return links;
	}
	
	public Node getNode(Integer id) {
		return nodeMap.get(id);
	}
	
	public TravelSurveyZone getZone(Integer id) {
		return zones.get(id);
	}
	
	public Collection<Node> getNodes(){
		return order;
//		Set<Node> ret = new HashSet<Node>(inLinks.keySet());
//		ret.addAll(outLinks.keySet());
//		return ret;
//		return nodeOrder.keySet();
	}

	public int numZones() {
		return zones.size();
	}


//	public Link[] inLinks(Node u){
//		return reverseStar[u.getOrder()];
////		return inLinks.getOrDefault(u, Collections.emptySet());
//	}

	public Integer numNodes() {
		return numNodes;
	}


	public Boolean remove(Link link) {
		
		//FIXME doesn't remove from Node object
		Node tail = link.getTail();
		Node head = link.getHead();
		Boolean altered = false;
		for (int i = 0; i < forwardStar[tail.getOrder()].length;i++) {
			if (forwardStar[tail.getOrder()][i].equals(link)) {
				altered = true;
				forwardStar[tail.getOrder()][i] = null;
				break;
			}
		}
		for (int i = 0; i < reverseStar[head.getOrder()].length;i++) {
			if (reverseStar[tail.getOrder()][i].equals(link)) {
				altered = true;
				reverseStar[tail.getOrder()][i] = null;
				break;
			}
		}
		
//		Boolean altered = outLinks.get(link.getTail()).remove(link);
//		altered |= inLinks.get(link.getHead()).remove(link);
		return altered;
	}

	public void remove(Node node) {
		for (Link link : inLinks.getOrDefault(node, Collections.emptySet())) {
			outLinks.get(link.getTail()).remove(link);
		}
		for (Link link : outLinks.getOrDefault(node, Collections.emptySet())) {
			inLinks.get(link.getHead()).remove(link);
		}
		inLinks.remove(node);
		outLinks.remove(node);
	}

	public void printFlows(PrintStream out) {
		out.println("\r\nTail\tHead\tflow");
		for (Link l : getLinks()) {
			Double sum = l.getFlow();
			out.println(l+"\t"+sum);
		}
	}

	public void setMD5(byte[] md5) {
		this.md5 = md5;
	}
	
	public byte[] getMD5() {
		return md5;
	}
	
//	public int getOrder(Node n) {
//		return n.getOrder();
////		return nodeOrder.getOrDefault(n,-1);
//	}

	public String toString() {
		StringBuilder sb = new StringBuilder("struct/");
		for (byte b : getMD5()) {
			sb.append(String.format("%02X", b));
		}
		return sb.toString();
	}
	
	public int numLinks() {
		return numLinks;
	}

	public Map<Node,Node> duplicateNodes(){
		return order.stream().collect(
				Collectors.toMap(Function.identity(), x -> new Node(x)));
	}

	public Graph getDerivativeGraph(
			Map<Link, Link> linkMap, 
			Map<Node,Node> mapOfNodes) {
		// TODO Auto-generated method stub
		Graph ret = new Graph(zones);
		
		ret.nodeMap = nodeMap;
		ret.numLinks = numLinks;
		ret.numNodes = numNodes;
		ret.forwardStar = new Link[forwardStar.length][];
		ret.reverseStar = new Link[reverseStar.length][];
		ret.setMD5(getMD5());
		ret.order = order.stream().map(
				n -> mapOfNodes.get(n)
				).collect(Collectors.toList());
		
		IntStream.range(0,forwardStar.length).parallel().forEach(j ->{
			Link[] oldLinks = forwardStar[j];
			if (oldLinks==null) return;
			Link[] newLinks = new Link[oldLinks.length];
			for (int i = 0; i < oldLinks.length; i++) {
				Link oldLink = oldLinks[i];
				Link newLink = linkMap.get(oldLink);
				newLinks[i] = newLink;
			}
			ret.order.get(j).setForwardStar(newLinks);
			ret.forwardStar[j] = newLinks;
		});
		
		IntStream.range(0, reverseStar.length).parallel().forEach(i->{
//		for (int i = 0; i < reverseStar.length; i++) {
			Link[] oldLinks = reverseStar[i];
			if (oldLinks == null) return;
			Link[] newLinks = new Link[oldLinks.length];
			for (int j = 0; j < oldLinks.length; j++) {
				Link l = oldLinks[j];
				Link ll = linkMap.get(l);
				newLinks[j] = ll;
				
			}
			ret.reverseStar[i] = newLinks;
			ret.order.get(i).setReverseStar(newLinks);
		});
		return ret;
	}
	
	public void setSignalTimings(
			Map<Integer,Float> greenShares,
			Map<Integer,Float> cycleLengths
			) {
		if (greenShares == null || cycleLengths == null) return;
		
		Set<SignalizedNode> toCover = new HashSet<SignalizedNode>();
		
		for (Integer link_hash : greenShares.keySet()) {
			Link l = hashes.get(link_hash);
			Node n = l.getHead();
			if (n instanceof SignalizedNode) toCover.add((SignalizedNode) n);
		}
		
		for (SignalizedNode n : toCover) {
			
			//TODO handle case where some entries are missing in greenShares
			Float[] greenArray = new Float[reverseStar[n.getOrder()].length];
			float total = 0.f;
			for (int i = 0; i < reverseStar[n.getOrder()].length;i++) {
				Link l = reverseStar[n.getOrder()][i];
				Float greenShare = greenShares.get(l.hashCode());
				greenArray[i] = greenShare;
				if (
						!greenShare.isNaN()
					&&	!greenShare.isInfinite()
					) total += greenShare;
			}
			
			for (int i = 0; i < greenArray.length; i++) {
				greenArray[i] = greenArray[i]/total;
			}
			n.setGreenShares(greenArray);
			n.setCycleLength(cycleLengths.get(n.getID()));
		}
	}

	
	public void complete() {
		forwardStar = new Link[numNodes][];
		reverseStar = new Link[numNodes][];
		outLinks.keySet().parallelStream().forEach(x -> {
			Link[] fs = outLinks.get(x).stream().toArray(Link[]::new);
			forwardStar[x.getOrder()] = fs;
			x.setForwardStar(fs);
		});
		inLinks.keySet().parallelStream().forEach(x ->{
			Link[] rs = inLinks.get(x).stream().toArray(Link[]::new);
			for (int i = 0; i < rs.length; i++) {
				rs[i].setHeadIndex(i);
			}
			reverseStar[x.getOrder()] = rs;
			x.setReverseStar(rs);
			
		});
		outLinks = null;
		inLinks = null;
	}

	public Collection<TravelSurveyZone> getTSZs() {
		return zones.values();
	}

	public void loadDemand(AssignmentContainer container) {
		container.flows(false).entrySet().parallelStream()
			.forEach(pair -> pair.getKey().changeFlow(pair.getValue()));
	}

	public double cheapestCostPossible(AssignmentContainer container) {
		// TODO Auto-generated method stub
		Collection<Node> nodes = getNodes();
		BackVector[] initMap = new BackVector[nodes.size()];
		FibonacciHeap Q = new FibonacciHeap(nodes.size(),1.0f);
		
		Double cost = 0.0;
		
		for (Node n : nodes) {
			if (!n.getID().equals(container.root().getID())) {
				Q.add(n, Double.MAX_VALUE);
			}
			else Q.add(n,0.0);
		}
//		Q.add(container.root().node(), 0.0);

		while (!Q.isEmpty()) {
			FibonacciLeaf u = Q.poll();
			cost += container.demand(u.node) * u.key;
			
			for (Link uv : u.node.forwardStar()) {
				//TODO expand this admissibility check to other implementations 
				//of AssignmentContainer
				if (container instanceof Bush && 
						!((Bush) container).canUseLink(uv)) continue;
//				if (!uv.allowsClass(c) || isInvalidConnector(uv)) continue;
				
				
				//If this link doesn't allow this bush's class of driver on the 
				//link, don't consider it
				//This was removed to allow flow onto all links for the initial 
				//bush, and any illegal flow will be removed on the first flow 
				//shift due to high price
				
				FibonacciLeaf v = Q.getLeaf(uv.getHead());
				Double alt = uv.getPrice(container)+u.key;
				if (alt<v.key) {
					Q.decreaseKey(v, alt);
					initMap[v.node.getOrder()] = uv;
				}
			}
		}
		return cost;
	}
	

}
