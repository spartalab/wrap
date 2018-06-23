package edu.utexas.wrap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class Network {

	//private Map<Integer, Node> nodes;
	protected Set<Link> links;
	protected Set<Origin> origins;
	protected Map<Integer, Node> nodes;
	protected Graph graph;
	
	public Network(Set<Link> links, Set<Origin> origins, Map<Integer, Node> nodes, Graph g) {
		setLinks(links);
		setOrigins(origins);
		this.nodes = nodes;
		graph = g;
	}
	
	public Graph getGraph() {
		return new Graph(graph);
	}
	
	public static Network fromFiles(File linkFile, File odMatrix, File VOTfile) throws Exception {
		// Initialization
		Map<Integer,Node> nodes = new HashMap<Integer, Node>();
		Set<Link> links = new HashSet<Link>();
		LinkedList<Double[]> VOTs = new LinkedList<Double[]>();
		// Open the files for reading
		BufferedReader lf = new BufferedReader(new FileReader(linkFile));
		BufferedReader of = new BufferedReader(new FileReader(odMatrix));
		BufferedReader vf = new BufferedReader(new FileReader(VOTfile));
		HashMap<Integer, Double> dest = new HashMap<Integer, Double>();
		
		Graph g = new Graph();
		
		//////////////////////////////////////////////
		// Read links and build corresponding nodes
		//////////////////////////////////////////////
		String line;
		vf.readLine();
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
				dest.put(tail, new Double(0));
			}
			if (!nodes.containsKey(head)) {
				nodes.put(head,new Node(head));
				dest.put(head,new Double(0));
			}
			
			//Construct new link and add to the list
			Link link = new Link(nodes.get(tail), nodes.get(head), capacity, length, fftime, B, power, toll);
			g.addLink(link);
			
			nodes.get(tail).addOutgoing(link);
			nodes.get(head).addIncoming(link);
			links.add(link);
		}
		lf.close();
		
		/////////////////////////////////////
		// Read OD Matrix and assign flows
		/////////////////////////////////////
		
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
			old = nodes.get(origID);	// Retrieve the existing node with that ID
			dests = new HashMap<Integer, Double>(dest);
			
			
			while (true) {
				line = of.readLine();
				if (line.trim().startsWith("O") || line.trim().equals("")) break; // If we've reached the gap, move to the next origin
				entries = line.trim().split(";");
				
				for (String entry : entries) {	// For each entry on this line
					cols = entry.split(":");	// Get its values
					destID = Integer.parseInt(cols[0].trim());
					demand = Double.parseDouble(cols[1].trim());
					if (demand > 0.0) dests.put(destID, demand);
				}
			}
			o = new Origin(old, dests.keySet()); 	// Construct an origin to replace it
			
			for (Double[] entry : VOTs) {
				bushDests = new HashMap<Integer, Double>();
				for (Integer temp : dests.keySet()) {
					bushDests.put(temp, entry[1] * dests.get(temp));
				}
				o.buildBush(links, nodes, entry[0], bushDests);
			}
			//TODO: Handle multiple VOT classes and split destination demand accordingly
			//nodes.put(origID, o); // Replace the node with its origin equivalent
			origins.add(o);
			
			while (line != null && !line.startsWith("O")) line = of.readLine(); // Read in the origin header
			if (line == null || line.trim().equals("")) break; // If the origin header is empty, we've reached the end of the list
		}
		of.close();
		return new Network(links, origins, nodes, g);
		
	}
	
	public Set<Link> getLinks() {
		return links;
	}
	private void setLinks(Set<Link> links) {
		this.links = links;
	}


	public Set<Origin> getOrigins() {
		return origins;
	}
	private void setOrigins(Set<Origin> origins) {
		this.origins = origins;
	}
	
	
	public Double tstt() throws Exception{
		Double tstt = 0.0;
		
		for(Link l:links){
			tstt += l.getFlow() * l.getTravelTime();
		}
		return tstt;
	}
	
	public Double relativeGap() throws Exception {
		Double numerator = new Double(0);
		Double denominator = new Double(0);
		
		for (Link l : links) {
			for (Origin o : origins) {
				for (Bush b : o.getBushes()) {
					numerator += b.getBushFlow(l) * l.getPrice(b.getVOT());
				}
			}
		}
		
		for (Origin o : origins) {
			for (Bush b : o.getBushes()) {
				for (Node d : b.getNodes()) {
					
					Double demand = b.getDemand(d.getID());
					try {
						denominator += b.getL(d) * demand;
					} catch (UnreachableException e) {
						if (e.demand > 0.0)	e.printStackTrace();
					}
				}
			}
		}
		
		return (numerator/denominator) - 1.0;
	}
	
	public Double AEC() throws Exception {
		//TODO: Modify for generalized cost
		Double numerator = tstt();
		Double denominator = new Double(0);
		
		for (Origin o : origins) {
			for (Bush b : o.getBushes()) {
				for (Node d : b.getNodes()) {
					Double demand = o.getDemand(d.getID());
					if (demand > 0.0) numerator -= b.getL(d) * demand;
					denominator += demand;
				}
			}
		}
		
		return numerator/denominator;
	}
	
	public Double Beckmann() throws Exception {
		Double b = new Double(0);
		for (Link l : links) {
			b += l.tIntegral();
		}
		return b;
	}

}
