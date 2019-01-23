package edu.utexas.wrap.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import edu.utexas.wrap.VehicleClass;
import edu.utexas.wrap.assignment.AssignmentLoader;
import edu.utexas.wrap.demand.AutomotiveDemandMap;
import edu.utexas.wrap.demand.AutomotiveOriginDestinationMatrix;
import edu.utexas.wrap.demand.DemandMap;
import edu.utexas.wrap.net.Graph;
import edu.utexas.wrap.net.Node;

public class OriginFactory {
	
	private static Float parse(String s) {
		try {
			return Float.parseFloat(s);
		} catch (NumberFormatException e) {
			return 0.0F;
		}
	}
	
	private static DemandMap readDestinationDemand(BufferedReader of, Graph g) throws IOException {
		String[] cols;
		Integer destID;
		Float demand;
		AutomotiveDemandMap dests = new AutomotiveDemandMap(null);

		while (true) {
			String line = of.readLine();
			if (line == null || line.trim().startsWith("O") || line.trim().equals(""))
				break; // If we've reached the gap, move to the next origin
			String[] entries = line.trim().split(";");

			for (String entry : entries) { // For each entry on this line
				cols = entry.split(":"); // Get its values
				destID = Integer.parseInt(cols[0].trim());
				demand = Float.parseFloat(cols[1].trim());
				if (demand > 0.0)
					dests.put(g.getNode(destID), demand);
			}
		}
		return dests;
	}
	
	public static void readEnhancedTrips(File odMatrix, Graph g, AssignmentLoader dl) throws FileNotFoundException, IOException {
		BufferedReader matrixFile = new BufferedReader(new FileReader(odMatrix));
		String line;
		Integer curOrig = null;
		AutomotiveOriginDestinationMatrix odda17 = new AutomotiveOriginDestinationMatrix(0.17F, VehicleClass.SINGLE_OCC);
		AutomotiveOriginDestinationMatrix odda35 = new AutomotiveOriginDestinationMatrix(0.35F, VehicleClass.SINGLE_OCC);
		AutomotiveOriginDestinationMatrix odda45 = new AutomotiveOriginDestinationMatrix(0.45F, VehicleClass.SINGLE_OCC);
		AutomotiveOriginDestinationMatrix odda90 = new AutomotiveOriginDestinationMatrix(0.90F, VehicleClass.SINGLE_OCC);
		
		AutomotiveOriginDestinationMatrix odsr17 = new AutomotiveOriginDestinationMatrix(0.17F, VehicleClass.HIGH_OCC);
		AutomotiveOriginDestinationMatrix odsr35 = new AutomotiveOriginDestinationMatrix(0.35F, VehicleClass.HIGH_OCC);
		AutomotiveOriginDestinationMatrix odsr45 = new AutomotiveOriginDestinationMatrix(0.45F, VehicleClass.HIGH_OCC);
		AutomotiveOriginDestinationMatrix odsr90 = new AutomotiveOriginDestinationMatrix(0.90F, VehicleClass.HIGH_OCC);
		
		AutomotiveOriginDestinationMatrix odmt = new AutomotiveOriginDestinationMatrix(1.0F, VehicleClass.MED_TRUCK);
		AutomotiveOriginDestinationMatrix odht = new AutomotiveOriginDestinationMatrix(1.0F, VehicleClass.HVY_TRUCK);
		
		AutomotiveDemandMap solo17 = null, solo35 = null, solo45 = null, solo90 = null,
				hov17 = null, hov35 = null, hov45 = null, hov90 = null,
				medTrucks = null, hvyTrucks = null;

		// read each line and map to correct bush identity
		while (true) {
			line = matrixFile.readLine();
			if (line == null || line.trim().equals("")) {
				if (curOrig != null) {
					// build previous bushes
					
					dl.add(g.getNode(curOrig), solo17);
					dl.add(g.getNode(curOrig), solo35);
					dl.add(g.getNode(curOrig), solo45);
					dl.add(g.getNode(curOrig), solo90);

					dl.add(g.getNode(curOrig), hov17);
					dl.add(g.getNode(curOrig), hov35);
					dl.add(g.getNode(curOrig), hov45);
					dl.add(g.getNode(curOrig), hov90);
					
					dl.add(g.getNode(curOrig), medTrucks);
					dl.add(g.getNode(curOrig), hvyTrucks);
					
					dl.load(g.getNode(curOrig));
				}
				break;
			}
			String[] args = line.split(",");

			Integer orig = Integer.parseInt(args[0]);
			Node dest = g.getNode(Integer.parseInt(args[1]));

			Float da35 = parse(args[2]);
			Float da90 = parse(args[3]);
			Float sr35 = parse(args[4]);
			Float sr90 = parse(args[5]);
			Float da17 = parse(args[6]);
			Float da45 = parse(args[7]);
			Float sr17 = parse(args[8]);
			Float sr45 = parse(args[9]);
			Float mdtk = parse(args[10]);
			Float hvtk = parse(args[11]);

			if (curOrig == null || !orig.equals(curOrig)) {
				// Moving on to next origin
				if (curOrig != null) {
					// build previous origin's bushes
					dl.add(g.getNode(curOrig), solo17);
					dl.add(g.getNode(curOrig), solo35);
					dl.add(g.getNode(curOrig), solo45);
					dl.add(g.getNode(curOrig), solo90);

					dl.add(g.getNode(curOrig), hov17);
					dl.add(g.getNode(curOrig), hov35);
					dl.add(g.getNode(curOrig), hov45);
					dl.add(g.getNode(curOrig), hov90);
					
					dl.add(g.getNode(curOrig), medTrucks);
					dl.add(g.getNode(curOrig), hvyTrucks);
					
					dl.load(g.getNode(curOrig));

				}

				// Reset maps
				System.out.print("\rBuilding bushes for origin " + orig);
				curOrig = orig;
				solo17 = new AutomotiveDemandMap(odda17);
				solo35 = new AutomotiveDemandMap(odda35);
				solo45 = new AutomotiveDemandMap(odda45);
				solo90 = new AutomotiveDemandMap(odda90);

				hov17 = new AutomotiveDemandMap(odsr17);
				hov35 = new AutomotiveDemandMap(odsr35);
				hov45 = new AutomotiveDemandMap(odsr45);
				hov90 = new AutomotiveDemandMap(odsr90);

				medTrucks = new AutomotiveDemandMap(odmt);
				hvyTrucks = new AutomotiveDemandMap(odht);
			}

			if (da17 > 0.0F)
				solo17.put(dest, da17);
			if (da35 > 0.0F)
				solo35.put(dest, da35);
			if (da45 > 0.0F)
				solo45.put(dest, da45);
			if (da90 > 0.0F)
				solo90.put(dest, da90);

			if (sr17 > 0.0F)
				hov17.put(dest, sr17);
			if (sr35 > 0.0F)
				hov35.put(dest, sr35);
			if (sr45 > 0.0F)
				hov45.put(dest, sr45);
			if (sr90 > 0.0F)
				hov90.put(dest, sr90);

			if (mdtk > 0.0F)
				medTrucks.put(dest, mdtk);
			if (hvtk > 0.0F)
				hvyTrucks.put(dest, hvtk);
		}
		
		matrixFile.close();
	}

	/**
	 * Used when each origin has a specific VOT proportion breakdown with a single
	 * class
	 * 
	 * @param odMatrix
	 * @param VOTs
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static void readTNTPOriginSpecificProportionalVOTDemand(File odMatrix, Map<Node, List<Float[]>> VOTs, Graph g, AssignmentLoader dl)
			throws FileNotFoundException, IOException {
		/////////////////////////////////////
		// Read OD Matrix and assign flows
		/////////////////////////////////////
		BufferedReader of = new BufferedReader(new FileReader(odMatrix));
		String line;
		
		Map<Float, AutomotiveOriginDestinationMatrix> ods = new HashMap<Float, AutomotiveOriginDestinationMatrix>();
		
		
		do { // Move past headers in the file
			line = of.readLine();
		} while (!line.startsWith("Origin"));

		while (true) { // While more Origins to read
			while (line != null && !line.startsWith("O"))
				line = of.readLine(); // Read in the origin header
			if (line == null || line.trim().equals(""))
				break; // If the origin header is empty, we've reached the end of the list

			Integer origID = Integer.parseInt(line.trim().split("\\s+")[1]);
			Node root = g.getNode(origID); // Retrieve the existing node with that ID
			System.out.print("\rBuilding bushes for origin " + origID);
			DemandMap unified = readDestinationDemand(of, g);

			
			for (Float[] entry : VOTs.get(root)) {
				ods.putIfAbsent(entry[0], new AutomotiveOriginDestinationMatrix(entry[0], null)); //Ensure a parent OD matrix exists
				AutomotiveDemandMap split = new AutomotiveDemandMap(ods.get(entry[0]));	//Attach the parent OD
				
				for (Node dest : unified.keySet()) { //Split each destination proportionally
					split.put(dest, entry[1] * unified.get(dest));
				}
				dl.add(root,split);
			}
			
			dl.load(root);
			
			line = of.readLine();

		}
		of.close();

	}

	public static void readTNTPUniformVOTtrips(File VOTfile, File odMatrix, Graph g, AssignmentLoader dl) throws FileNotFoundException {
		if (g == null)
			throw new RuntimeException("Graph must be constructed before reading OD matrix");
		try {
			readTNTPOriginSpecificProportionalVOTDemand(odMatrix, readUniformVOTDistrib(VOTfile,g), g, dl);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param VOTfile
	 * @param graph
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static Map<Node, List<Float[]>> readUniformVOTDistrib(File VOTfile, Graph g) throws FileNotFoundException, IOException {

		BufferedReader vf = new BufferedReader(new FileReader(VOTfile));
		LinkedList<Float[]> VOTs = new LinkedList<Float[]>();

		String line;
		vf.readLine(); // Ignore header line
		do {
			line = vf.readLine();
			if (line == null)
				break;
			String[] args = line.split("\t");
			Float vot = parse(args[0]);
			Float vProp = parse(args[1]);
			Float[] entry = { vot, vProp };
			VOTs.add(entry);
		} while (!line.equals(""));
		vf.close();

		Map<Node, List<Float[]>> votMap = new HashMap<Node, List<Float[]>>();
		for (Node n : g.getNodes()) {
			votMap.put(n, VOTs);
		}
		return votMap;
	}
}