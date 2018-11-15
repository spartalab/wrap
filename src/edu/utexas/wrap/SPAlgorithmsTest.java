package edu.utexas.wrap;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.jupiter.api.*;

class SPAlgorithmsTest {
	static Graph graph;
	static Node A,B,C,D;
	static Link AB,AC,BC,CD,BD;
	
	@BeforeClass
	void setUpBraess(){
		graph = new Graph();
		
		A = new Node(1,false);
		B = new Node(2,false);
		C = new Node(3,false);
		D = new Node(4,false);
		
		AB = new TolledBPRLink(A, B, null, null, null, null, null,null) {
			@Override
			public Double getTravelTime() { return new Double("15");}
		};
		AC = new TolledBPRLink(A, C, null, null, null, null, null, null) {
			@Override
			public Double getTravelTime() { return new Double("22.0");}
		};
		BC = new TolledBPRLink(B, C, null, null, null, null, null, null) {
			@Override
			public Double getTravelTime() { return new Double("5.0");}
		};
		CD = new TolledBPRLink(C, D, null, null, null, null, null, null) {
			@Override
			public Double getTravelTime() { return new Double("6.0");}
		};
		BD = new TolledBPRLink(B, D, null, null, null, null, null, null) {
			@Override
			public Double getTravelTime() { return new Double("17.0");}
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
