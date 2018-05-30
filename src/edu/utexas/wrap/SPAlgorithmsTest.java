package edu.utexas.wrap;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;

class SPAlgorithmsTest {

	@Test
	void braessTestFixed() {
		
		Graph graph = new Graph();
		
		Node A = new Node(1);
		Node B = new Node(2);
		Node C = new Node(3);
		Node D = new Node(4);
		
		Link AB = new Link(A, B, null, null, null, null, null) {
			@Override
			public Double getTravelTime() { return 15.0;}
		};
		Link AC = new Link(A, C, null, null, null, null, null) {
			@Override
			public Double getTravelTime() { return 22.0;}
		};
		Link BC = new Link(B, C, null, null, null, null, null) {
			@Override
			public Double getTravelTime() { return 5.0;}
		};
		Link CD = new Link(C, D, null, null, null, null, null) {
			@Override
			public Double getTravelTime() { return 6.0;}
		};
		Link BD = new Link(B, D, null, null, null, null, null) {
			@Override
			public Double getTravelTime() { return 17.0;}
		};

		A.addOutgoing(AB);
		A.addOutgoing(AC);
		B.addIncoming(AB);
		B.addOutgoing(BC);
		B.addOutgoing(BD);
		C.addIncoming(AC);
		C.addIncoming(BC);
		C.addOutgoing(CD);
		D.addIncoming(BD);
		D.addIncoming(CD);
		
		graph.addLink(AB);
		graph.addLink(AC);
		graph.addLink(BC);
		graph.addLink(BD);
		graph.addLink(CD);
		
		List<Path> shortPath = null;
		try {
			shortPath = SPAlgorithms.kShortestPaths(graph, A, D,3);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			fail(e);
		}
		Path abd = new Path(); abd.add(AB); abd.add(BD);
		Path acd = new Path(); acd.add(AC); acd.add(CD);
		Path abcd = new Path(); abcd.add(AB); abcd.add(BC); abcd.add(CD);
		
		LinkedList<Path> p = new LinkedList<Path>();
		p.add(abcd); p.add(acd); p.add(abd);
		if (shortPath == null || !(shortPath.equals(p))) fail("K Short Path returned "+shortPath.toString());
	}

}
