package edu.utexas.wrap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Network {

	//private Map<Integer, Node> nodes;
	protected List<Link> links;
	protected List<Origin> origins;
	
	
	public Network(/*Map<Integer, Node> nodes,*/ List<Link> links, List<Origin> origins) {
		//setNodes(nodes);
		setLinks(links);
		setOrigins(origins);
	}
	
	public Network(List<Link> links, Origin origin) {
		setLinks(links);
		List<Origin> origins = new ArrayList<Origin>();
		origins.add(origin);
		setOrigins(origins);
	}
	
	public static Network fromFiles(File linkFile, File odMatrix) throws IOException {
		// Initialization
		Map<Integer,Node> nodes = new HashMap<Integer, Node>();
		List<Link> links = new ArrayList<Link>();
		List<Origin> origins = new ArrayList<Origin>();
		// Open the files for reading
		BufferedReader lf = new BufferedReader(new FileReader(linkFile));
		BufferedReader of = new BufferedReader(new FileReader(odMatrix));
		
		//////////////////////////////////////////////
		// Read links and build corresponding nodes
		//////////////////////////////////////////////
		String line;
		do { //Move past headers in the file
			line = lf.readLine();
		} while (!line.startsWith("~"));
		
		while (true) { //Iterate through each link (row)
			line = lf.readLine().trim();
			if (line.equals("")) break;	// End of link list reached
			
			String[] cols = line.split("\t");
			Integer tail = Integer.parseInt(cols[0]);
			Integer head = Integer.parseInt(cols[1]);
			Integer capacity = Integer.parseInt(cols[2]);
			Integer length = Integer.parseInt(cols[3]);
			Double fftime = Double.parseDouble(cols[4]);
			Double B = Double.parseDouble(cols[5]);
			Integer power = Integer.parseInt(cols[6]);
			//System.out.println(""+orig.toString());
			
			//Create new node(s) if new, then add to map
			if (!nodes.containsKey(tail)) nodes.put(tail,new Node(tail));
			if (!nodes.containsKey(head)) nodes.put(head,new Node(head));
			
			//Construct new link and add to the list
			links.add(new Link(nodes.get(tail), nodes.get(head), capacity, length, fftime, B, power));
		}
		lf.close();
		
		/////////////////////////////////////
		// Read OD Matrix and assign flows
		/////////////////////////////////////
		do { // Move past headers in the file
			line = of.readLine();
		} while (!line.startsWith("Origin"));
		
		while (true) { //While more Origins to read
			Integer origID = Integer.parseInt(line.trim().split(" ")[1]);
			Node old = nodes.get(origID);	// Retrieve the existing node with that ID
			
			String[] entries;
			HashMap<Node, Double> dests = new HashMap<Node, Double>();
			while (true) {
				line = of.readLine();
				if (line.trim().equals("")) break; // If we've reached the gap, move to the next origin
				entries = line.split(";");
				
				for (String entry : entries) {	// For each entry on this line
					String[] cols = entry.split(":");	// Get its values
					Integer destID = Integer.parseInt(cols[0].trim());
					Node dest = nodes.get(destID);
					Double demand = Double.parseDouble(cols[1].trim());
					dests.put(dest, demand);
				}
			}
			Origin o = new Origin(old, dests); 	// Construct an origin to replace it
			o.buildBush(links, nodes);
			nodes.put(origID, o); // Replace the node with its origin equivalent
			origins.add(o);
			
			line = of.readLine(); // Read in the origin header
			if (line.trim().equals("")) break; // If the origin header is empty, we've reached the end of the list
		}
		of.close();
		return new Network(/*nodes,*/ links, origins);
		
	}
	
	public List<Link> getLinks() {
		return links;
	}
	private void setLinks(List<Link> links) {
		this.links = links;
	}
//	public Collection<Node> getNodes() {
//		return nodes.values();
//	}
//	private void setNodes(Map<Integer, Node> nodes2) {
//		this.nodes = nodes2;
//	}
	public List<Origin> getOrigins() {
		return origins;
	}
	private void setOrigins(List<Origin> origins) {
		this.origins = origins;
	}
	
	
	public Double tstt(){
		Double tstt = 0.0;
		
		for(Link l:links){
			tstt += l.getTravelTime();
		}
		return tstt;
	}
}
