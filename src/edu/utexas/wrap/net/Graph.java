package edu.utexas.wrap.net;

import java.io.PrintStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.utexas.wrap.modechoice.Mode;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class Graph {
	
	private Map<Node, Set<Link>> outLinks;
	private Map<Node, Set<Link>> inLinks;
	private Map<Integer, Node> nodeMap;
	private List<Node> order;
//	private Map<Node,Integer> nodeOrder;
	private Set<Link> links;
	private Collection<TravelSurveyZone> zones;
	private int numZones;
	private int numNodes;
	private int numLinks;
	private byte[] md5;
	
	private Link[][] forwardStar;
	private Link[][] reverseStar;
	
	
	public Graph() {
		outLinks = (new Object2ObjectOpenHashMap<Node, Set<Link>>());
		inLinks = (new Object2ObjectOpenHashMap<Node, Set<Link>>());
		nodeMap = (new Int2ObjectOpenHashMap<Node>());
		order = (new ObjectArrayList<Node>());
		links = new ObjectOpenHashSet<Link>();
//		nodeOrder = new Object2IntOpenHashMap<Node>();
		numZones = 0;
		numNodes = 0;
		numLinks = 0;
		
	}
	
	public Graph(Graph g) {
		outLinks = (new Object2ObjectOpenHashMap<Node, Set<Link>>());
		for (Node n : g.outLinks.keySet()) {
			outLinks.put(n, (new ObjectOpenHashSet<Link>(g.outLinks.get(n))));
		}
		outLinks = Collections.unmodifiableMap(outLinks);
		inLinks = (new Object2ObjectOpenHashMap<Node, Set<Link>>());
		for (Node n : g.inLinks.keySet()) {
			inLinks.put(n, (new ObjectOpenHashSet<Link>(g.inLinks.get(n))));
		}
		inLinks = Collections.unmodifiableMap(inLinks);
		nodeMap = g.nodeMap;
		order = Collections.unmodifiableList(g.order);
		links = g.links;
		numZones = g.numZones;
		numNodes = g.numNodes;
		numLinks = g.numLinks;
//		nodeOrder = g.nodeOrder;
	}
	
	public Boolean add(Link link) {
		numLinks++;
		Boolean altered = links.add(link);
		Node head = link.getHead();
		Node tail = link.getTail();

		Set<Link> headIns = inLinks.getOrDefault(head, (new ObjectOpenHashSet<Link>()));
		Set<Link> tailOuts= outLinks.getOrDefault(tail,  (new ObjectOpenHashSet<Link>()));

		altered |= headIns.add(link);
		altered |= tailOuts.add(link);
		if (altered) {
			inLinks.put(head, headIns);
			outLinks.put(tail, tailOuts);
			nodeMap.put(link.getHead().getID(), head);
			nodeMap.put(link.getTail().getID(), tail);
		}

		if (!order.contains(tail)) {
//			nodeOrder.put(tail, numNodes);
			order.add(tail);
			numNodes++;
		}
		if (!order.contains(head)) {
			order.add(head);
//			nodeOrder.put(head,numNodes);
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
	
	public Collection<Node> getNodes(){
		return order;
//		Set<Node> ret = new HashSet<Node>(inLinks.keySet());
//		ret.addAll(outLinks.keySet());
//		return ret;
//		return nodeOrder.keySet();
	}

	public int numZones() {
		return numZones;
	}

	public void setNumZones(int numZones) {
		this.numZones = numZones;
	}

//	public Link[] inLinks(Node u){
//		return reverseStar[u.getOrder()];
////		return inLinks.getOrDefault(u, Collections.emptySet());
//	}

	public Integer numNodes() {
		return numNodes;
	}
	
//	public Link[] outLinks(Node u) {
//		return forwardStar[u.getOrder()];
////		return outLinks.getOrDefault(u, Collections.emptySet()).stream().toArray(n->new Link[n]);
//	}

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
//		ret.outLinks = new HashMap<Node,Set<Link>>(outLinks.size(),1.0f);
//		ret.inLinks = new HashMap<Node,Set<Link>>(inLinks.size(),1.0f);
		ret.nodeMap = nodeMap;
		ret.numLinks = numLinks;
		ret.numNodes = numNodes;
		ret.numZones = numZones;
		ret.forwardStar = forwardStar;
		ret.reverseStar = reverseStar;
//		ret.nodeOrder = nodeOrder;
		ret.setMD5(getMD5());
		
		for (int j = 0; j<forwardStar.length;j++) {
			Link[] ol = forwardStar[j];
//			Set<Link> ol = outLinks.get(n);
//			Set<Link> nl = new HashSet<Link>(ol.size(),1.0f);
			Link[] nl = new Link[ol.length];
			for (int i = 0; i < ol.length; i++) {
				Link l = ol[i];
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
				nl[i] = ll;
			}
			ret.forwardStar[j] = nl;
		}
		
		for (int i = 0; i < reverseStar.length; i++) {
			Link[] ol = reverseStar[i];
			Link[] nl = new Link[ol.length];
//			Set<Link> ol = inLinks.get(n);
//			Set<Link> nl = new HashSet<Link>(ol.size(),1.0f);
			for (int j = 0; j < ol.length; j++) {
				Link l = ol[j];
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
						throw new RuntimeException("not yet implemented");
					}

					@Override
					public double tPrime() {
						return deriv;
					}
				};
				nl[j] = ll;
			}
			ret.reverseStar[i] = nl;
		}
		return ret;
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
			reverseStar[x.getOrder()] = rs;
			x.setReverseStar(rs);
			
		});
		outLinks = null;
		inLinks = null;
	}

	public Collection<TravelSurveyZone> getTSZs() {
		// TODO Auto-generated method stub
		return zones;
	}
}
