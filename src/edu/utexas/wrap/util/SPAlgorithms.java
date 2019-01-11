package edu.utexas.wrap.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import edu.utexas.wrap.assignment.Path;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;


public class SPAlgorithms {
	public static Path dijkstra(Graph g, Node origin, Node destination) {
		
		Map<Node, Link> back = new HashMap<Node, Link>();
		FibonacciHeap<Node> Q = new FibonacciHeap<Node>();
		
		for (Node n : g.getNodes()) {
			if (!n.equals(origin)) {
				Q.add(n, Double.MAX_VALUE);
			}
		}
		Q.add(origin, 0.0);
		
		while (!Q.isEmpty()) {
			FibonacciLeaf<Node> u = Q.poll();
			for (Link uv : g.outLinks(u.n)) {
				FibonacciLeaf<Node> v = Q.getLeaf(uv.getHead());
				Double alt = uv.getTravelTime().doubleValue() + u.key;
				if (alt < v.key) {
					Q.decreaseKey(v, alt);
					back.put(v.n, uv);
				}
			}
		}
		
		Path path = new Path();
		Node i = destination;
		while (i != origin) {
			Link backLink = back.get(i);
			if (backLink==null) return null;
			path.addFirst(backLink);
			i = backLink.getTail();
		}
		return path;
	}
	
	public static List<Path> kShortestPaths(Graph g, Node origin, Node destination, Integer K) throws Exception{
		List<Path> A = new LinkedList<Path>();
		PriorityQueue<Path> B = new PriorityQueue<Path>();
		A.add(SPAlgorithms.dijkstra(g, origin,destination));
		if (A.get(0) == null) throw new Exception();
		
		for (Integer k = 1; k < K; k++) {
		
			for (Integer i = 0; i < A.get(k-1).size(); i++) {
				Graph gprime = new Graph(g);
				Node spurNode = A.get(k-1).node(i);
				Path rootPath = A.get(k-1).subPath(0,i);
				
				for (Path p : A) {
					if (rootPath.equals(p.subPath(0,i))){
						gprime.remove(p.get(i));
					}
				}
				
				for (Node rootPathNode : rootPath.nodes()) {
					if (!rootPathNode.equals(spurNode)){
						gprime.remove(rootPathNode);
					}
				}
				
				Path spurPath = SPAlgorithms.dijkstra(gprime, spurNode, destination);
				if (spurPath != null) {
					rootPath.append(spurPath);
					B.add(rootPath);	
				}
			}
			
			if (B.isEmpty()) break;
			
			A.add(B.poll());
			
		}
		
		return A;
	}
}

