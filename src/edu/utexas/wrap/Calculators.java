package edu.utexas.wrap;

import java.util.HashMap;

import java.util.Map;
import java.util.Set;

class AECCalculator extends Thread {
	Double val;
	Graph graph;
	Set<Origin> origins;
	TSGCCalculator cc;
	
	public AECCalculator(Graph g, Set<Origin> o, TSGCCalculator tc) {
		graph = g;
		origins = o;
		this.cc = tc;
	}
	
	@Override
	public void run() {
		//TODO: Modify for generalized cost
		if (cc == null) {
			cc = new TSGCCalculator(graph, origins);
			cc.start();
		}

		Double numerator = 0.0;
		Double denominator = 0.0;
		
		for (Origin o : origins) {
			for (Bush b : o.getBushes()) {
				for (Node d : b.getNodes()) {
					Double demand = o.getDemand(d);
					if (demand > 0.0) {
						Map<Node, Double> cache = new HashMap<Node, Double>();
						try {
							numerator -= b.getCachedL(d, cache).doubleValue() * demand;
						} catch (UnreachableException e) {
							if (demand > 0) e.printStackTrace();
						}
						denominator += demand;
					}
				}
			}
		}
		val = null;
		try {
			cc.join();
			numerator += cc.val;
			val = numerator/denominator;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
}

class BeckmannCalculator extends Thread {
	Double val;
	Graph graph;
	
	public BeckmannCalculator(Graph g) {
		graph = g;
	}
	
	@Override
	public void run() {
		val = null;
		Double b = 0.0;
		for (Link l : graph.getLinks()) {
			b += l.tIntegral().doubleValue();
		}
		val = b;
	}
}

class GapCalculator extends Thread {
	Double val;
	Graph graph; 
	Set<Origin> origins;
	TSGCCalculator cc;
	
	public GapCalculator(Graph g, Set<Origin> o, TSGCCalculator tc) {
		graph = g;
		origins = o;
	}
	
	@Override
	public void run() {
		val = null;

		Double denominator = 0.0;
		if (cc == null) {
			cc = new TSGCCalculator(graph, origins);
		cc.start();
		}

		
		for (Origin o : origins) {
			for (Bush b : o.getBushes()) {
				b.shortTopoSearch();
				Map<Node, Double> cache = new HashMap<Node, Double>(graph.numNodes());
				for (Node d : b.getNodes()) {
					
					Float demand = b.getDemand(d);
					if (demand > 0.0F) try {
						denominator += b.getCachedL(d,cache).doubleValue() * demand;
					} catch (UnreachableException e) {
							e.printStackTrace();
					}
				}
			}
		}
		try{
			cc.join();
			val = (cc.val/denominator) - 1.0;
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}

class TSGCCalculator extends Thread {
	Double val;
	Graph graph;
	Set<Origin> origins;
	
	public TSGCCalculator(Graph g, Set<Origin> o) {
		graph = g;
		origins = o;
	}
	
	@Override
	public void run() {
		double tsgc = 0.0;
		
		for (Origin o : origins) {
			for (Bush b : o.getBushes()) {
				for (Link l : b) {
					tsgc += l.getBushFlow(b).doubleValue() * l.getPrice(b.getVOT(),b.getVehicleClass()).doubleValue();
				}
			}
		}
		val =  tsgc;
	}
}

class TSTTCalculator extends Thread {
	Double val;
	Graph graph;
	
	public TSTTCalculator(Graph g) {
		graph = g;
	}
	
	@Override
	public void run() {
		Double tstt = 0.0;
		
		for(Link l: graph.getLinks()){
			tstt += l.getFlow().doubleValue() * l.getTravelTime().doubleValue();
		}
		val = tstt;
	}
}