package edu.utexas.wrap.net;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class Graph {
	private Map<Node, Set<Link>> outLinks;
	private Map<Node, Set<Link>> inLinks;
	private Map<Integer, Node> nodeMap;
	private List<Node> order;
	private Set<Link> links;
	private int numZones;
	private byte[] md5;
	
	public Graph() {
		outLinks = new HashMap<Node, Set<Link>>();
		inLinks = new Object2ObjectOpenHashMap<Node, Set<Link>>();
		nodeMap = new Int2ObjectOpenHashMap<Node>();
		order = new ObjectArrayList<Node>();
	}
	
	public Graph(Graph g) {
		outLinks = new HashMap<Node, Set<Link>>();
		for (Node n : g.outLinks.keySet()) {
			outLinks.put(n, new ObjectOpenHashSet<Link>(g.outLinks.get(n)));
		}
		inLinks = new Object2ObjectOpenHashMap<Node, Set<Link>>();
		for (Node n : g.inLinks.keySet()) {
			inLinks.put(n, new ObjectOpenHashSet<Link>(g.inLinks.get(n)));
		}
		nodeMap = g.nodeMap;
		order = new ObjectArrayList<Node>(g.order);
	}
	
	public Boolean add(Link link) {
		Node head = link.getHead();
		Node tail = link.getTail();
		Set<Link> headIns = inLinks.getOrDefault(head, new ObjectOpenHashSet<Link>());
		Set<Link> tailOuts= outLinks.getOrDefault(tail, new ObjectOpenHashSet<Link>());

		Boolean altered = headIns.add(link);
		altered |= tailOuts.add(link);
		if (altered) {
			inLinks.put(head, headIns);
			outLinks.put(tail, tailOuts);
			nodeMap.put(link.getHead().getID(), head);
			nodeMap.put(link.getTail().getID(), tail);
		}
		if (!order.contains(head)) order.add(head);
		if (!order.contains(tail)) order.add(tail);
		return altered;
	}
	
	public void addAll(Collection<Link> links) {
		for (Link l : links) add(l);
	}
	
	public Boolean contains(Link l) {
		for (Link m : inLinks(l.getHead())) {
			if (m.equals(l)) return true;
		}
		for (Link m : outLinks(l.getHead())) {
			if (m.equals(l)) return true;
		}
		return false;
	}
	
	public Set<Link> getLinks(){
		if (links != null) return links;
			HashSet<Link> ret = new HashSet<Link>();
		outLinks.values().stream().reduce(ret, (a,b)->{
			a.addAll(b);
			return a;
		});
		links = ret;
//		for (Node n : outLinks.keySet()) ret.addAll(outLinks.get(n));
		return ret;
	}
	
	public Node getNode(Integer id) {
		return nodeMap.get(id);
	}
	
	public Collection<Node> getNodes(){
		return order;
	}

	public int getNumZones() {
		return numZones;
	}

	public void setNumZones(int numZones) {
		this.numZones = numZones;
	}

	public Link[] inLinks(Node u){
		return inLinks.getOrDefault(u, Collections.emptySet()).stream().toArray(n->new Link[n]);
	}

	public Integer numNodes() {
		return nodeMap.size();
	}
	
	public Link[] outLinks(Node u) {
		return outLinks.getOrDefault(u, Collections.emptySet()).stream().toArray(n->new Link[n]);
	}

	public Boolean remove(Link link) {
		Boolean altered = outLinks.get(link.getTail()).remove(link);
		altered |= inLinks.get(link.getHead()).remove(link);
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
	
	public int getOrder(Node n) {
		return order.indexOf(n);
	}
}
