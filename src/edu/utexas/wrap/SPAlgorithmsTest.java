package edu.utexas.wrap;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.jupiter.api.*;
import org.mapdb.DB;
import org.mapdb.DBMaker;

class SPAlgorithmsTest {
	static Graph graph;
	static Node A,B,C,D;
	static Link AB,AC,BC,CD,BD;
	
	@BeforeClass
	void setUpBraess(){
		DB db = null;
		graph = new Graph();
		
		A = new Node(1,false);
		B = new Node(2,false);
		C = new Node(3,false);
		D = new Node(4,false);
		
		AB = new TolledBPRLink(db,A, B, null, null, null, null, null,null) {
			@Override
			public BigDecimal getTravelTime() { return new BigDecimal("15");}
		};
		AC = new TolledBPRLink(db,A, C, null, null, null, null, null, null) {
			@Override
			public BigDecimal getTravelTime() { return new BigDecimal("22.0");}
		};
		BC = new TolledBPRLink(db,B, C, null, null, null, null, null, null) {
			@Override
			public BigDecimal getTravelTime() { return new BigDecimal("5.0");}
		};
		CD = new TolledBPRLink(db,C, D, null, null, null, null, null, null) {
			@Override
			public BigDecimal getTravelTime() { return new BigDecimal("6.0");}
		};
		BD = new TolledBPRLink(db,B, D, null, null, null, null, null, null) {
			@Override
			public BigDecimal getTravelTime() { return new BigDecimal("17.0");}
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
