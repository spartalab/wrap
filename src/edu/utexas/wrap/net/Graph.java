package edu.utexas.wrap.net;

import java.io.PrintStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Graph {
	private Map<Node, Set<Link>> outLinks;
	private Map<Node, Set<Link>> inLinks;
	private Map<Integer, Node> nodeMap;
	private int numZones;
	private byte[] md5;
	
	public Graph() {
		outLinks = new HashMap<Node, Set<Link>>();
		inLinks = new HashMap<Node, Set<Link>>();
		nodeMap = new HashMap<Integer, Node>();
	}
	
	public Graph(Graph g) {
		outLinks = new HashMap<Node, Set<Link>>();
		for (Node n : g.outLinks.keySet()) {
			outLinks.put(n, new HashSet<Link>(g.outLinks.get(n)));
		}
		inLinks = new HashMap<Node, Set<Link>>();
		for (Node n : g.inLinks.keySet()) {
			inLinks.put(n, new HashSet<Link>(g.inLinks.get(n)));
		}
		nodeMap = g.nodeMap;
	}
	
	public Boolean add(Link link) {
		Node head = link.getHead();
		Node tail = link.getTail();
		Set<Link> headIns = inLinks.getOrDefault(head, new HashSet<Link>());
		Set<Link> tailOuts= outLinks.getOrDefault(tail, new HashSet<Link>());

		Boolean altered = headIns.add(link);
		altered |= tailOuts.add(link);
		if (altered) {
			inLinks.put(head, headIns);
			outLinks.put(tail, tailOuts);
			nodeMap.put(link.getHead().getID(), head);
			nodeMap.put(link.getTail().getID(), tail);
		}
		return altered;
	}
	
	public void addAll(Collection<Link> links) {
		for (Link l : links) add(l);
	}
	
	public Boolean contains(Link l) {
		return outLinks(l.getTail()).contains(l) || inLinks(l.getHead()).contains(l);
	}
	
	public Set<Link> getLinks(){
		HashSet<Link> ret = new HashSet<Link>();
		outLinks.values().stream().reduce(ret, (a,b)->{
			a.addAll(b);
			return a;
		});
//		for (Node n : outLinks.keySet()) ret.addAll(outLinks.get(n));
		return ret;
	}
	
	public Node getNode(Integer id) {
		return nodeMap.get(id);
	}
	
	public Collection<Node> getNodes(){
		return nodeMap.values();
	}

	public int getNumZones() {
		return numZones;
	}

	public void setNumZones(int numZones) {
		this.numZones = numZones;
	}

	public Set<Link> inLinks(Node u){
		return inLinks.getOrDefault(u, new HashSet<Link>(0));
	}

	public Integer numNodes() {
		return nodeMap.size();
	}
	
	public Set<Link> outLinks(Node u) {
		return outLinks.getOrDefault(u, new HashSet<Link>(0));
	}

	public Boolean remove(Link link) {
		Boolean altered = outLinks.get(link.getTail()).remove(link);
		altered |= inLinks.get(link.getHead()).remove(link);
		return altered;
	}

	public void remove(Node node) {
		for (Link link : inLinks.getOrDefault(node, new HashSet<Link>())) {
			outLinks.get(link.getTail()).remove(link);
		}
		for (Link link : outLinks.getOrDefault(node, new HashSet<Link>())) {
			inLinks.get(link.getHead()).remove(link);
		}
		inLinks.remove(node);
		outLinks.remove(node);
	}

	public void printFlows(PrintStream out) {
		out.println("\r\nTail\tHead\tflow");
		for (Link l : getLinks()) {
			Double sum = l.getFlow().doubleValue();
			out.println(l+"\t"+sum);
		}
	}

	public void setMD5(byte[] md5) {
		// TODO Auto-generated method stub
		this.md5 = md5;
	}
	
	public byte[] getMD5() {
		return md5;
	}
	
}
