package edu.utexas.wrap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GraphFactory {
	public static Graph readTNTPGraph(File linkFile) throws FileNotFoundException, IOException {
		String line;
		Graph g = new Graph();
		BufferedReader lf = new BufferedReader(new FileReader(linkFile));
		Map<Integer, Node> nodes = new HashMap<Integer, Node>();
		Integer ftn = 1;
		do { // Move past headers in the file
			line = lf.readLine();
			if (line.startsWith("<FIRST THRU NODE>")) {
				ftn = Integer.parseInt(line.trim().split("\\s+")[3]);
			}
		} while (!line.startsWith("~"));

		while (true) { // Iterate through each link (row)
			line = lf.readLine();
			if (line == null)
				break; // End of link list reached
			if (line.startsWith("~") || line.trim().equals(""))
				continue;
			line = line.trim();
			String[] cols = line.split("\\s+");
			Integer tail = Integer.parseInt(cols[0]);
			Integer head = Integer.parseInt(cols[1]);
			Float capacity = parse(cols[2]);
			Float length = parse(cols[3]);
			Float fftime = parse(cols[4]);
			Float toll = parse(cols[8]);

			// Create new node(s) if new, then add to map
			if (!nodes.containsKey(tail))
				nodes.put(tail, new Node(tail, tail < ftn? true : false));

			if (!nodes.containsKey(head))
				nodes.put(head, new Node(head, head < ftn? true: false));
			
			// Construct new link and add to the list
			Link link;
			if (tail >= ftn && head >= ftn) {
				Float B = parse(cols[5]);
				Float power = parse(cols[6]);
				link = new TolledBPRLink(nodes.get(tail), nodes.get(head), capacity, length, fftime, B, power, toll);
			}
			else {
				link = new CentroidConnector(nodes.get(tail), nodes.get(head), capacity, length, fftime, toll);
			}
			g.add(link);
		}
		lf.close();
		return g;
	}

	
	public static Graph readEnhancedGraph(File f, Integer thruNode) throws FileNotFoundException, IOException {
		String line;
		Graph g = new Graph();
		int numZones = 0;
		BufferedReader lf = new BufferedReader(new FileReader(f));
		Map<Integer, Node> nodes = new HashMap<Integer, Node>();
		lf.readLine(); // skip header

		while (true) {
			line = lf.readLine();
			if (line == null || line.equals(""))
				break;

			String[] args = line.split(",");

			Integer nodeA = Integer.parseInt(args[1]);
			Integer nodeB = Integer.parseInt(args[2]);

			if (!nodes.containsKey(nodeA)) {
				if (nodeA < thruNode) {
					nodes.put(nodeA, new Node(nodeA, true));
					numZones++;
				} else
					nodes.put(nodeA, new Node(nodeA, false));
			}

			if (!nodes.containsKey(nodeB)) {
				if (nodeB < thruNode) {
					nodes.put(nodeB, new Node(nodeB, true));
					numZones++;
				} else
					nodes.put(nodeB, new Node(nodeB, false));

			}

			Float aCap = parse(args[3]);
			Float bCap = parse(args[4]);
			Float length = parse(args[5]);
			Float ffTimeA = parse(args[6]);
			Float ffTimeB = parse(args[7]);

			Float alpha = (parse(args[8]));
			Float epsilon = (parse(args[9]));
			Float sParA = (parse(args[10]));
			Float sParB = (parse(args[11]));
			Float satFlowA = (parse(args[12]));
			Float satFlowB = (parse(args[13]));

			Float caA = (parse(args[14]));
			Float cbA = (parse(args[15]));
			Float ccA = (parse(args[16]));
			Float cdA = (parse(args[17]));

			Float caB = (parse(args[18]));
			Float cbB = (parse(args[19]));
			Float ccB = (parse(args[20]));
			Float cdB = (parse(args[21]));

			Float minDel = (parse(args[22]));
			Float uParA = (parse(args[23]));
			Float uParB = (parse(args[24]));
			Float opCostA = (parse(args[25]));
			Float opCostB = (parse(args[26]));

			Map<VehicleClass, Float> tollsA = new HashMap<VehicleClass, Float>();
			Map<VehicleClass, Float> tollsB = new HashMap<VehicleClass, Float>();

			tollsA.put(VehicleClass.SINGLE_OCC, (parse(args[27])));
			tollsB.put(VehicleClass.SINGLE_OCC, (parse(args[28])));
			tollsA.put(VehicleClass.HIGH_OCC, (parse(args[29])));
			tollsB.put(VehicleClass.HIGH_OCC, (parse(args[30])));
			tollsA.put(VehicleClass.MED_TRUCK, (parse(args[31])));
			tollsB.put(VehicleClass.MED_TRUCK, (parse(args[32])));
			tollsA.put(VehicleClass.HVY_TRUCK, (parse(args[33])));
			tollsB.put(VehicleClass.HVY_TRUCK, (parse(args[34])));

			Map<VehicleClass, Boolean> allowed = new HashMap<VehicleClass, Boolean>();

			allowed.put(VehicleClass.SINGLE_OCC, !Boolean.parseBoolean(args[35].trim()));

			// TODO: find out which links are not symmetric
			// TODO: figure out what I meant by ^^^ that ^^^ comment
			if (aCap > 0.0) {
				Link AB = null;
				if (satFlowA > 0) {
					AB = new TolledEnhancedLink(nodes.get(nodeA), nodes.get(nodeB), aCap, length, ffTimeA, alpha,
							epsilon, sParA, uParA, satFlowA, minDel, opCostA, caA, cbA, ccA, cdA, allowed, tollsA);
				} else if (aCap > 0.0) {
					AB = new CentroidConnector(nodes.get(nodeA), nodes.get(nodeB), aCap, length, ffTimeA,
							opCostA.floatValue());
				}
				g.add(AB);
			}

			if (bCap > 0.0) {
				Link BA = null;
				if (satFlowB > 0) {
					BA = new TolledEnhancedLink(nodes.get(nodeB), nodes.get(nodeA), bCap, length, ffTimeB, alpha,
							epsilon, sParB, uParB, satFlowB, minDel, opCostB, caB, cbB, ccB, cdB, allowed, tollsB);
				} else {
					BA = new CentroidConnector(nodes.get(nodeB), nodes.get(nodeA), bCap, length, ffTimeB,
							opCostB.floatValue());
				}
				g.add(BA);
			}
		}
		lf.close();
		g.setNumZones(numZones);
		return g;
	}

	private static Float parse(String s) {
		try {
			return Float.parseFloat(s);
		} catch (NumberFormatException e) {
			return 0.0F;
		}
	}

}
