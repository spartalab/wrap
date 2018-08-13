package edu.utexas.wrap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
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

	public void readTNTPUniformVOTtrips(File VOTfile, File odMatrix) throws FileNotFoundException {
		if (g == null) throw new RuntimeException("Graph must be constrcuted before reading OD matrix");
		try {
			readTNTPOriginSpecificProportionalVOTDemand(odMatrix,readUniformVOTDistrib(VOTfile));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Used when each origin has a specific VOT proportion breakdown with a single class
	 * 
	 * @param odMatrix
	 * @param VOTs
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void readTNTPOriginSpecificProportionalVOTDemand(File odMatrix, Map<Node, List<Double[]>> VOTs)
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
				o.buildBush(g, entry[0], bushDests, null);
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
			if (!nodes.containsKey(tail)) nodes.put(tail,new Node(tail));
			
			if (!nodes.containsKey(head)) nodes.put(head,new Node(head));
			

			//Construct new link and add to the list
			Link link = new TolledBPRLink(nodes.get(tail), nodes.get(head), capacity, length, fftime, B, power, toll);
			g.addLink(link);

			nodes.get(tail).addOutgoing(link);
			nodes.get(head).addIncoming(link);
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

	public void readEnhancedGraph(File f) throws FileNotFoundException, IOException {
		String line;
		g = new Graph();
		BufferedReader lf = new BufferedReader(new FileReader(f));
		Map<Integer,Node> nodes = new HashMap<Integer, Node>();
		lf.readLine();	//skip header
		
		while (true) {
			line = lf.readLine();
			if (line == null || line.equals("")) break;
			
			String[] args = line.split(",");

			Integer nodeA = Integer.parseInt(args[1]);
			Integer nodeB = Integer.parseInt(args[2]);
			
			if (!nodes.containsKey(nodeA)) nodes.put(nodeA, new Node(nodeA));
			if (!nodes.containsKey(nodeB)) nodes.put(nodeB, new Node(nodeB));
			
			Double aCap		= parse(args[3]);
			Double bCap		= parse(args[4]);
			Double length	= parse(args[5]);
			Double ffTimeA	= parse(args[6]);
			Double ffTimeB	= parse(args[7]);
			
			BigDecimal alpha	= BigDecimal.valueOf(parse(args[8]));
			BigDecimal epsilon	= BigDecimal.valueOf(parse(args[9]));
			BigDecimal sParA	= BigDecimal.valueOf(parse(args[10]));
			BigDecimal sParB	= BigDecimal.valueOf(parse(args[11]));
			BigDecimal satFlowA	= BigDecimal.valueOf(parse(args[12]));
			BigDecimal satFlowB	= BigDecimal.valueOf(parse(args[13]));
			
			BigDecimal caA = BigDecimal.valueOf(parse(args[14]));
			BigDecimal cbA = BigDecimal.valueOf(parse(args[15]));
			BigDecimal ccA = BigDecimal.valueOf(parse(args[16]));
			BigDecimal cdA = BigDecimal.valueOf(parse(args[17]));
			
			BigDecimal caB = BigDecimal.valueOf(parse(args[18]));
			BigDecimal cbB = BigDecimal.valueOf(parse(args[19]));
			BigDecimal ccB = BigDecimal.valueOf(parse(args[20]));
			BigDecimal cdB = BigDecimal.valueOf(parse(args[21]));
			
			BigDecimal minDel	= BigDecimal.valueOf(parse(args[22]));
			BigDecimal uParA	= BigDecimal.valueOf(parse(args[23]));
			BigDecimal uParB	= BigDecimal.valueOf(parse(args[24]));
			BigDecimal opCostA	= BigDecimal.valueOf(parse(args[25]));
			BigDecimal opCostB	= BigDecimal.valueOf(parse(args[26]));
			
			Map<VehicleClass, BigDecimal> tollsA = new HashMap<VehicleClass, BigDecimal>();
			Map<VehicleClass, BigDecimal> tollsB = new HashMap<VehicleClass, BigDecimal>();
			
			tollsA.put(VehicleClass.SINGLE_OCC, BigDecimal.valueOf(parse(args[27])));
			tollsB.put(VehicleClass.SINGLE_OCC, BigDecimal.valueOf(parse(args[28])));
			tollsA.put(VehicleClass.HIGH_OCC, BigDecimal.valueOf(parse(args[29])));
			tollsB.put(VehicleClass.HIGH_OCC, BigDecimal.valueOf(parse(args[30])));
			tollsA.put(VehicleClass.MED_TRUCK, BigDecimal.valueOf(parse(args[31])));
			tollsB.put(VehicleClass.MED_TRUCK, BigDecimal.valueOf(parse(args[32])));
			tollsA.put(VehicleClass.HVY_TRUCK, BigDecimal.valueOf(parse(args[33])));
			tollsB.put(VehicleClass.HVY_TRUCK, BigDecimal.valueOf(parse(args[34])));
			
			Map<VehicleClass, Boolean> allowed = new HashMap<VehicleClass, Boolean>();
			
			allowed.put(VehicleClass.SINGLE_OCC, !Boolean.parseBoolean(args[35].trim()));
			
			// TODO: find out which links are not symmetric
			if (aCap > 0.0) {
				TolledEnhancedLink AB = new TolledEnhancedLink(nodes.get(nodeA), nodes.get(nodeB), aCap, length, ffTimeA, alpha, epsilon, sParA, uParA, satFlowA, minDel, opCostA, caA, cbA, ccA, cdA, allowed, tollsA);
				g.addLink(AB);
				nodes.get(nodeA).addOutgoing(AB);
				nodes.get(nodeB).addIncoming(AB);
			}
			
			if (bCap > 0.0) {
				TolledEnhancedLink BA = new TolledEnhancedLink(nodes.get(nodeB), nodes.get(nodeA), bCap, length, ffTimeB, alpha, epsilon, sParB, uParB, satFlowB, minDel, opCostB, caB, cbB, ccB, cdB, allowed, tollsB);
				g.addLink(BA);
				nodes.get(nodeB).addOutgoing(BA);
				nodes.get(nodeA).addIncoming(BA);
			}
		}
		lf.close();
	}
	
	private Double parse(String s) {
		try {
			return Double.parseDouble(s);
		} catch (NumberFormatException e) {
			return 0.0;
		}
	}

	public void readEnhancedTrips(File odMatrix) throws FileNotFoundException, IOException {
		// TODO Auto-generated method stub
		Map<Integer, 				// Origin
			Map<VehicleClass, 		// VehicleClass
				Map<Double, 		// VOT
					Map<Integer,	// Destination
						Double>>>> origMap = new HashMap<Integer, Map<VehicleClass, Map<Double, Map<Integer, Double>>>>();
		BufferedReader of = new BufferedReader(new FileReader(odMatrix));
		String line;
		
		// read each line and map to correct bush identity
		while (true) {
			line = of.readLine();
			if (line == null || line.trim().equals("")) break;
			String[] args = line.split(",");
			
			Integer orig = Integer.parseInt(args[0]);
			Integer dest = Integer.parseInt(args[1]);
			
			Double da35 = parse(args[2]);
			Double da90 = parse(args[3]);
			Double sr35 = parse(args[4]);
			Double sr90 = parse(args[5]);
			Double da17 = parse(args[6]);
			Double da45 = parse(args[7]);
			Double sr17 = parse(args[8]);
			Double sr45 = parse(args[9]);
			Double mdtk = parse(args[10]);
			Double hvtk = parse(args[11]);
			
			origMap.putIfAbsent(orig,  new HashMap<VehicleClass, Map<Double, Map<Integer, Double>>>());
			
			Map<VehicleClass, Map<Double, Map<Integer, Double>>> classMap = origMap.get(orig);
			
			classMap.putIfAbsent(VehicleClass.SINGLE_OCC, new HashMap<Double, Map<Integer, Double>>());
			classMap.putIfAbsent(VehicleClass.HIGH_OCC, new HashMap<Double, Map<Integer, Double>>());

			
			
			
			Map<Double, Map<Integer, Double>> daMap = classMap.get(VehicleClass.SINGLE_OCC);
			
			daMap.putIfAbsent(0.17, new HashMap<Integer, Double>());
			daMap.putIfAbsent(0.35, new HashMap<Integer, Double>());
			daMap.putIfAbsent(0.45, new HashMap<Integer, Double>());
			daMap.putIfAbsent(0.90, new HashMap<Integer, Double>());
			
			if (da17 > 0.0) daMap.get(0.17).put(dest, da17);
			if (da35 > 0.0) daMap.get(0.35).put(dest, da35);
			if (da45 > 0.0) daMap.get(0.45).put(dest, da45);
			if (da90 > 0.0) daMap.get(0.90).put(dest, da90);
			
			Map<Double, Map<Integer, Double>> srMap = classMap.get(VehicleClass.HIGH_OCC);
			
			srMap.putIfAbsent(0.17, new HashMap<Integer, Double>());
			srMap.putIfAbsent(0.35, new HashMap<Integer, Double>());
			srMap.putIfAbsent(0.45, new HashMap<Integer, Double>());
			srMap.putIfAbsent(0.90, new HashMap<Integer, Double>());
			
			if (sr17 > 0.0) srMap.get(0.17).put(dest, sr17);
			if (sr35 > 0.0) srMap.get(0.35).put(dest, sr35);
			if (sr45 > 0.0) srMap.get(0.45).put(dest, sr45);
			if (sr90 > 0.0) srMap.get(0.90).put(dest, sr90);
			
			if (mdtk > 0.0) {
				classMap.putIfAbsent(VehicleClass.MED_TRUCK, new HashMap<Double, Map<Integer, Double>>());
				Map<Double, Map<Integer, Double>> mtMap = classMap.get(VehicleClass.MED_TRUCK);
				mtMap.putIfAbsent(1.0, new HashMap<Integer, Double>());
				mtMap.get(1.0).put(dest, mdtk);
			}
			
			if (hvtk > 0.0) {
				classMap.putIfAbsent(VehicleClass.HVY_TRUCK, new HashMap<Double, Map<Integer, Double>>());
				Map<Double, Map<Integer, Double>> htMap = classMap.get(VehicleClass.HVY_TRUCK);
				htMap.putIfAbsent(1.0, new HashMap<Integer, Double>());
				htMap.get(1.0).put(dest, hvtk);
			}
			
		}
		of.close();
		
		// Now create origins and bushes for network
		origins = new HashSet<Origin>();
		for (Integer orig : origMap.keySet()) {
			Map<VehicleClass, Map<Double, Map<Integer, Double>>> classMap = origMap.get(orig);
			Origin o = new Origin(g.getNode(orig));
			
			for (VehicleClass c : VehicleClass.values()) {
				Map<Double, Map<Integer, Double>> votMap = classMap.get(c);
				for (Double vot : votMap.keySet()) {
					o.buildBush(g, vot, votMap.get(vot), c);
				}
			}
			
			origins.add(o);
		}
	}
}
