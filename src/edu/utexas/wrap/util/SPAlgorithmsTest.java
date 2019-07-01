package edu.utexas.wrap.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.LinkedList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.jupiter.api.*;

import edu.utexas.wrap.assignment.Path;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Link;
import edu.utexas.wrap.net.Node;
import edu.utexas.wrap.net.TolledBPRLink;

class SPAlgorithmsTest {
	static Graph graph;
	static Node A,B,C,D;
	static Link AB,AC,BC,CD,BD;
	
	@BeforeClass
	void setUpBraess(){
		graph = new Graph();
		
		A = new Node(1,false,0);
		B = new Node(2,false,1);
		C = new Node(3,false,2);
		D = new Node(4,false,3);
		
		AB = new TolledBPRLink(A, B, null, null, null, null, null,null) {
			@Override
			public double getTravelTime() { return 15.0;}
		};
		AC = new TolledBPRLink(A, C, null, null, null, null, null, null) {
			@Override
			public double getTravelTime() { return 22.0;}
		};
		BC = new TolledBPRLink(B, C, null, null, null, null, null, null) {
			@Override
			public double getTravelTime() { return 5.0;}
		};
		CD = new TolledBPRLink(C, D, null, null, null, null, null, null) {
			@Override
			public double getTravelTime() { return 6.0;}
		};
		BD = new TolledBPRLink(B, D, null, null, null, null, null, null) {
			@Override
			public double getTravelTime() { return 17.0;}
		};
		graph.add(AB);
		graph.add(AC);
		graph.add(BC);
		graph.add(BD);
		graph.add(CD);
		
	}

	@Test
	void braessTestFixed() {
		
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
		assertTrue(shortPath != null && shortPath.equals(p));
		assertEquals(shortPath,p);
		
	}

}
