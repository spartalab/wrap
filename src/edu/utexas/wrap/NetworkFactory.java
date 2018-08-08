package edu.utexas.wrap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NetworkFactory {
	private Graph g;
	private Set<Origin> origins;

	public NetworkFactory() {}

	public Network getNetwork() {
		if (g == null) 				throw new RuntimeException("Graph has not been constructed. Use a Graph-reading method");
		else if (origins == null)	throw new RuntimeException("OD Matrix has not been constructed. Use an OD Matrix-reading method");
		return new Network(origins,g);
	}

	public void readTNTPUniformODs(File VOTfile, File odMatrix) throws FileNotFoundException {
		if (g == null) throw new RuntimeException("Graph must be constrcuted before reading OD matrix");
		try {
			readMultipleVOTSingleTypeMatrix(odMatrix,readUniformVOTDistrib(VOTfile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * @param odMatrix
	 * @param VOTs
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void readMultipleVOTSingleTypeMatrix(File odMatrix, Map<Node, List<Double[]>> VOTs)
			throws FileNotFoundException, IOException {
		/////////////////////////////////////
		// Read OD Matrix and assign flows
		/////////////////////////////////////
		BufferedReader of = new BufferedReader(new FileReader(odMatrix));
		String line;

		HashMap<Integer, Double> bushDests;
		origins = new HashSet<Origin>();

		do { // Move past headers in the file
			line = of.readLine();
		} while (!line.startsWith("Origin"));

		while (true) { //While more Origins to read
			while (line != null && !line.startsWith("O")) line = of.readLine(); // Read in the origin header
			if (line == null || line.trim().equals("")) break; // If the origin header is empty, we've reached the end of the list

			Integer origID = Integer.parseInt(line.trim().split("\\s+")[1]);
			Node root = g.getNode(origID);	// Retrieve the existing node with that ID

			HashMap<Integer, Double> dests = readDestinationDemand(of);
			Origin o = new Origin(root); 	// Construct an origin to replace it

			for (Double[] entry : VOTs.get(root)) {
				bushDests = new HashMap<Integer, Double>();
				for (Integer temp : dests.keySet()) {
					bushDests.put(temp, entry[1] * dests.get(temp));
				}
				o.buildBush(g, entry[0], bushDests);
			}
			origins.add(o);
			line = of.readLine();

		}
		of.close();
	}


	/**
	 * @param of
	 * @return
	 * @throws IOException
	 */
	private HashMap<Integer, Double> readDestinationDemand(BufferedReader of) throws IOException {
		String[] cols;
		Integer destID;
		Double demand;
		HashMap<Integer, Double> dests = new HashMap<Integer, Double>();


		while (true) {
			String line = of.readLine();
			if (line == null || line.trim().startsWith("O") || line.trim().equals("")) break; // If we've reached the gap, move to the next origin
			String[] entries = line.trim().split(";");

			for (String entry : entries) {	// For each entry on this line
				cols = entry.split(":");	// Get its values
				destID = Integer.parseInt(cols[0].trim());
				demand = Double.parseDouble(cols[1].trim());
				if (demand > 0.0) dests.put(destID, demand);
			}
		}
		return dests;
	}

	/**
	 * @param linkFile
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void readTNTPGraph(File linkFile) throws FileNotFoundException, IOException {
		String line;
		g = new Graph();
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
			Link link = new TolledBPRLink(nodes.get(tail), nodes.get(head), capacity, length, fftime, B, power, toll);
			g.addLink(link);

			nodes.get(tail).addOutgoing(link);
			nodes.get(head).addIncoming(link);
			links.add(link);
		}
		lf.close();
	}

	/**
	 * @param VOTfile
	 * @param g 
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private Map<Node, List<Double[]>> readUniformVOTDistrib(File VOTfile) throws FileNotFoundException, IOException {

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

}
