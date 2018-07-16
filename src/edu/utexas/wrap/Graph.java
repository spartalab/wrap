package edu.utexas.wrap;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Graph {
	private Map<Node, Set<Link>> outLinks;
	private Map<Node, Set<Link>> inLinks;
	private Map<Integer, Node> nodeMap;
	
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
	
	public void addLink(Link link) {
		Node head = link.getHead();
		Node tail = link.getTail();
		Set<Link> headIns = inLinks.getOrDefault(head, new HashSet<Link>());
		Set<Link> tailOuts= outLinks.getOrDefault(tail, new HashSet<Link>());
		
		headIns.add(link);
		tailOuts.add(link);
		inLinks.put(head, headIns);
		outLinks.put(tail, tailOuts);
		nodeMap.put(link.getHead().getID(), link.getHead());
		nodeMap.put(link.getTail().getID(), link.getTail());
	}
	
	public Collection<Node> getNodes(){
		return nodeMap.values();
	}
	
	public Node getNode(Integer id) {
		return nodeMap.get(id);
	}
	
	public Map<Integer, Node> getNodeMap(){
		return nodeMap;
	}
	
	public Set<Link> getLinks(){
		HashSet<Link> ret = new HashSet<Link>();
		for (Node n : outLinks.keySet()) ret.addAll(outLinks.get(n));
		return ret;
	}

	public Set<Link> outLinks(Node u) {
		return outLinks.getOrDefault(u, new HashSet<Link>());
	}

	public void remove(Link link) {
		outLinks.get(link.getTail()).remove(link);
		inLinks.get(link.getHead()).remove(link);
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

}
