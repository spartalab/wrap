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

import edu.utexas.wrap.DestinationMatrix;
import edu.utexas.wrap.NetworkLoader;
import edu.utexas.wrap.VehicleClass;
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
	
	private static HashMap<Node, Float> readDestinationDemand(BufferedReader of, Graph g) throws IOException {
		String[] cols;
		Integer destID;
		Float demand;
		HashMap<Node, Float> dests = new HashMap<Node, Float>();

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
	
	public static void readEnhancedTrips(File odMatrix, Graph g, NetworkLoader dl) throws FileNotFoundException, IOException {
		DestinationMatrix origMap = null;
		BufferedReader matrixFile = new BufferedReader(new FileReader(odMatrix));
		String line;
		Integer curOrig = null;
//		Set<Origin> origins = new HashSet<Origin>();
//		Set<BushBuilder> pool = new HashSet<BushBuilder>();
		Map<Node, Float> solo17 = null, solo35 = null, solo45 = null, solo90 = null,

				hov17 = null, hov35 = null, hov45 = null, hov90 = null,

				medTrucks = null, hvyTrucks = null;

		// read each line and map to correct bush identity
		while (true) {
			line = matrixFile.readLine();
			if (line == null || line.trim().equals("")) {
				if (curOrig != null) {
					// build previous bushes

					origMap = new DestinationMatrix();

					Map<Float, Map<Node, Float>> soMap = new HashMap<Float, Map<Node, Float>>();
					Map<Float, Map<Node, Float>> hoMap = new HashMap<Float, Map<Node, Float>>();
					Map<Float, Map<Node, Float>> mtMap = new HashMap<Float, Map<Node, Float>>();
					Map<Float, Map<Node, Float>> htMap = new HashMap<Float, Map<Node, Float>>();

					soMap.put(0.17F, solo17);
					soMap.put(0.35F, solo35);
					soMap.put(0.45F, solo45);
					soMap.put(0.90F, solo90);

					hoMap.put(0.17F, hov17);
					hoMap.put(0.35F, hov35);
					hoMap.put(0.45F, hov45);
					hoMap.put(0.90F, hov90);

					mtMap.put(1.0F, medTrucks);
					htMap.put(1.0F, hvyTrucks);

					origMap.put(VehicleClass.SINGLE_OCC, soMap);
					origMap.put(VehicleClass.HIGH_OCC, hoMap);
					origMap.put(VehicleClass.MED_TRUCK, mtMap);
					origMap.put(VehicleClass.HVY_TRUCK, htMap);
					
					dl.add(g.getNode(curOrig), origMap);

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
					origMap = new DestinationMatrix();

					Map<Float, Map<Node, Float>> soMap = new HashMap<Float, Map<Node, Float>>();
					Map<Float, Map<Node, Float>> hoMap = new HashMap<Float, Map<Node, Float>>();
					Map<Float, Map<Node, Float>> mtMap = new HashMap<Float, Map<Node, Float>>();
					Map<Float, Map<Node, Float>> htMap = new HashMap<Float, Map<Node, Float>>();

					soMap.put(0.17F, solo17);
					soMap.put(0.35F, solo35);
					soMap.put(0.45F, solo45);
					soMap.put(0.90F, solo90);

					hoMap.put(0.17F, hov17);
					hoMap.put(0.35F, hov35);
					hoMap.put(0.45F, hov45);
					hoMap.put(0.90F, hov90);

					mtMap.put(1.0F, medTrucks);
					htMap.put(1.0F, hvyTrucks);

					origMap.put(VehicleClass.SINGLE_OCC, soMap);
					origMap.put(VehicleClass.HIGH_OCC, hoMap);
					origMap.put(VehicleClass.MED_TRUCK, mtMap);
					origMap.put(VehicleClass.HVY_TRUCK, htMap);

					dl.add(g.getNode(curOrig), origMap);

				}

				// Reset maps
				System.out.print("\rBuilding bushes for origin " + orig);
				curOrig = orig;
				solo17 = new HashMap<Node, Float>(g.getNumZones() / 2, 1.0f);
				solo35 = new HashMap<Node, Float>(g.getNumZones() / 2, 1.0f);
				solo45 = new HashMap<Node, Float>(g.getNumZones() / 2, 1.0f);
				solo90 = new HashMap<Node, Float>(g.getNumZones() / 2, 1.0f);

				hov17 = new HashMap<Node, Float>(g.getNumZones() / 2, 1.0f);
				hov35 = new HashMap<Node, Float>(g.getNumZones() / 2, 1.0f);
				hov45 = new HashMap<Node, Float>(g.getNumZones() / 2, 1.0f);
				hov90 = new HashMap<Node, Float>(g.getNumZones() / 2, 1.0f);

				medTrucks = new HashMap<Node, Float>(g.getNumZones() / 2, 1.0f);
				hvyTrucks = new HashMap<Node, Float>(g.getNumZones() / 2, 1.0f);
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
	public static void readTNTPOriginSpecificProportionalVOTDemand(File odMatrix, Map<Node, List<Float[]>> VOTs, Graph g, NetworkLoader dl)
			throws FileNotFoundException, IOException {
		/////////////////////////////////////
		// Read OD Matrix and assign flows
		/////////////////////////////////////
		BufferedReader of = new BufferedReader(new FileReader(odMatrix));
		String line;
//		Set<BushBuilder> pool = new HashSet<BushBuilder>();
		HashMap<Node, Float> bushDests;
		
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
			HashMap<Node, Float> dests = readDestinationDemand(of, g);

			Map<Float, Map<Node, Float>> nmap = new HashMap<Float, Map<Node, Float>>();
			for (Float[] entry : VOTs.get(root)) {
				bushDests = new HashMap<Node, Float>();
				for (Node temp : dests.keySet()) {
					bushDests.put(temp, entry[1] * dests.get(temp));
				}
				nmap.put(entry[0], bushDests);
			}

			DestinationMatrix omap = new DestinationMatrix();
			omap.put(null, nmap);
			dl.add(root, omap);


			line = of.readLine();

		}
		of.close();

	}

	public static void readTNTPUniformVOTtrips(File VOTfile, File odMatrix, Graph g, NetworkLoader dl) throws FileNotFoundException {
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
