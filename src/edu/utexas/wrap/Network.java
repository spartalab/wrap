package edu.utexas.wrap;

import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.HashMap;

import java.util.Map;
import java.util.Set;

public class Network {

//	private Map<Integer, Node> nodes;
//	protected Set<Link> links;
	protected Set<Origin> origins;
	protected Graph graph;
	
	private Double cachedRelGap;
	private Double cachedTSTT;
//	private Double cachedTSGC;
	private GapCalculator gc;
	private TSTTCalculator tc;
	private BeckmannCalculator bc;
	
	public Network(Set<Origin> origins, Graph g) {
		this.origins = origins;
		graph = g;

	}
	
	public Graph getGraph() {
		return new Graph(graph);
	}
	
	public Integer numNodes() {
		return graph.numNodes();
	}
	
	public Set<Link> getLinks() {
		return graph.getLinks();
	}

	public Set<Origin> getOrigins() {
		return origins;
	}
	
	public Double tstt() {
		if (cachedTSTT != null) return cachedTSTT;
		Double tstt = 0.0;
		
		for(Link l: getLinks()){
			tstt += l.getFlow().doubleValue() * l.getTravelTime().doubleValue();
		}
		cachedTSTT = tstt;
		return tstt;
	}
	
	public Double tsgc() {
		double tsgc = 0.0;
		
		for (Origin o : origins) {
			for (Bush b : o.getBushes()) {
				for (Link l : b.getLinks()) {
					tsgc += l.getBushFlow(b).doubleValue() * l.getPrice(b.getVOT(),b.getVehicleClass()).doubleValue();
				}
			}
		}
		return tsgc;
	}
	
	public Double relativeGap() {
		if (cachedRelGap != null) return cachedRelGap;
		Double numerator = 0.0;
		Double denominator = 0.0;
		
		for (Link l : getLinks()) {
			for (Origin o : origins) {
				for (Bush b : o.getBushes()) {
					numerator += l.getBushFlow(b).doubleValue() * l.getPrice(b.getVOT(),b.getVehicleClass()).doubleValue();
				}
			}
		}
		
		for (Origin o : origins) {
			for (Bush b : o.getBushes()) {
				b.topoSearch(false);
				Map<Node, BigDecimal> cache = new HashMap<Node, BigDecimal>(graph.numNodes());
				for (Node d : b.getNodes()) {
					
					Double demand = b.getDemand(d.getID());
					try {
						denominator += b.getCachedL(d,cache).doubleValue() * demand;
					} catch (UnreachableException e) {
						if (e.demand > 0.0)	e.printStackTrace();
					}
				}
			}
		}
		
		cachedRelGap = (numerator/denominator) - 1.0;
		return cachedRelGap;
	}
	
	public Double AEC() throws Exception {
		//TODO: Modify for generalized cost
		throw new Exception();
//		Double numerator = tsgc();
//		Double denominator = 0.0;
//		
//		for (Origin o : origins) {
//			for (Bush b : o.getBushes()) {
//				for (Node d : b.getNodes()) {
//					Double demand = o.getDemand(d.getID());
//					Map<Node, BigDecimal> cache = new HashMap<Node, BigDecimal>();
//					if (demand > 0.0) numerator -= b.getCachedL(d, cache).doubleValue() * demand;
//					denominator += demand;
//				}
//			}
//		}
//		
//		return numerator/denominator;
	}
	
	public Double Beckmann() {
		Double b = 0.0;
		for (Link l : getLinks()) {
			b += l.tIntegral().doubleValue();
		}
		return b;
	}
	
	public String toString() {
		String out = "";
		try {
			out += String.format("%6.10E", AEC()) + "\t";
		} catch (Exception e) {
			out += "Error           \t";
		}
		
//		Double tstt = tstt();
//		Double beck = Beckmann();
//		Double relg = relativeGap();
	
		gc = new GapCalculator(this);
		tc = new TSTTCalculator(this);
		bc = new BeckmannCalculator(this);

		tc.start();gc.start();bc.start();
		try {
			tc.join();gc.join();bc.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		out += String.format("%6.10E",tc.tstt) + "\t";
		out += String.format("%6.10E",bc.beck) + "\t";
		out += String.format("%6.10E",gc.gap);
	
		return out;
	}

	public void printFlows(PrintStream out) {
		System.out.println("\r\n\r\nTail\tHead\tflow");
		for (Link l : getLinks()) {
			Double sum = 0.0;
			for (Origin o : origins) {
				for (Bush b : o.getBushes()) {
						sum += l.getBushFlow(b).doubleValue();	
				}
			}
			out.println(l+"\t"+sum);
		}
	}

	public void clearCache() {
		cachedRelGap = null;
		cachedTSTT = null;
//		cachedTSGC = null;
	}

}

class TSTTCalculator extends Thread {
	Double tstt;
	Network net;
	
	public TSTTCalculator(Network net) {
		this.net = net;
	}
	
	@Override
	public void run() {
		tstt = net.tstt();
	}
}

class TSGCCalculator extends Thread {
	Double tsgc;
	Network net;
	
	public TSGCCalculator(Network net) {
		this.net = net;
	}
	
	@Override
	public void run() {
		tsgc = net.tsgc();
	}
}

class BeckmannCalculator extends Thread {
	Double beck;
	Network net;
	
	public BeckmannCalculator(Network net) {
		this.net = net;
	}
	
	@Override
	public void run() {
		beck = net.Beckmann();
	}
}

class GapCalculator extends Thread {
	Double gap;
	Network net;
	
	public GapCalculator(Network net) {
		this.net = net;
	}
	
	@Override
	public void run() {
		gap = net.relativeGap();
	}
}