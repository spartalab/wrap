package edu.utexas.wrap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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
	
	public static Network fromFiles(File linkFile, File odMatrix, File VOTfile) throws FileNotFoundException, IOException {

		//Read the underlying network structure
		Graph g = readGraph(linkFile);
		
		//Read the VOT distribution
		Map<Node, List<Double[]>> VOTs = readUniformVOTDistrib(VOTfile,g);
		
		//Read the OD matrices for each VOT class
		Set<Origin> origins = readODMatrices(odMatrix, g, VOTs);
		
		return new Network(origins, g);
	}

	/**
	 * @param odMatrix
	 * @param g
	 * @param VOTs
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static Set<Origin> readODMatrices(File odMatrix, Graph g, Map<Node, List<Double[]>> VOTs)
			throws FileNotFoundException, IOException {
		/////////////////////////////////////
		// Read OD Matrix and assign flows
		/////////////////////////////////////
		BufferedReader of = new BufferedReader(new FileReader(odMatrix));
		String line;
		Integer origID;
		Node old;
		HashMap<Integer, Double> dests;
		String[] entries;
		Origin o;
		String[] cols;
		Integer destID;
		Double demand;
		HashMap<Integer, Double> bushDests;
		Set<Origin> origins = new HashSet<Origin>();
		
		do { // Move past headers in the file
			line = of.readLine();
		} while (!line.startsWith("Origin"));
		
		while (true) { //While more Origins to read
			origID = Integer.parseInt(line.trim().split("\\s+")[1]);
			old = g.getNode(origID);	// Retrieve the existing node with that ID
			dests = new HashMap<Integer, Double>();
			
			
			while (true) {
				line = of.readLine();
				if (line == null || line.trim().startsWith("O") || line.trim().equals("")) break; // If we've reached the gap, move to the next origin
				entries = line.trim().split(";");
				
				for (String entry : entries) {	// For each entry on this line
					cols = entry.split(":");	// Get its values
					destID = Integer.parseInt(cols[0].trim());
					demand = Double.parseDouble(cols[1].trim());
					if (demand > 0.0) dests.put(destID, demand);
				}
			}
			o = new Origin(old); 	// Construct an origin to replace it
			
			for (Double[] entry : VOTs.get(old)) {
				bushDests = new HashMap<Integer, Double>();
				for (Integer temp : dests.keySet()) {
					bushDests.put(temp, entry[1] * dests.get(temp));
				}
				o.buildBush(g, entry[0], bushDests);
			}
			origins.add(o);
			
			while (line != null && !line.startsWith("O")) line = of.readLine(); // Read in the origin header
			if (line == null || line.trim().equals("")) break; // If the origin header is empty, we've reached the end of the list
		}
		of.close();
		return origins;
	}

	/**
	 * @param linkFile
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static Graph readGraph(File linkFile) throws FileNotFoundException, IOException {
		String line;
		Graph g = new Graph();
		BufferedReader lf = new BufferedReader(new FileReader(linkFile));
		Map<Integer,Node> nodes = new HashMap<Integer, Node>();
		Set<Link> links = new HashSet<Link>();
		do { //Move past headers in the file
			line = lf.readLine();
		} while (!line.startsWith("~"));
		
		while (true) { //Iterate through each link (row)
			line = lf.readLine();
			if (line == null) break;	// End of link list reached
			if (line.startsWith("~") || line.trim().equals("")) continue;
			line = line.trim();
			String[] cols 	= line.split("\\s+");
			Integer tail 	= Integer.parseInt(cols[0]);
			Integer head 	= Integer.parseInt(cols[1]);
			Double capacity = Double.parseDouble(cols[2]);
			Double length 	= Double.parseDouble(cols[3]);
			Double fftime 	= Double.parseDouble(cols[4]);
			Double B 		= Double.parseDouble(cols[5]);
			Double power 	= Double.parseDouble(cols[6]);
			Double toll		= Double.parseDouble(cols[8]);
			
			//Create new node(s) if new, then add to map
			if (!nodes.containsKey(tail)) {
				nodes.put(tail,new Node(tail));
			}
			if (!nodes.containsKey(head)) {
				nodes.put(head,new Node(head));
			}
			
			//Construct new link and add to the list
			Link link = new Link(nodes.get(tail), nodes.get(head), capacity, length, fftime, B, power, toll);
			g.addLink(link);
			
			nodes.get(tail).addOutgoing(link);
			nodes.get(head).addIncoming(link);
			links.add(link);
		}
		lf.close();
		return g;
	}

	/**
	 * @param VOTfile
	 * @param g 
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static Map<Node, List<Double[]>> readUniformVOTDistrib(File VOTfile, Graph g) throws FileNotFoundException, IOException {
		// TODO handle this better so that breakdowns can be different per origin
		BufferedReader vf = new BufferedReader(new FileReader(VOTfile));
		LinkedList<Double[]> VOTs = new LinkedList<Double[]>();


		String line;
		vf.readLine(); //Ignore header line
		do {
			line = vf.readLine();
			if (line == null) break;
			String[] args = line.split("\t");
			Double vot = Double.parseDouble(args[0]);
			Double vProp = Double.parseDouble(args[1]);
			Double[] entry = {vot, vProp};
			VOTs.add(entry);
		} while (!line.equals(""));
		vf.close();
		
		Map<Node, List<Double[]>> votMap = new HashMap<Node, List<Double[]>>();
		for (Node n : g.getNodes()) {
			votMap.put(n, VOTs);
		}
		return votMap;
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
					tsgc += l.getBushFlow(b).doubleValue() * l.getPrice(b.getVOT()).doubleValue();
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
					numerator += l.getBushFlow(b).doubleValue() * l.getPrice(b.getVOT()).doubleValue();
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