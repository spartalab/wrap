package edu.utexas.wrap.net;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.utexas.wrap.modechoice.Mode;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;

public class Graph {
	private Map<Node, Set<Link>> outLinks;
	private Map<Node, Set<Link>> inLinks;
	private Map<Integer, Node> nodeMap;
	private List<Node> order;
	private Set<Link> links;
	private Collection<TravelSurveyZone> zones;
	private int numZones;
	private int numNodes;
	private int numLinks;
	private byte[] md5;
	
	public Graph() {
		outLinks = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<Node, Set<Link>>());
		inLinks = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<Node, Set<Link>>());
		nodeMap = Int2ObjectMaps.synchronize(new Int2ObjectOpenHashMap<Node>());
		order = ObjectLists.synchronize(new ObjectArrayList<Node>());
		numZones = 0;
		numNodes = 0;
		numLinks = 0;
	}
	
	public Graph(Graph g) {
		outLinks = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<Node, Set<Link>>());
		for (Node n : g.outLinks.keySet()) {
			outLinks.put(n, ObjectSets.synchronize(new ObjectOpenHashSet<Link>(g.outLinks.get(n))));
		}
		inLinks = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<Node, Set<Link>>());
		for (Node n : g.inLinks.keySet()) {
			inLinks.put(n, ObjectSets.synchronize(new ObjectOpenHashSet<Link>(g.inLinks.get(n))));
		}
		nodeMap = g.nodeMap;
		order = ObjectLists.synchronize(new ObjectArrayList<Node>(g.order));
		numZones = g.numZones;
		numNodes = g.numNodes;
		numLinks = g.numLinks;
	}
	
	public Boolean add(Link link) {
		numLinks++;
		Node head = link.getHead();
		Node tail = link.getTail();

		Set<Link> headIns = inLinks.getOrDefault(head, ObjectSets.synchronize(new ObjectOpenHashSet<Link>()));
		Set<Link> tailOuts= outLinks.getOrDefault(tail, ObjectSets.synchronize(new ObjectOpenHashSet<Link>()));

		Boolean altered = headIns.add(link);
		altered |= tailOuts.add(link);
		if (altered) {
			inLinks.put(head, headIns);
			outLinks.put(tail, tailOuts);
			nodeMap.put(link.getHead().getID(), head);
			nodeMap.put(link.getTail().getID(), tail);
		}
		if (!order.contains(head)) {
			order.add(head);
			numNodes++;
		}
		if (!order.contains(tail)) {
			order.add(tail);
			numNodes++;
		}
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
		return ret;
	}
	
	public Node getNode(Integer id) {
		return nodeMap.get(id);
	}
	
	public Collection<Node> getNodes(){
		return order;
	}

	public int numZones() {
		return numZones;
	}

	public void setNumZones(int numZones) {
		this.numZones = numZones;
	}

	public Set<Link> inLinks(Node u){
		return inLinks.getOrDefault(u, Collections.emptySet());
	}

	public Integer numNodes() {
		return numNodes;
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
	
	public int getOrder(Node n) {
		return order.indexOf(n);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (byte b : getMD5()) {
			sb.append(String.format("%02X", b));
		}
		return sb.toString();
	}
	
	public int numLinks() {
		return numLinks;
	}

	public Graph getDerivativeGraph(Map<Link, Double> derivs) {
		// TODO Auto-generated method stub
		Graph ret = new Graph();
		ret.order = order;
		ret.outLinks = new HashMap<Node,Set<Link>>(outLinks.size(),1.0f);
		ret.inLinks = new HashMap<Node,Set<Link>>(inLinks.size(),1.0f);
		ret.nodeMap = nodeMap;
		ret.order = order;
		ret.numLinks = numLinks;
		ret.numNodes = numNodes;
		ret.numZones = numZones;
		ret.setMD5(getMD5());
		
		for (Node n : outLinks.keySet()) {
			Set<Link> ol = outLinks.get(n);
			Set<Link> nl = new HashSet<Link>(ol.size(),1.0f);
			for (Link l : ol) {
				Link ll = new Link(l) {
					Link parent = l;
					Double deriv = derivs.getOrDefault(l, 0.0);
					@Override
					public Boolean allowsClass(Mode c) {
						return parent.allowsClass(c);
					}

					@Override
					public double getPrice(Float vot, Mode c) {
						return getTravelTime();
					}

					@Override
					public double getTravelTime() {
						return deriv*flo;
					}

					@Override
					public double pricePrime(Float vot) {
						return tPrime();
					}

					@Override
					public double tIntegral() {
						// TODO Auto-generated method stub
						return 0;
					}

					@Override
					public double tPrime() {
						return deriv;
					}
					
				};
				nl.add(ll);
			}
			ret.outLinks.put(n, nl);
		}
		
		for (Node n : inLinks.keySet()) {
			Set<Link> ol = inLinks.get(n);
			Set<Link> nl = new HashSet<Link>(ol.size(),1.0f);
			for (Link l : ol) {
				Link ll = new Link(l) {
					Link parent = l;
					Double deriv = derivs.getOrDefault(l, 0.0);
					@Override
					public Boolean allowsClass(Mode c) {
						return parent.allowsClass(c);
					}

					@Override
					public double getPrice(Float vot, Mode c) {
						return getTravelTime();
					}

					@Override
					public double getTravelTime() {
						return deriv*flo;
					}

					@Override
					public double pricePrime(Float vot) {
						return tPrime();
					}

					@Override
					public double tIntegral() {
						// TODO Auto-generated method stub
						return 0;
					}

					@Override
					public double tPrime() {
						return deriv;
					}
				};
				nl.add(ll);
			}
			ret.inLinks.put(n, nl);
		}
		return ret;
	}

	public Collection<TravelSurveyZone> getTSZs() {
		// TODO Auto-generated method stub
		return zones;
	}
}
