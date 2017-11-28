package edu.utexas.wrap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Network {

	//private Map<Integer, Node> nodes;
	protected Set<Link> links;
	protected Set<Origin> origins;
	
	
	public Network(/*Map<Integer, Node> nodes,*/ Set<Link> links, Set<Origin> origins) {
		//setNodes(nodes);
		setLinks(links);
		setOrigins(origins);
	}
	
	public Network(Set<Link> links, Origin origin) {
		setLinks(links);
		Set<Origin> origins = new HashSet<Origin>();
		origins.add(origin);
		setOrigins(origins);
	}
	
	public static Network fromFiles(File linkFile, File odMatrix) throws IOException {
		// Initialization
		Map<Integer,Node> nodes = new HashMap<Integer, Node>();
		Set<Link> links = new HashSet<Link>();
		Set<Origin> origins = new HashSet<Origin>();
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
			line = lf.readLine();
			if (line == null) break;	// End of link list reached
			if (line.startsWith("~") || line.trim().equals("")) continue;
			line = line.trim();
			String[] cols 	= line.split("\t");
			Integer tail 	= Integer.parseInt(cols[0]);
			Integer head 	= Integer.parseInt(cols[1]);
			Float capacity 	= Float.parseFloat(cols[2]);
			Float length 	= Float.parseFloat(cols[3]);
			Float fftime 	= Float.parseFloat(cols[4]);
			Float B 		= Float.parseFloat(cols[5]);
			Float power 	= Float.parseFloat(cols[6]);
			//System.out.println(""+orig.toString());
			
			//Create new node(s) if new, then add to map
			if (!nodes.containsKey(tail)) nodes.put(tail,new Node(tail));
			if (!nodes.containsKey(head)) nodes.put(head,new Node(head));
			
			//Construct new link and add to the list
			Link link = new Link(nodes.get(tail), nodes.get(head), capacity, length, fftime, B, power);
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
			
			String[] entries;
			HashMap<Integer, Float> dests = new HashMap<Integer, Float>();
			while (true) {
				line = of.readLine();
				if (line.trim().startsWith("O") || line.trim().equals("")) break; // If we've reached the gap, move to the next origin
				entries = line.trim().split(";");
				
				for (String entry : entries) {	// For each entry on this line
					String[] cols = entry.split(":");	// Get its values
					Integer destID = Integer.parseInt(cols[0].trim());
					Float demand = Float.parseFloat(cols[1].trim());
					dests.put(destID, demand);
				}
			}
			Origin o = new Origin(old, dests); 	// Construct an origin to replace it
			o.buildBush(links, nodes);
			nodes.put(origID, o); // Replace the node with its origin equivalent
			origins.add(o);
			
			while (line != null && !line.startsWith("O")) line = of.readLine(); // Read in the origin header
			if (line == null || line.trim().equals("")) break; // If the origin header is empty, we've reached the end of the list
		}
		of.close();
		return new Network(links, origins);
		
	}
	
	public Set<Link> getLinks() {
		return links;
	}
	private void setLinks(Set<Link> links) {
		this.links = links;
	}
//	public Collection<Node> getNodes() {
//		return nodes.values();
//	}
//	private void setNodes(Map<Integer, Node> nodes2) {
//		this.nodes = nodes2;
//	}
	public Set<Origin> getOrigins() {
		return origins;
	}
	private void setOrigins(Set<Origin> origins) {
		this.origins = origins;
	}
	
	
	public Float tstt(){
		Float tstt = 0.0f;
		
		for(Link l:links){
			tstt += l.getTravelTime();
		}
		return tstt;
	}
}
