package edu.utexas.wrap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.HashSet;
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
	
	public static Network fromFiles(File linkFile, File odMatrix) throws Exception {
		// Initialization
		Map<Integer,Node> nodes = new HashMap<Integer, Node>();
		Set<Link> links = new HashSet<Link>();
		Set<Origin> origins = new HashSet<Origin>();
		// Open the files for reading
		BufferedReader lf = new BufferedReader(new FileReader(linkFile));
		BufferedReader of = new BufferedReader(new FileReader(odMatrix));
		HashMap<Integer, Double> dest = new HashMap<Integer, Double>();
		
		Graph g = new Graph();
		
		//////////////////////////////////////////////
		// Read links and build corresponding nodes
		//////////////////////////////////////////////
		String line;
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
			Double capacity 	= Double.parseDouble(cols[2]);
			Double length 	= Double.parseDouble(cols[3]);
			Double fftime 	= Double.parseDouble(cols[4]);
			Double B 		= Double.parseDouble(cols[5]);
			Double power 	= Double.parseDouble(cols[6]);
			
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
			Link link = new Link(nodes.get(tail), nodes.get(head), capacity, length, fftime, B, power);
			g.addLink(link);
			
			nodes.get(tail).addOutgoing(link);
			nodes.get(head).addIncoming(link);
			links.add(link);
		}
		lf.close();
		
		/////////////////////////////////////
		// Read OD Matrix and assign flows
		/////////////////////////////////////
		do { // Move past headers in the file
			line = of.readLine();
		} while (!line.startsWith("Origin"));
		
		while (true) { //While more Origins to read
			Integer origID = Integer.parseInt(line.trim().split("\\s+")[1]);
			Node old = nodes.get(origID);	// Retrieve the existing node with that ID
			HashMap<Integer, Double> dests = new HashMap<Integer, Double>(dest);
			String[] entries;
			
			while (true) {
				line = of.readLine();
				if (line.trim().startsWith("O") || line.trim().equals("")) break; // If we've reached the gap, move to the next origin
				entries = line.trim().split(";");
				
				for (String entry : entries) {	// For each entry on this line
					String[] cols = entry.split(":");	// Get its values
					Integer destID = Integer.parseInt(cols[0].trim());
					Double demand = Double.parseDouble(cols[1].trim());
					dests.put(destID, demand);
				}
			}
			Origin o = new Origin(old, dests); 	// Construct an origin to replace it
			o.buildBush(links, nodes, 0.0);
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
			numerator += l.getTravelTime() * l.getFlow();
		}
		
		for (Origin o : origins) {
			for (Bush b : o.getBushes()) {
				for (Node d : b.getNodes()) {
					denominator += b.getL(d) * o.getDemand(d.getID());
				}
			}
		}
		
		return (numerator/denominator) - 1.0;
	}
	
	public Double AEC() throws Exception {
		Double numerator = tstt();
		Double denominator = new Double(0);
		
		for (Origin o : origins) {
			for (Bush b : o.getBushes()) {
				for (Node d : b.getNodes()) {
					numerator -= b.getL(d) * o.getDemand(d.getID());
					denominator += o.getDemand(d.getID());
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
