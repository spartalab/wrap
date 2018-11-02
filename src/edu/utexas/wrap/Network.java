package edu.utexas.wrap;

import java.io.PrintStream;
import java.util.HashMap;

import java.util.Map;
import java.util.Set;

class AECCalculator extends Thread {
	Double val;
	Network net;
	TSGCCalculator cc;
	
	public AECCalculator(Network net, TSGCCalculator tc) {
		this.net = net;
		this.cc = tc;
	}
	
	@Override
	public void run() {
		try {
			val = net.AEC(cc);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class BeckmannCalculator extends Thread {
	Double val;
	Network net;
	
	public BeckmannCalculator(Network net) {
		this.net = net;
	}
	
	@Override
	public void run() {
		val = net.Beckmann();
	}
}

class GapCalculator extends Thread {
	Double val;
	Network net;
	TSGCCalculator cc;
	
	public GapCalculator(Network net, TSGCCalculator tc) {
		this.net = net;
	}
	
	@Override
	public void run() {
		val = net.relativeGap(cc);
	}
}

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
	private TSGCCalculator cc;
//	private AECCalculator ac;
	
	public Network(Set<Origin> origins, Graph g) {
		this.origins = origins;
		graph = g;

	}
	
	public Double AEC(TSGCCalculator cc) throws Exception {
		//TODO: Modify for generalized cost
		if (cc == null) {
			cc = new TSGCCalculator(this);
			cc.start();
		}
		
		Double numerator = 0.0;
		Double denominator = 0.0;
		
		for (Origin o : origins) {
			for (Bush b : o.getBushes()) {
				for (Node d : b.getNodes()) {
					Double demand = o.getDemand(d.getID());
					if (demand > 0.0) {
						Map<Node, Double> cache = new HashMap<Node, Double>();
						numerator -= b.getCachedL(d, cache).doubleValue() * demand;
						denominator += demand;
					}
				}
			}
		}
		
		cc.join();
		numerator += cc.val;
		return numerator/denominator;
	}
	
	public Double Beckmann() {
		Double b = 0.0;
		for (Link l : getLinks()) {
			b += l.tIntegral().doubleValue();
		}
		return b;
	}
	
	public void clearCache() {
		cachedRelGap = null;
		cachedTSTT = null;
//		cachedTSGC = null;
	}

	public Graph getGraph() {
		return new Graph(graph);
	}
	
	public Set<Link> getLinks() {
		return graph.getLinks();
	}
	
	public Set<Origin> getOrigins() {
		return origins;
	}
	
	public Integer numNodes() {
		return graph.numNodes();
	}
	
	public void printFlows(PrintStream out) {
		out.println("\r\nTail\tHead\tflow");
		for (Link l : getLinks()) {
			Double sum = l.getFlow().doubleValue();
			out.println(l+"\t"+sum);
		}
	}
	
	public Double relativeGap(TSGCCalculator cc) {
		if (cachedRelGap != null) return cachedRelGap;

		Double denominator = 0.0;
		if (cc == null) {
			cc = new TSGCCalculator(this);
		cc.start();
		}

		
		for (Origin o : origins) {
			for (Bush b : o.getBushes()) {
				b.shortTopoSearch();
				Map<Node, Double> cache = new HashMap<Node, Double>(graph.numNodes());
				for (Node d : b.getNodes()) {
					
					Float demand = b.getDemand(d.getID());
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
			cachedRelGap = (cc.val/denominator) - 1.0;
			return cachedRelGap;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String toString() {
		String out = "";

		tc = new TSTTCalculator(this);
		bc = new BeckmannCalculator(this);
		cc = new TSGCCalculator(this);
//		ac = new AECCalculator(this,cc);
		gc = new GapCalculator(this,cc);

		cc.start();tc.start();gc.start();bc.start();//ac.start();
		try {
			tc.join();gc.join();bc.join();cc.join();//ac.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
//		out += String.format("%6.10E",ac.val) + "\t";
		out += "\t\t\t";
		
		out += String.format("%6.10E",tc.val) + "\t";
		out += String.format("%6.10E",bc.val) + "\t";
		out += String.format("%6.10E",gc.val) + "\t";
		out += String.format("%6.10E", cc.val);
	
		return out;
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

	public Double tstt() {
		if (cachedTSTT != null) return cachedTSTT;
		Double tstt = 0.0;
		
		for(Link l: getLinks()){
			tstt += l.getFlow().doubleValue() * l.getTravelTime().doubleValue();
		}
		cachedTSTT = tstt;
		return tstt;
	}

}

class TSGCCalculator extends Thread {
	Double val;
	Network net;
	
	public TSGCCalculator(Network net) {
		this.net = net;
	}
	
	@Override
	public void run() {
		val = net.tsgc();
	}
}

class TSTTCalculator extends Thread {
	Double val;
	Network net;
	
	public TSTTCalculator(Network net) {
		this.net = net;
	}
	
	@Override
	public void run() {
		val = net.tstt();
	}
}